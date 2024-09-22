package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.Direction;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;

/**
 * The LookEvent class is responsible for changing the facing direction of a {@link GameObject}.<br>
 * This event can either set a static direction or make the object face another target object.
 */
public class LookEvent extends Event {

    /** Determines if the object should look in a static direction. */
    protected boolean staticLook = true;
    /** The direction the object should face if staticLook is true. */
    protected Direction direction = Direction.SOUTH;
    /** The ID of the object that will change its facing direction. */
    protected int objectID;
    /** The ID of the target object that this object will face if staticLook is false. */
    protected int lookAtObjID = -1;

    private GameObject obj, lookAt;

    public LookEvent() {
        super("look");
    }

    @Override
    public void load(DataSaveObject dso) {
        staticLook = dso.getBool("static_look", true);
        objectID = dso.getInt("object", -1);
        if(!staticLook)
            lookAtObjID = dso.getInt("look_at_obj", -1);
        else
            direction = Direction.values()[dso.getByte("dir", (byte) Direction.SOUTH.ordinal())];
    }

    @Override
    public void prepare(World world) {
        obj = world.getGameObject(objectID);
        if(!staticLook)
            lookAt = world.getGameObject(lookAtObjID);
    }

    /**
     * Executes the event by setting the facing direction of the object.<br>
     *
     * If {@code staticLook} is true, the object will face a fixed direction.
     * Otherwise, the object will face the target object based on their relative positions.
     */
    @Override
    public void run(World world) {
        if(obj == null || !staticLook && lookAt == null)
            return;
        if(staticLook)
            obj.setFacing(direction);
        else {
            var objPos = obj.getPosition();
            var lookPos = lookAt.getPosition();
            var xDiff = objPos.x - lookPos.x;
            var zDiff = objPos.z - lookPos.z;
            var horizontal = Math.abs(xDiff) > Math.abs(zDiff);

            if (horizontal)
                obj.setFacing(xDiff < 0 ? Direction.WEST : Direction.EAST);
            else
                obj.setFacing(zDiff > 0 ? Direction.NORTH : Direction.SOUTH);
        }

    }

    @Override
    public boolean isFinished(World world) {
        return true;
    }
}
