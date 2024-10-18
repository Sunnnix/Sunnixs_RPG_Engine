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
    protected ObjectValue objectID;
    /** The ID of the target object that this object will face if staticLook is false. */
    protected ObjectValue lookAtObjID;

    private GameObject obj, lookAt;

    public LookEvent() {
        super("look");
    }

    @Override
    public void load(DataSaveObject dso) {
        staticLook = dso.getBool("static_look", true);
        objectID = new ObjectValue(dso.getObject("obj"));
        if(!staticLook)
            lookAtObjID = new ObjectValue(dso.getObject("look_at_obj"));
        else
            direction = Direction.values()[dso.getByte("dir", (byte) Direction.SOUTH.ordinal())];
    }

    @Override
    public void prepare(World world, GameObject parent) {
        obj = objectID.getObject(world, parent);
        if(!staticLook)
            lookAt = lookAtObjID.getObject(world, parent);
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
                obj.setFacing(xDiff < 0 ? Direction.EAST : Direction.WEST);
            else
                obj.setFacing(zDiff > 0 ? Direction.NORTH : Direction.SOUTH);
        }

    }

    @Override
    public boolean isFinished(World world) {
        return true;
    }

    @Override
    public boolean isInstant(World world) {
        return true;
    }
}
