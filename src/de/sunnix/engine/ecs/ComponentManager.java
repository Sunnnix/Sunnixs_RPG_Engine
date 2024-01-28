package de.sunnix.engine.ecs;

import de.sunnix.engine.ecs.components.BaseComponent;

import java.util.*;

public class ComponentManager {

    private static final Map<Class<? extends BaseComponent>, List<? extends BaseComponent>> components = new HashMap();

    public static <T extends BaseComponent> void addComponent(T component){
        var list = components.computeIfAbsent(component.getClass(), k -> new ArrayList<T>());
        ((List<T>)list).add(component);
    }

    public static <T extends BaseComponent> void removeComponent(T component){
        var list = components.get(component.getClass());
        if(list == null)
            return;
        list.remove(component);
    }

    public static <T extends BaseComponent> List<T> getComponentList(Class<T> classType){
        var list = components.get(classType);
        if(list == null)
            list = Collections.emptyList();
        return (List<T>) list;
    }

}
