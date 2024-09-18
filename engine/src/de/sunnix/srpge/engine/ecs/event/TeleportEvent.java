package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.stage.GameplayState;
import org.joml.Vector4f;

import static org.joml.Math.lerp;

public class TeleportEvent extends Event{

    public enum TransitionType {
        NONE, BLACK, WHITE, CUSTOM
    }

    /**
     * If map is -1 then no map switch will happen
     */
    protected int map = -1;
    protected int objectID;
    protected float x, y, z;
    protected TransitionType transitionType;
    protected int transitionTime;
    protected GlobalColorTintEvent customTransitionEvent;

    private GameObject object;
    private int startTime, endTime;
    private int processedTime;
    private Vector4f initialColor;
    private boolean cancel;

    private Thread mapLoader;

    public TeleportEvent() {
        super("teleport");
    }

    @Override
    public void load(DataSaveObject dso) {
        map = dso.getInt("map", -1);
        objectID = dso.getInt("object", 0);
        var pos = dso.getFloatArray("pos", 3);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        transitionType = TransitionType.values()[dso.getByte("transition_type", (byte) TransitionType.BLACK.ordinal())];
        transitionTime = dso.getInt("transition_time", 60);
        var obj = dso.getObject("custom_transition_event");
        if(obj != null) {
            customTransitionEvent = new GlobalColorTintEvent();
            customTransitionEvent.load(obj);
        }
    }

    @Override
    public void prepare(World world) {
        object = world.getGameObject(objectID);
        // if teleport is to another map, but object is not player, cancel the teleport
        if(object == null || (map != -1 && map != world.ID && object.getID() != 999)) {
            cancel = true;
            return;
        }
        if(transitionTime < 2 && transitionType != TransitionType.NONE)
            startTime = endTime = 1;
        else {
            startTime = (int)Math.ceil(transitionTime / 2f);
            endTime = transitionTime / 2;
        }
        processedTime = 0;
        if(map != -1 && map != world.ID){
            var gameState = ((GameplayState)Core.getCurrent_game_state().state);
            if(!gameState.checkMapExists(map)) {
                cancel = true;
                GameLogger.logE("Teleport Event", "There is no map with the ID %s", map);
                return;
            }
            mapLoader = new Thread(() -> {
                cancel = !world.getGameState().loadMap(map);
            }, "TeleportEvent - Map Loader");
            mapLoader.setDaemon(true);
            mapLoader.start();
        }
        initialColor = Core.getGlobalColoring().get(new Vector4f());
        GameLogger.logI("Teleport Event", "Start transition to " + this.map);
    }

    @Override
    public void run(World world) {
        if(cancel)
            return;
        switch (transitionType){
            case BLACK, WHITE -> {
                if(processedTime < startTime) {
                    processedTime++;

                    float progress = Math.min((float) processedTime / startTime, 1.0f);

                    var c = transitionType == TransitionType.BLACK ? -1 : 1;
                    var color = new float[]{ c, c, c, 1 };

                    float r = lerp(initialColor.x, color[0], progress);
                    float g = lerp(initialColor.y, color[1], progress);
                    float b = lerp(initialColor.z, color[2], progress);
                    float a = lerp(initialColor.w, color[3], progress);

                    Core.getGlobalColoring().set(r, g, b, a);
                } else if(processedTime == startTime){
                    if(teleport(world)) {
                        processedTime++;
                    }
                } else {
                    processedTime++;
                    float progress = Math.min((float) (processedTime - startTime - 1) / endTime, 1.0f);

                    float r = lerp(initialColor.x, 0, progress);
                    float g = lerp(initialColor.y, 0, progress);
                    float b = lerp(initialColor.z, 0, progress);
                    float a = lerp(initialColor.w, 0, progress);

                    Core.getGlobalColoring().set(r, g, b, a);
                }
            }
            case CUSTOM -> customTransitionEvent.run(world);
            default -> {
                if(teleport(world)) {
                    processedTime++;
                }
            }
        }
    }

    private boolean teleport(World world){
        if(map != -1 && map != world.ID) {
            AudioManager.get().setBGM(null);
            if (mapLoader.isAlive())
                return false;
        }
        initialColor.set(Core.getGlobalColoring());
        try {
            if(map != -1 && map != world.ID) {
                world.getGameState().switchMaps();
                object = world.getGameState().getPlayer();
            }
        } catch (Exception e){
            var ex = new RuntimeException("Error switching to other map!");
            GameLogger.logException("Teleport Event", ex);
            throw ex;
        }
        object.setPosition(x, y, z);
        PhysicSystem.update(world.getGameState().getWorld());
        return true;
    }

    @Override
    public boolean isFinished(World world) {
        if(cancel)
            return true;
        return switch (transitionType){
            case BLACK, WHITE -> processedTime > startTime + endTime;
            case CUSTOM -> customTransitionEvent.isFinished(world);
            default -> processedTime > 0;
        };
    }

    @Override
    public void finish(World world) {
        if(cancel)
            return;
        if(transitionType == TransitionType.CUSTOM)
            customTransitionEvent.finish(world);
    }

    @Override
    public byte getBlockingType() {
        return BLOCK_GLOBAL_UPDATE;
    }
}
