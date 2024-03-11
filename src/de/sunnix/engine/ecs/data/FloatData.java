package de.sunnix.engine.ecs.data;

import de.sunnix.engine.ecs.GameObject;

public class FloatData extends NumericData<Float> {

    public FloatData(String key, float defNumber) {
        super(key, defNumber);
    }

    @Override
    public Float get(GameObject go) {
        return go.<Number>getData(key).floatValue();
    }
}
