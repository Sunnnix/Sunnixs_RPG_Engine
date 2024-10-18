package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;

public class ChangeObjectVariableEvent extends Event {

    protected enum Operation {
        SET, INC, DEC
    }

    protected ObjectValue objectID;
    protected int index;
    protected int value;
    protected Operation operation = Operation.SET;

    private GameObject object;

    /**
     * Constructs a new event with the specified ID.
     */
    public ChangeObjectVariableEvent() {
        super("change_local_var");
    }

    @Override
    public void load(DataSaveObject dso) {
        objectID = new ObjectValue(dso.getObject("obj"));
        index = dso.getInt("index", 0);
        value = dso.getInt("value", 0);
        operation = Operation.values()[dso.getByte("op", (byte) Operation.SET.ordinal())];
    }

    @Override
    public void prepare(World world, GameObject parent) {
        object = objectID.getObject(world, parent);
    }

    @Override
    public void run(World world) {
        if(object == null)
            return;
        switch (operation){
            case SET -> object.setVariable(index, value);
            case INC -> object.setVariable(index, object.getVariable(index) + value);
            case DEC -> object.setVariable(index, object.getVariable(index) - value);
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
