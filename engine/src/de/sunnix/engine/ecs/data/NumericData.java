package de.sunnix.engine.ecs.data;

public abstract class NumericData<T extends Number> extends Data<T>{

    public NumericData(String key, T defNumber) {
        super(key, () -> defNumber);
    }

}
