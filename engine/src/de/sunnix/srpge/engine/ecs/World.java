package de.sunnix.srpge.engine.ecs;

import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.ecs.components.Component;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.OldRenderComponent;
import de.sunnix.srpge.engine.ecs.event.Event;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.TestCubeRenderObject;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import org.lwjgl.opengl.GL11;
import test.Textures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class World {

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    private TileMap map;

    @Getter
    private GameObject player;

    private boolean inited;

    private TestCubeRenderObject tcro = new TestCubeRenderObject();

    private List<Event> blockingEventQueue = new ArrayList<>();

    private int animTimer = -1;

    public void init(ZipFile zip, BetterJSONObject config) throws IOException {
        if(inited)
            return;
        var startMapID = config.get("start_map", -1);
        if(startMapID == -1)
            throw new RuntimeException("Invalid start map id: " + startMapID);
        var mapDSO = new DataSaveObject().load(zip.getInputStream(new ZipEntry(String.format("maps\\%04d.map", startMapID))));
        map = new TileMap(mapDSO);

        // Player
        player = new GameObject(this, new DataSaveObject().load(zip.getInputStream(new ZipEntry("player"))));
        player.init(this);

        // Load objects
        mapDSO.<DataSaveObject>getList("objects").forEach(o -> {
            var object = new GameObject(this, o);
            object.init(this);
        });

        gameObjectsToAdd.forEach(go -> gameObjects.put(go.getID(), go));

    }

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void update(){
        Event event = null;
        if(!blockingEventQueue.isEmpty())
            event = blockingEventQueue.get(0);
        if(event != null){
            if((event.getBlockingType() & Event.BLOCK_USER_INPUT) != Event.BLOCK_USER_INPUT)
                animTimer++;
            event.run(this);
            if(event.isFinished(this))
                blockingEventQueue.remove(event);
        } else {
            animTimer++;
            map.update();
            gameObjects.values().forEach(go -> go.update(this));
        }
        var pPos = player.getPosition();
        Camera.getPos().set(pPos.x * 24, (-pPos.z + pPos.y) * 16);
        RenderSystem.prepareRender();
        AudioManager.get().setLocation(pPos.x, pPos.y, pPos.z);
    }

    public void render(){
        map.render();
        RenderSystem.renderObjects();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        tcro.render();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        gameObjects.values().forEach(GameObject::postRender);
    }

    public void postUpdate(){
        gameObjectsToAdd.forEach(o -> gameObjects.put(o.getID(), o));
        gameObjectsToAdd.clear();
        gameObjects.values().stream().filter(GameObject::isToDelete).forEach(o -> gameObjects.remove(o.getID()));
    }

    public GameObject getGameObject(long id){
        return gameObjects.get(id);
    }

    public void onDestroy(){
        map.onDestroy();
    }

    public void addBlockingEvent(Event event) {
        blockingEventQueue.add(event);
    }
}
