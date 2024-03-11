package de.sunnix.engine.ecs.data;

import de.sunnix.engine.ecs.GameObject;

public class IntData extends NumericData<Integer> {

    public IntData(String key, int defNumber) {
        super(key, defNumber);
    }

    @Override
    public Integer get(GameObject go) {
        return go.<Number>getData(key).intValue();
    }
}
