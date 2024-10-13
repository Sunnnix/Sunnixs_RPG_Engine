package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;

public class ObjectPropertyEvent extends Event {

    protected int objectID = -1;
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
        objectID = dso.getInt("object", -1);
        enabled = dso.getBool("enabled", true);
    }

    @Override
    public void prepare(World world) {
        object = world.getGameObject(objectID);
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
