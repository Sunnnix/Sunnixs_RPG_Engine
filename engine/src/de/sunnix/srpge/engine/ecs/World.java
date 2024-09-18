package de.sunnix.srpge.engine.ecs;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.audio.AudioResource;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.event.Event;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.stage.GameplayState;
import de.sunnix.srpge.engine.util.FunctionUtils;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.sunnix.srpge.engine.ecs.Direction.*;

public class World {

    @Getter
    private final GameplayState gameState;
    public final int ID;

    private AudioResource bgm;

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    @Getter
    private TileMap map;

    /**
     * The current world ticks<br>
     * How often this world has been {@link World#update() updated}
     */
    @Getter
    private long ticks;

    private boolean inited;

    /**
     * If a world is created it must be inited via {@link World#init() init} before it can be used.
     * @param gameState parent {@link GameplayState GameState}
     * @param dso The map data
     */
    public World(int id, GameplayState gameState, DataSaveObject dso){
        this.ID = id;
        this.gameState = gameState;
        map = new TileMap(dso);

        bgm = Resources.get().getAudio(dso.getString("bgm", null));

        dso.<DataSaveObject>getList("objects").forEach(o -> {
            var object = new GameObject(this, o);
            object.size.set(1, 1);
        });
    }

    public void init() throws IOException, InvocationTargetException, IllegalAccessException {
        if(inited)
            return;
        inited = true;

        map.init();
        var am = AudioManager.get();
        am.setBGM(bgm);
        am.playBGM();

        RenderSystem.init(map.width, map.height);
        PhysicSystem.init(map.width, map.height);

        gameObjectsToAdd.forEach(go -> {
            go.init(this);
            gameObjects.put(go.getID(), go);
        });

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

    public void removeEntity(long entity, boolean destroy) {
        var e = gameObjects.remove(entity);
        if(destroy && e != null)
            e.freeMemory();
    }

    public void removeEntity(GameObject entity, boolean destroy) {
        removeEntity(entity.getID(), destroy);
    }

    public void movePlayer(float x, boolean y, float z){
        if(gameState.isGlobalEventRunnung())
            return;
        if(y)
            getPlayer().getComponent(PhysicComponent.class).jump();
        var pVel = getPlayer().getVelocity();
        var moveSpeed = .1f;
        pVel.add(x * moveSpeed, 0, z * moveSpeed);

        var player = getPlayer();
        if(x != 0 || z != 0) {
            player.addState(States.MOVING.id());
            var comp = player.getComponent(RenderComponent.class);
            if(Math.abs(x) > Math.abs(z))
                if(x > 0)
                    comp.setDirection(EAST);
                else
                    comp.setDirection(WEST);
            else
            if(z > 0)
                comp.setDirection(SOUTH);
            else
                comp.setDirection(NORTH);
        } else
            player.removeState(States.MOVING.id());
    }

    public void update(){
        ticks++;
        map.update();
        PhysicSystem.update(this);
        gameObjects.values().forEach(go -> go.update(this));
    }

    public void render(){
        FunctionUtils.checkForOpenGLErrors("GameplayState - Pre render map");
        map.render();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post render map");
        RenderSystem.renderObjects();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post render objects");
        PhysicSystem.renderShadows();
        FunctionUtils.checkForOpenGLErrors("GameplayState - Post render shadows");
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        if(Core.isDebug()) {
            map.drawHitbixes();
            PhysicSystem.renderHitboxes();
            FunctionUtils.checkForOpenGLErrors("GameplayState - Post render hitboxes");
        }
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
        gameObjects.values().forEach(GameObject::freeMemory);
    }

    public void addBlockingEvent(Event event) {
        gameState.getBlockingEventQueue().add(event);
    }

    public Tile getTile(int x, int y){
        return map.getTile(x, y);
    }

    public GameObject getPlayer(){
        return gameState.getPlayer();
    }

}
