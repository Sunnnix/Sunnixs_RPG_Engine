package de.sunnix.srpge.engine.ecs.data;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;

public class StringData extends Data<String>{
    public StringData(String key, String defValue) {
        super(key, () -> defValue);
    }

    @Override
    public void save(GameObject go, DataSaveObject dso) {
        dso.putString(key, get(go));
    }

    @Override
    public void load(GameObject go, DataSaveObject dso) {
        set(go, dso.getString(key, generator.get()));
    }
}
