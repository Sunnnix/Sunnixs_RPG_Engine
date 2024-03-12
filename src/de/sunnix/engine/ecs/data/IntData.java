package de.sunnix.engine.ecs.data;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;

public class IntData extends NumericData<Integer> {

    public IntData(String key, int defNumber) {
        super(key, defNumber);
    }

    @Override
    public Integer get(GameObject go) {
        return go.<Number>getData(key).intValue();
    }

    @Override
    public void save(GameObject go, DataSaveObject dso) {
        dso.putInt(key, get(go));
    }

    @Override
    public void load(GameObject go, DataSaveObject dso) {
        set(go, dso.getInt(key, generator.get()));
    }
}
