package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;

public class NumberVariableProvider extends ValueProvider<Number>{

    protected enum Array {
        INT, FLOAT
    }

    protected Array array = Array.INT;
    protected int index;

    public NumberVariableProvider() {
        super("num_var", 0);
    }

    @Override
    public Number getValue(World world) {
        return switch (array){
            case INT -> Variables.getInt(index, defaultValue.intValue());
            case FLOAT -> Variables.getFloat(index, defaultValue.floatValue());
        };
    }

    @Override
    public void load(DataSaveObject dso) {
        array = Array.values()[dso.getByte("array", (byte) Array.INT.ordinal())];
        index = dso.getInt("index", 0);
    }

}
