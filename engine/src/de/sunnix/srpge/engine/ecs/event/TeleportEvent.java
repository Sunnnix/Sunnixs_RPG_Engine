package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.Direction;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.stage.GameplayState;
import org.joml.Vector4f;

import static de.sunnix.srpge.engine.util.FunctionUtils.mix;

/**
 * The TeleportEvent class is responsible for handling teleportation events in the game.<br>
 * It supports teleportation to different locations within the current map or to another map entirely for the <font color="#F66">player only</font>.<br>
 * Additionally, it can apply visual transition effects during the teleportation process, such as
 * fading to black, white, or using a custom color transition <font color="#F66">(Not implemented yet)</font>.
 */
public class TeleportEvent extends Event{

    /**
     * TransitionType defines the possible types of transitions during teleportation.
     * <ul>
     *  <li><b>NONE:</b> No transition effect.</li>
     *  <li><b>BLACK:</b> Fade the screen to black and back.</li>
     *  <li><b>WHITE:</b> Fade the screen to white and back.</li>
     *  <li><b>CUSTOM:</b> A custom transition defined by the GlobalColorTintEvent. <font color="#F66">(Not implemented yet)</font></li>
     * </ul>
     */
    public enum TransitionType {
        NONE, BLACK, WHITE, CUSTOM
    }

    /** Map ID to teleport to. If map is -1, no map switch occurs, and only position will be updated. */
    protected int map = -1;
    /** The ID of the object (e.g., player) that will be teleported. */
    private ObjectValue objectID;
    /** The other object id if {@link #toObject} is true */
    private ObjectValue otherID;
    /** Should this object teleport to {@link #otherID another object}<br>If so, the coords ({@link #x}, {@link #y}, {@link #z}) would be used relative to the object */
    protected boolean toObject;
    /** Static position if {@link #toObject} is false, otherwise relative to {@link #otherID other Object} */
    protected float x, y, z;
    /**
     * Type of transition to apply during teleportation.
     */
    protected TransitionType transitionType;
    /**
     * The duration of the transition in frames. This defines how long the visual effect lasts.
     */
    protected int transitionTime;
    /** Custom transition event for handling more complex color transitions, if transitionType is CUSTOM. <font color="#F66">(Not implemented yet)</font> */
    protected GlobalColorTintEvent customTransitionEvent;

    /** Determines if the fading in (clearing the color) after the map switch happened should occur */
    protected boolean fadeIn = true;

    /** In which direction should the object look after the teleport */
    protected Direction facing;

    // Internal attributes used during the teleportation process
    private GameObject object, otherObject;
    private int startTime, endTime;
    private int processedTime;
    private Vector4f initialColor;
    private boolean cancel;
    private Thread mapLoader;

    /**
     * Constructor initializes a new TeleportEvent with the event name "teleport".
     */
    public TeleportEvent() {
        super("teleport");
        blockingType = EventList.BlockType.UPDATE;
    }

    @Override
    public void load(DataSaveObject dso) {
        map = dso.getInt("map", -1);
        objectID = new ObjectValue(dso.getObject("obj"));
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
        fadeIn = dso.getBool("fade_in", true);
        var fID = dso.getByte("facing", (byte) -1);
        facing = fID == -1 ? null : Direction.values()[fID];
        otherID = new ObjectValue(dso.getObject("other"));
        toObject = dso.getBool("to_other", false);
    }

    /**
     * Prepares the teleportation event by retrieving the game object to teleport and setting up the transition effect.<br>
     * If the target map does not exist or if the object cannot be teleported to another map (e.g., not the player), the event is canceled.
     *
     * @param world  the current game world.
     * @param parent
     */
    @Override
    public void prepare(World world, GameObject parent) {
        object = objectID.getObject(world, parent);
        if(toObject)
            otherObject = otherID.getObject(world, parent);
        if(object == null || toObject && otherObject == null || (map != -1 && map != world.ID && object.getID() != 999)) {
            cancel = true;
            return;
        }
        // Set up transition times
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
    }

    /**
     * Executes the teleportation process and applies the transition effect over time.<br>
     * Depending on the {@link #transitionType transition type}, the screen will fade to black, white, or apply a custom effect.
     *
     * @param world the current game world.
     */
    @Override
    public void run(World world) {
        if(cancel)
            return;
        switch (transitionType){
            case BLACK, WHITE -> {
                // First phase: fade to black or white
                if(processedTime < startTime) {
                    processedTime++;
                    float progress = Math.min((float) processedTime / startTime, 1.0f);
                    var c = transitionType == TransitionType.BLACK ? -1 : 1;
                    Core.getGlobalColoring().set(mix(initialColor, new Vector4f(c, c, c, 1), progress));
                }
                // Teleport once the fade-out is complete and the map is loaded
                else if(processedTime == startTime){
                    if(teleport(world)) {
                        processedTime++;
                    }
                }
                // Second phase: fade back in
                else if(fadeIn) {
                    processedTime++;
                    float progress = Math.min((float) (processedTime - startTime - 1) / endTime, 1.0f);
                    Core.getGlobalColoring().set(mix(initialColor, new Vector4f(0), progress));
                }
                // skip fade in
                else {
                    processedTime += endTime;
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

    /**
     * Handles the actual teleportation by setting the object's position and switching maps if necessary.
     *
     * @param world the current game world.
     * @return true if the teleportation is successful, false if the map is still loading.
     */
    private boolean teleport(World world){
        var curFacing = object.getFacing();
        if(map != -1 && map != world.ID) {
            if (mapLoader.isAlive())
                return false; // Wait for the map to load
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
        if(toObject){
            var oPos = otherObject.getPosition();
            object.setPosition(oPos.x + x, Math.max(0, oPos.y + y), oPos.z + z);
        } else
            object.setPosition(x, y, z);
        object.setFacing(facing == null ? curFacing : facing);
        if(object.getID() == 999 && Camera.getAttachedObject() != null && Camera.getAttachedObject().getID() == 999)
            Camera.setAttachedObject(object, true);
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
    public boolean isInstant(World world) {
        return cancel || transitionType == TransitionType.NONE && map == -1;
    }
}
