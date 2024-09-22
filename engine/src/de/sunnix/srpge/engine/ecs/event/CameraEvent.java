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
    protected int objectID;
    protected boolean moveCamera;
    protected float x;
    protected float y;
    protected float z;

    private GameObject object;

    public CameraEvent() {
        super("camera");
    }

    @Override
    public void load(DataSaveObject dso) {
        attachObject = dso.getBool("attack_obj", false);
        objectID = dso.getInt("object", 0);
        moveCamera = dso.getBool("move_cam", false);
        var pos = dso.getFloatArray("pos", 3);
        x = pos[0];
        y = pos[1];
        z = pos[2];
    }

    @Override
    public void prepare(World world) {
        if(attachObject)
            object = world.getGameObject(objectID);
    }

    @Override
    public void run(World world) {
        Camera.setAttachObject(attachObject);
        if(attachObject)
            Camera.setAttachedObject(object);
        if(moveCamera)
            Camera.setPositionTo(x, y, z);
    }

    @Override
    public boolean isFinished(World world) {
        return true;
    }
}
