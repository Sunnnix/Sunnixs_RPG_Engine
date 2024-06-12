package de.sunnix.srpge.engine.ecs.data;

public abstract class NumericData<T extends Number> extends Data<T>{

    public NumericData(String key, T defNumber) {
        super(key, () -> defNumber);
    }

}
