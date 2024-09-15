package de.sunnix.srpge.engine.ecs;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.event.Event;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.ecs.systems.TileAnimationSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.TestCubeRenderObject;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class World {

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    @Getter
    private TileMap map;

    @Getter
    private GameObject player;

    private boolean inited;

    private TestCubeRenderObject tcro = new TestCubeRenderObject();

    private List<Event> blockingEventQueue = new ArrayList<>();

    private int animTimer = -1;

    public void init(ZipFile zip, BetterJSONObject config) throws IOException, InvocationTargetException, IllegalAccessException {
        if(inited)
            return;
        var startMapID = config.get("start_map", -1);
        var startPos = config.getFloatArr("start_map_pos", 3);
        if(startMapID == -1)
            throw new RuntimeException("Invalid start map id: " + startMapID);
        var mapDSO = new DataSaveObject().load(zip.getInputStream(new ZipEntry(String.format("maps\\%04d.map", startMapID))));
        map = new TileMap(mapDSO);

        RenderSystem.initMapGrid(map.width, map.height);
        PhysicSystem.initMapGrid(map.width, map.height);

        // Player
        player = new GameObject(this, new DataSaveObject().load(zip.getInputStream(new ZipEntry("player"))));
        player.size.set(.78, 1.8);
        player.setPosition(startPos[0], startPos[1], startPos[2]);
        {
            var comp = new PhysicComponent(new DataSaveObject());
            comp.setCanClimb(true);
            player.addComponent(comp);
        }
        player.init(this);

        // Load objects
        mapDSO.<DataSaveObject>getList("objects").forEach(o -> {
            var object = new GameObject(this, o);
            object.size.set(1, 1);
            object.init(this);
        });

        gameObjectsToAdd.forEach(go -> gameObjects.put(go.getID(), go));

        for (var i = 0; i < 0; i++){
            var obj = new GameObject(this, 1, 1);
            obj.setPosition((int)(Math.random() * map.width), (int)(Math.random() * 9), (int)(Math.random() * map.height));
            var dso = new DataSaveObject();
            dso.putString("sprite", "objects/box");
            obj.addComponent(new RenderComponent(dso));
            var comp = new PhysicComponent(new DataSaveObject());
            comp.setFlying(true);
            obj.addComponent(comp);
            obj.init(this);
        }

    }

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void movePlayer(float x, boolean y, float z){
        if(y)
            player.getComponent(PhysicComponent.class).jump();
        var pVel = player.getVelocity();
        var moveSpeed = .1f;
        pVel.add(x * moveSpeed, 0, z * moveSpeed);
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
            PhysicSystem.update(this);
            gameObjects.values().forEach(go -> go.update(this));
        }
        var pPos = player.getPosition();
        Camera.getPos().set(pPos.x * 24, (-pPos.z + pPos.y) * 16);
        RenderSystem.prepareRender();
        TileAnimationSystem.update(this);
        AudioManager.get().setLocation(pPos.x, pPos.y, pPos.z);
    }

    public void render(){
        map.render();
        RenderSystem.renderObjects();
        PhysicSystem.renderShadows();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        if(Core.isDebug()) {
            map.drawHitbixes();
            PhysicSystem.renderHitboxes();
        }
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

    public Tile getTile(int x, int y){
        return map.getTile(x, y);
    }
}
