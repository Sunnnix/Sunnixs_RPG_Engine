package de.sunnix.engine.ecs.data;

import de.sunnix.engine.debug.GameLogger;
import de.sunnix.engine.ecs.GameData;
import de.sunnix.engine.ecs.GameObject;
import de.sunnix.sdso.DataSaveObject;

import java.util.Map;
import java.util.function.Supplier;

public abstract class Data<T> {

    public final String key;
    public final Supplier<T> generator;

    public Data(String key, Supplier<T> generator){
        this.key = key;
        this.generator = generator;
    }

    public void init(GameObject go) {
        set(go, generator.get());
    }

    @SuppressWarnings("unchecked")
    public T get(GameObject go){
        return go.getData(key);
    }

    public T set(GameObject go, T data){
        return go.setData(key, data);
    }

    @SuppressWarnings("unchecked")
    public static <T> void registerDataToMap(Class<T> clazz, Map<String, Data<Object>> map){
        var fields = clazz.getFields();
        for(var field : fields){
            try {
                if(field.isAnnotationPresent(GameData.class) && Data.class.isAssignableFrom(field.getType()))
                    map.put(field.getAnnotation(GameData.class).key(), (Data<Object>) field.get(null));
            } catch (Exception e){
                GameLogger.logException("Data init", e);
            }
        }
    }

    public abstract void save(GameObject go, DataSaveObject dso);

    public abstract void load(GameObject go, DataSaveObject dso);

}
