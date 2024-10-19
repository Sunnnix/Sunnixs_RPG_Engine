package de.sunnix.srpge.engine.ecs;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.Parallax;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.audio.AudioResource;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.event.EventList;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.CombatSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.stage.GameplayState;
import de.sunnix.srpge.engine.util.FunctionUtils;
import de.sunnix.srpge.engine.util.ObjChain;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static de.sunnix.srpge.engine.ecs.Direction.*;

public class World {

    @Getter
    private final GameplayState gameState;
    public final int ID;

    private AudioResource bgm;

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();
    private Set<Long> gameObjectsToRemove = new HashSet<>();

    @Getter
    private TileMap map;
    @Getter
    private Parallax parallax;

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
        var pDSO = dso.getObject("parallax");
        if(pDSO != null) {
            parallax = new Parallax();
            parallax.load(pDSO);
        }
    }

    public void init() throws IOException, InvocationTargetException, IllegalAccessException {
        if(inited)
            return;
        inited = true;

        map.init();
        var am = AudioManager.get();
        am.setBGM(bgm);

        RenderSystem.init(map.width, map.height);
        PhysicSystem.init(map.width, map.height);
        CombatSystem.init(map.width, map.height);

        gameObjectsToAdd.forEach(go -> {
            go.init(this);
            gameObjects.put(go.getID(), go);
        });

        if(parallax != null)
            parallax.init();

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

        gameObjects.values().forEach(go -> go.runInitEvents(this));
    }

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void removeEntity(long entity) {
        gameObjectsToRemove.add(entity);
    }

    public void removeEntity(GameObject entity) {
        removeEntity(entity.getID());
    }

    public void movePlayer(float x, boolean y, float z){
        var player = getPlayer();
        if(player.hasState(States.HURT) || player.hasState(States.DEAD) || gameState.isPlayerInputBlock() || gameState.isUpdateBlock() || gameState.isRenderBlock()) {
            player.removeState(States.MOVING.id());
            return;
        }
        if(y)
            getPlayer().getComponent(PhysicComponent.class).jump();
        var pVel = getPlayer().getVelocity();
        var moveSpeed = .1f;
        pVel.add(x * moveSpeed, 0, z * moveSpeed);

        if(x != 0 || z != 0) {
            player.addState(States.MOVING.id());
            if(Math.abs(x) > Math.abs(z))
                if(x > 0)
                    player.setFacing(EAST);
                else
                    player.setFacing(WEST);
            else
            if(z > 0)
                player.setFacing(SOUTH);
            else
                player.setFacing(NORTH);
        } else
            player.removeState(States.MOVING.id());
    }

    public void startPlayerAction() {
        if(gameState.isPlayerInputBlock() || gameState.isUpdateBlock() || gameState.isRenderBlock())
            return;
        var player = getPlayer();
        var playerPos = player.getPosition();
        var hitbox = switch (player.getFacing()){
            case SOUTH -> new AABB(playerPos.x, playerPos.y, playerPos.z + player.size.x, player.size.x, player.size.y);
            case EAST -> new AABB(playerPos.x + player.size.x, playerPos.y, playerPos.z, player.size.x, player.size.y);
            case WEST -> new AABB(playerPos.x - player.size.x, playerPos.y, playerPos.z, player.size.x, player.size.y);
            case NORTH -> new AABB(playerPos.x, playerPos.y, playerPos.z - player.size.x, player.size.x, player.size.y);
        };
        var objects = PhysicSystem.getCollidingObjects(hitbox.getX(), hitbox.getZ());
        for(var obj: objects){
            if(obj.equals(player))
                continue;
            var hb = obj.getComponent(PhysicComponent.class).getHitbox();
            if(hb.intersects(hitbox) && obj.containsEventType(PhysicComponent.RUN_TYPE_PLAYER_CONSULT)){
                obj.startEvent(PhysicComponent.RUN_TYPE_PLAYER_CONSULT);
                return;
            }
        }
    }

    public void update(){
        ticks++;
        map.update();
        PhysicSystem.update(this);
        CombatSystem.update(this);
        gameObjects.values().forEach(go -> go.update(this));
    }

    public void render(){
        if(parallax != null && !parallax.isOnTop()) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            parallax.render();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
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
        if(parallax != null && parallax.isOnTop())
            parallax.render();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        gameObjects.values().forEach(GameObject::postRender);
    }

    public void postUpdate(){
        gameObjectsToAdd.forEach(o -> {
            gameObjects.put(o.getID(), o);
            o.init(this);
        });
        gameObjectsToAdd.clear();
        gameObjectsToRemove.forEach(id -> new ObjChain<>(gameObjects.remove(id)).ifPresent(GameObject::free));
    }

    public GameObject getGameObject(long id){
        return gameObjects.get(id);
    }

    public void onDestroy(){
        map.onDestroy();
        gameObjects.values().forEach(GameObject::freeMemory);
    }

    public void addBlockingEvent(EventList event) {
        gameState.getBlockingEventQueue().add(event);
    }

    public Tile getTile(int x, int y){
        return map.getTile(x, y);
    }

    public GameObject getPlayer(){
        return gameState.getPlayer();
    }

}
