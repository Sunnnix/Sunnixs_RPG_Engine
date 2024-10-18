package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.evaluation.Variables;

public class ChangeVariableEvent extends Event{

    protected enum Array {
        INT, FLOAT, BOOL
    }

    protected enum Operation {
        SET, INC, DEC
    }

    protected Array array = Array.INT;
    protected int index;
    protected Number value = 0;
    protected Operation operation = Operation.SET;

    /**
     * Constructs a new event with the specified ID.
     */
    public ChangeVariableEvent() {
        super("change_var");
    }

    @Override
    public void load(DataSaveObject dso) {
        array = Array.values()[dso.getByte("array", (byte) Array.INT.ordinal())];
        index = dso.getInt("index", 0);
        value = dso.getDouble("value", 0);
        operation = Operation.values()[dso.getByte("op", (byte) Operation.SET.ordinal())];
    }

    @Override
    public void prepare(World world, GameObject parent) {}

    @Override
    public void run(World world) {
        switch (array){
            case INT -> {
                switch (operation){
                    case SET -> Variables.setIntVar(index, value.intValue());
                    case INC -> Variables.setIntVar(index, Variables.getInt(index) + value.intValue());
                    case DEC -> Variables.setIntVar(index, Variables.getInt(index) - value.intValue());
                }
            }
            case FLOAT -> {
                switch (operation){
                    case SET -> Variables.setFloatVar(index, value.floatValue());
                    case INC -> Variables.setFloatVar(index, Variables.getFloat(index) + value.floatValue());
                    case DEC -> Variables.setFloatVar(index, Variables.getFloat(index) - value.floatValue());
                }
            }
            case BOOL -> {
                if(operation == Operation.SET)
                    Variables.setBoolVar(index, value.intValue() != 0);
                else
                    Variables.setBoolVar(index, !Variables.getBool(index));
            }
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
