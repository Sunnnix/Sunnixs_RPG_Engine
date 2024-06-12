package de.sunnix.srpge.engine.ecs;

import de.sunnix.srpge.engine.ecs.components.Component;

import java.util.*;

public class ComponentManager {

    private static final Map<Class<? extends Component>, List<? extends Component>> components = new HashMap();

    public static <T extends Component> void addComponent(T component){
        var list = components.computeIfAbsent(component.getClass(), k -> new ArrayList<T>());
        ((List<T>)list).add(component);
    }

    public static <T extends Component> void removeComponent(T component){
        var list = components.get(component.getClass());
        if(list == null)
            return;
        list.remove(component);
    }

    public static <T extends Component> List<T> getComponentList(Class<T> classType){
        var list = components.get(classType);
        if(list == null)
            list = Collections.emptyList();
        return (List<T>) list;
    }

}
