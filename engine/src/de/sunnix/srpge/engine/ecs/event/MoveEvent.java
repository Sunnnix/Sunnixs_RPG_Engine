package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.Direction;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.util.ObjChain;

import static de.sunnix.srpge.engine.ecs.Direction.*;

/**
 * The MoveEvent class represents an event that moves a game object in a specified direction
 * within the world. The movement is based on the given x, y, and z distances, along with a specified speed.
 * The event will keep running until the target position is reached, or it is cancelled.
 */
public class MoveEvent extends Event{

    /** Enum representing the different movement handling strategies when a blockage occurs during movement. */
    public enum MoveEventHandle {
        /** Do nothing special. Continue the movement as usual. */
        NONE,
        /** Cancel the movement completely if the object is blocked. */
        CANCEL_MOVEMENT,
        /** Wait for the blockage to clear, maintaining the remaining path for completion. */
        WAIT_FOR_COMPLETION;
    }

    /** The object ID of the object that is being moved. */
    protected ObjectValue object;
    /** The amount to move along an axis. */
    protected float movX, movY, movZ;
    /** Should the Object jump */
    protected boolean jump;
    /** The speed of the movement. */
    protected float speed = .035f;
    /** Specifies how to handle movement when the object encounters a blockage. */
    protected MoveEventHandle onBlockHandle = MoveEventHandle.NONE;

    /**
     * The current position of the object.<br>
     * Used to determine if the object has moved in cases where {@link MoveEventHandle#WAIT_FOR_COMPLETION WAIT_FOR_COMPLETION} is active.
     */
    private float cPosX, cPosY, cPosZ;
    /** The remaining distance that needs to be moved along each axis. */
    private float rMovX, rMovY, rMovZ;
    private GameObject obj;
    private boolean firstRun;

    public MoveEvent() {
        super("move");
    }

    @Override
    public void load(DataSaveObject dso) {
        object = new ObjectValue(dso.getObject("obj"));
        movX = dso.getFloat("x", 0);
        movY = dso.getFloat("y", 0);
        movZ = dso.getFloat("z", 0);
        speed = dso.getFloat("s", .035f);
        onBlockHandle = MoveEventHandle.values()[dso.getByte("handle", (byte) MoveEventHandle.NONE.ordinal())];
        parallel = dso.getBool("parallel", false);
        jump = dso.getBool("jump", false);
    }

    /**
     * Prepares the {@link MoveEvent} by setting up the initial positions and remaining movement distances.
     */
    @Override
    public void prepare(World world, GameObject parent) {
        obj = object.getObject(world, parent);
        if(obj == null) {
            rMovX = 0;
            rMovY = 0;
            rMovZ = 0;
            return;
        }
        obj.addState(States.MOVING.id());
        rMovX = movX;
        rMovY = movY;
        rMovZ = movZ;
        var pos = obj.getPosition();
        cPosX = pos.x;
        cPosY = pos.y;
        cPosZ = pos.z;
        var comp = obj.getComponent(PhysicComponent.class);
        if(comp != null){
            if(jump)
                comp.jump();
        }
        firstRun = true;
    }

    /**
     * Executes the movement by adjusting the object's velocity and updating the remaining distance to be moved.
     * Handles movement restrictions or cancellations based on the {@link #onBlockHandle} value.
     */
    @Override
    public void run(World world) {
        if (obj == null)
            return;

        float xFactor, zFactor;
        zFactor = Math.min(1, rMovX == 0 ? 1 : Math.abs(rMovZ) / Math.abs(rMovX));
        xFactor = Math.min(1, rMovZ == 0 ? 1 : Math.abs(rMovX) / Math.abs(rMovZ));

        var pPosX = cPosX;
        var pPosY = cPosY;
        var pPosZ = cPosZ;

        var pos = obj.getPosition();
        cPosX = pos.x;
        cPosY = pos.y;
        cPosZ = pos.z;

        if(!firstRun) {
            if (onBlockHandle == MoveEventHandle.WAIT_FOR_COMPLETION) {
                if (movX != 0)
                    rMovX += pPosX - cPosX;
                if (movY != 0)
                    rMovY += pPosY - cPosY;
                if (movZ != 0)
                    rMovZ += pPosZ - cPosZ;
            }
            if (onBlockHandle == MoveEventHandle.CANCEL_MOVEMENT &&
                    new ObjChain<>(obj.getComponent(PhysicComponent.class)).next(PhysicComponent::isMovementBlocked).orElse(false)) {
                rMovX = 0;
                rMovY = 0;
                rMovZ = 0;
                return;
            }
        }

        // Calculate the velocities based on remaining distances and speed
        float[] velocities = calculateVelocity(rMovX, rMovY, rMovZ, speed, xFactor, zFactor);
        float velX = velocities[0], velY = velocities[1], velZ = velocities[2];

        obj.getVelocity().add(velX, velY, velZ);
        obj.addState(States.MOVING.id());

        new ObjChain<>(obj.getComponent(RenderComponent.class)).ifPresent(r -> obj.setFacing(determineDirection(velX, velZ)));

        // Update remaining distances if not waiting for completion
        if(onBlockHandle != MoveEventHandle.WAIT_FOR_COMPLETION) {
            rMovX = updateRemainingPosition(rMovX, speed * xFactor);
            rMovY = updateRemainingPosition(rMovY, speed);
            rMovZ = updateRemainingPosition(rMovZ, speed * zFactor);
        }

        if(obj.getComponent(PhysicComponent.class) == null){
            obj.getVelocity().sub(velX, velY, velZ);
            obj.addPosition(velX, velY, velZ);
        }

        firstRun = false;
    }

    /**
     * Calculates the velocity for each axis based on the remaining movement and speed.
     *
     * @param rMovX   the remaining distance along the X-axis
     * @param rMovY   the remaining distance along the Y-axis
     * @param rMovZ   the remaining distance along the Z-axis
     * @param speed   the speed of the movement
     * @param xFactor
     * @param zFactor
     * @return an array containing the calculated velocities for each axis
     */
    private float[] calculateVelocity(float rMovX, float rMovY, float rMovZ, float speed, float xFactor, float zFactor) {
        return new float[]{
                rMovX < 0 ? Math.max(rMovX, -speed * xFactor) : Math.min(rMovX, speed * xFactor),
                rMovY < 0 ? Math.max(rMovY, -speed) : Math.min(rMovY, speed),
                rMovZ < 0 ? Math.max(rMovZ, -speed * zFactor) : Math.min(rMovZ, speed * zFactor)
        };
    }

    /**
     * Determines the facing direction of the object based on its velocity.
     *
     * @param velX the velocity along the X-axis
     * @param velZ the velocity along the Z-axis
     * @return the direction the object should be facing
     */
    private Direction determineDirection(float velX, float velZ) {
        if (Math.abs(velX) > Math.abs(velZ)) {
            return velX > 0 ? EAST : WEST;
        } else if (velZ != 0) {
            return velZ > 0 ? SOUTH : NORTH;
        }
        return SOUTH;
    }

    /**
     * Updates the remaining position to be moved along a specific axis.
     *
     * @param rMov the remaining distance along the axis
     * @param speed the speed of the movement
     * @return the updated remaining distance along the axis
     */
    private float updateRemainingPosition(float rMov, float speed) {
        return rMov < 0 ? rMov - Math.max(rMov, -speed) : rMov - Math.min(rMov, speed);
    }

    @Override
    public boolean isFinished(World world) {
        return rMovX == 0 && rMovY == 0 && rMovZ == 0;
    }

    @Override
    public void finish(World world) {
        if(obj == null)
            return;
        obj.removeState(States.MOVING.id());
    }

}
