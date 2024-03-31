package de.sunnix.aje.engine.ecs.data;

import de.sunnix.aje.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;

public class FloatData extends NumericData<Float> {

    public FloatData(String key, float defNumber) {
        super(key, defNumber);
    }

    @Override
    public Float get(GameObject go) {
        return go.<Number>getData(key).floatValue();
    }

    @Override
    public void save(GameObject go, DataSaveObject dso) {
        dso.putFloat(key, get(go));
    }

    @Override
    public void load(GameObject go, DataSaveObject dso) {
        set(go, dso.getFloat(key, generator.get()));
    }
}
