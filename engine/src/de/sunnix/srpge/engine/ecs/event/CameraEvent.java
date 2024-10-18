package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.graphics.Camera;

/**
 * Handles Camera properties<br>
 * Can be set to:
 * <ul>
 *     <li>not move</li>
 *     <li>move towards an object</li>
 *     <li>set to a static position</li>
 * </ul>
 */
public class CameraEvent extends Event{

    protected boolean attachObject;
    protected ObjectValue objectID;
    protected boolean moveCamera;
    protected float x;
    protected float y;
    protected float z;

    protected boolean instant;

    private GameObject object;

    public CameraEvent() {
        super("camera");
    }

    @Override
    public void load(DataSaveObject dso) {
        attachObject = dso.getBool("attack_obj", false);
        objectID = new ObjectValue(dso.getObject("obj"));
        moveCamera = dso.getBool("move_cam", false);
        var pos = dso.getFloatArray("pos", 3);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        instant = dso.getBool("instant", false);
    }

    @Override
    public void prepare(World world, GameObject parent) {
        if(attachObject)
            object = objectID.getObject(world, parent);
    }

    @Override
    public void run(World world) {
        Camera.setAttachObject(attachObject);
        if(attachObject)
            Camera.setAttachedObject(object, instant);
        if(moveCamera)
            Camera.setPositionTo(x, y, z, instant);
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
