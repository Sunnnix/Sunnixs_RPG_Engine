package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ComponentRegistry {

    private static Map<String, Function<DataSaveObject, Component>> components = new HashMap<>();

    public static void add(String id, Function<DataSaveObject, Component> componentBuilder){
        components.put(id, componentBuilder);
    }

    public static Component build(DataSaveObject dso){
        var builder = components.get(dso.getString("id", null));
        if(builder == null)
            return NULLComponent.getNULL();
        return builder.apply(dso);
    }

}
