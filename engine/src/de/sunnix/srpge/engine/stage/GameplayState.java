package de.sunnix.srpge.engine.stage;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.event.Event;
import de.sunnix.srpge.engine.ecs.event.EventList;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.ecs.systems.TileAnimationSystem;
import de.sunnix.srpge.engine.evaluation.Variables;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.TestCubeRenderObject;
import de.sunnix.srpge.engine.memory.ContextQueue;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import de.sunnix.srpge.engine.util.FunctionUtils;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameplayState implements IState {

    /**
     * The current world where the game plays on
     */
    @Getter
    private World world;
    /**
     * The preloaded map/world that can be applied to {@link GameplayState#world} via {@link GameplayState#switchMaps()}<br>
     * This map is created via {@link GameplayState#loadMap(int)},<br>
     * To fast check if a map is available use {@link GameplayState#checkMapExists(int)}
     */
    @Getter
    private World nextMap;
    /**
     * Player data read from the game file in {@link GameplayState#onStart()}
     */
    private DataSaveObject playerData;
    @Getter
    private GameObject player;
    /**
     * The list of all active event lists on the world
     */
    private final List<EventList> activeEventLists = new ArrayList<>();
    /**
     * Listed events that are blocking the world from updating, like showing a text box, where the world shouldn't update
     */
    @Getter
    private final List<EventList> blockingEventQueue = new ArrayList<>();

    /**
     * Is a blocking event running.<br>
     * @see GameplayState#blockingEventQueue
     */
    @Getter
    private boolean globalEventRunning;
    @Getter
    private boolean playerInputBlock;
    @Getter
    private boolean updateBlock;
    @Getter
    private boolean renderBlock;

    /**
     * Little render object to show the middle of the screen
     */
    private final TestCubeRenderObject tcro = new TestCubeRenderObject();

    @Override
    public void onStart() {
        if(world == null)
            try(var zip = new ZipFile(Core.getGameFile())){
                var config = new BetterJSONObject(new String(zip.getInputStream(new ZipEntry("game.config")).readAllBytes()));
                var version = Arrays.stream(config.get("editor_version", "0.0").split("\\.")).mapToInt(Integer::parseInt).toArray();
                if(version[0] != Core.MAJOR_VERSION || version[1] != Core.MINOR_VERSION)
                    throw new IOException("The version of the GameFile is not equal to the version of the Engine!");
                Resources.get().loadResources(zip);

                var startMapID = config.get("start_map", -1);
                var startPos = config.getFloatArr("start_map_pos", 3);
                if(startMapID == -1)
                    throw new RuntimeException("Invalid start map id: " + startMapID);

                if(!loadMap(startMapID))
                    throw new RuntimeException("The entry map could not be loaded!");

                // load global variables
                var stream = zip.getInputStream(new ZipEntry("res/variables"));
                if(stream != null){
                    Variables.load(new DataSaveObject().load(stream));
                    stream.close();
                } else
                    Variables.load(new DataSaveObject());

                playerData = new DataSaveObject().load(zip.getInputStream(new ZipEntry("player")));

                world = nextMap;
                player = createPlayer();
                player.setPosition(startPos[0], startPos[1], startPos[2]);
                Camera.setAttachedObject(player, true);
                world.init();
            } catch (Exception e){
                GameLogger.logException("World", e);
            }
    }

    @Override
    public void update() {
        renderBlock = activeEventLists.stream().anyMatch(e -> e.getCurrentEventBlockType() == EventList.BlockType.UPDATE_GRAPHIC);
        updateBlock = renderBlock || !blockingEventQueue.isEmpty() || activeEventLists.stream().anyMatch(e -> e.getCurrentEventBlockType() == EventList.BlockType.UPDATE);
        playerInputBlock = updateBlock || activeEventLists.stream().anyMatch(e -> e.getCurrentEventBlockType() == EventList.BlockType.USER_INPUT);
        activeEventLists.removeIf(e -> !e.isActive());

        FunctionUtils.checkForOpenGLErrors("GameplayState - Pre update");

        EventList blockingEventList = null;
        if(!blockingEventQueue.isEmpty())
            blockingEventList = blockingEventQueue.get(0);
        if(blockingEventList != null){
            globalEventRunning = true;
            blockingEventList.run(world);
            if(blockingEventList.getCurrentEventBlockType().ordinal() < EventList.BlockType.UPDATE.ordinal()){
                blockingEventQueue.remove(0);
                if(!blockingEventList.isActive())
                    activeEventLists.remove(blockingEventList);
            }
        } else
            globalEventRunning = false;
        if(!updateBlock) {
            world.update();
            activeEventLists.forEach(el -> el.run(world));
        }
        if(!renderBlock)
            RenderSystem.update();
        TileAnimationSystem.update(world);
        FunctionUtils.checkForOpenGLErrors("GameplayState - update after event");
        var pPos = getPlayer().getPosition();
        Camera.calculateCameraPosition();
        AudioManager.get().setLocation(pPos.x, pPos.y, pPos.z);
        RenderSystem.prepareRender();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post update");
    }

    @Override
    public void render() {
        FunctionUtils.checkForOpenGLErrors("GameplayState - Pre render");
        world.render();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post render world");
        tcro.render();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post render");
    }

    @Override
    public void postUpdate() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        world.onDestroy();
    }

    /**
     * Checks in the game file if a specific map with a mapID exists.
     * @param mapID the ID of the map to search
     * @return if the map has been found
     * @throws RuntimeException if something went wrong reading the game file
     */
    public boolean checkMapExists(int mapID) throws RuntimeException{
        final var search = String.format("maps\\%04d.map", mapID);
        try(var zip = new ZipFile(Core.getGameFile())){
            var entries = zip.entries();
            while(entries.hasMoreElements()){
                var entry = entries.nextElement();
                if(search.equals(entry.getName()))
                    return true;
            }
        } catch (Exception e){
            GameLogger.logException("World", e);
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Loads a map into the nextMap variable
     * @return if the map has been loaded
     */
    public boolean loadMap(int mapID){
        try(var zip = new ZipFile(Core.getGameFile())){
            var mapData = new DataSaveObject().load(zip.getInputStream(new ZipEntry(String.format("maps\\%04d.map", mapID))));
            nextMap = new World(mapID, this, mapData);
            return true;
        } catch (Exception e){
            GameLogger.logException("World", e);
            return false;
        }
    }

    /**
     * Switches the main Map with the nextMap,<br>
     * free all resources of the previous map and<br>
     * creates a new player via {@link GameplayState#createPlayer()}
     */
    public void switchMaps() throws IOException, InvocationTargetException, IllegalAccessException {
        if(nextMap == null)
            throw new NullPointerException("There is no other map to load!");
        var tmp = world;
        world = nextMap;
        nextMap = tmp;
        player = createPlayer();
        nextMap.onDestroy();
        nextMap = null;
        activeEventLists.clear();
        world.init();
        ContextQueue.runQueueOnMain();
        FunctionUtils.checkForOpenGLErrors("GameplayState - switchMaps");
    }

    /**
     * Creates a new Player on the current World from the {@link GameplayState#playerData} created in {@link GameplayState#onStart()} from the game file
     * @return The created player
     */
    private GameObject createPlayer(){
        var player = new GameObject(world, playerData);
        var comp = new PhysicComponent(new DataSaveObject());
        comp.setWidth(.78f);
        comp.setHeight(1.8f);
        comp.setCanClimb(true);
        player.addComponent(comp);
        FunctionUtils.checkForOpenGLErrors("GameplayState - Create Player");
        return player;
    }

    /**
     * Marks the event list as {@link EventList#setActive(boolean) active} and adds it to the {@link #activeEventLists}
     * to run every update until the event list is finished.
     * @param eventList the event list to add.
     * @see EventList
     */
    public void startEventList(EventList eventList){
        eventList.setActive(true);
        activeEventLists.add(eventList);
    }

}
