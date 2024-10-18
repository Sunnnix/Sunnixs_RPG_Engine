package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.util.ObjChain;

public class ObjectPropertyEvent extends Event {

    private ObjectValue objectID;
    protected boolean enabled;

    private GameObject object;

    /**
     * Constructs a new event with the specified ID.
     */
    public ObjectPropertyEvent() {
        super("obj_prop");
    }

    @Override
    public void load(DataSaveObject dso) {
        objectID = new ObjectValue(dso.getObject("obj"));
        enabled = dso.getBool("enabled", true);
    }

    @Override
    public void prepare(World world, GameObject parent) {
        object = objectID.getObject(world, parent);
    }

    @Override
    public void run(World world) {
        if(object == null)
            return;
        object.setEnabled(enabled);
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
