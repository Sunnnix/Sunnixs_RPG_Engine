package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;

public class ChangeStateEvent extends Event{

    protected int objectID = -1;
    protected String state;
    protected boolean add;

    private GameObject object;

    /**
     * Constructs a new event with the specified ID.
     */
    public ChangeStateEvent() {
        super("change_state");
    }

    @Override
    public void load(DataSaveObject dso) {
        objectID = dso.getInt("object", -1);
        state = dso.getString("state", null);
        add = dso.getBool("add", false);
    }

    @Override
    public void prepare(World world) {
        object = world.getGameObject(objectID);
    }

    @Override
    public void run(World world) {
        if(object == null)
            return;
        if(add)
            object.addState(state);
        else
            object.removeState(state);
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
