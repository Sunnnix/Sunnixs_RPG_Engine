package de.sunnix.aje.engine.ecs.data;

import de.sunnix.aje.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;

public class EmptyData extends Data<Object> {


    public EmptyData() {
        super("null", () -> null);
    }

    @Override
    public Object get(GameObject go) {
        return null;
    }

    @Override
    public Object set(GameObject go, Object data) {
        return data;
    }

    @Override
    public void save(GameObject go, DataSaveObject dso) {}

    @Override
    public void load(GameObject go, DataSaveObject dso) {}
}
