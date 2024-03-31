package de.sunnix.engine.ecs.data;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class Vector3Data extends Data<Vector3f> {

    public Vector3Data(String key, Supplier<Vector3f> generator) {
        super(key, generator);
    }

    @Override
    public void save(GameObject go, DataSaveObject dso) {
        var vec = get(go);
        dso.putArray(key, new float[]{ vec.x, vec.y, vec.z });
    }

    @Override
    public void load(GameObject go, DataSaveObject dso) {
        set(go, new Vector3f().set(dso.getFloatArray(key, 3)));
    }

}
