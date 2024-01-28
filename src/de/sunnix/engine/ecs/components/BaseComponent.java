package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseComponent extends MemoryHolder {

    private static final Map<Long, Class<? extends BaseComponent>> registeredComponents = new HashMap<>();

    protected GameObject parent;

    public BaseComponent(GameObject parent){
        ComponentManager.addComponent(this);
        this.parent = parent;
    }

    public abstract void init();

    /**
     * register components to make them loadable from game-file
     * @param packageName where to register components
     * @return registered component types
     */
    public static int registerComponents(String packageName){
        try(var reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"))))){
            var classes = reader.lines().filter(line -> line.endsWith(".class")).map(line -> {
                try {
                    return Class.forName(packageName + "." + line.substring(0, line.lastIndexOf('.')));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).filter(c -> c.isAnnotationPresent(Component.class)).toList();
            for(var clazz : classes){
                if(!BaseComponent.class.isAssignableFrom(clazz))
                    throw new RuntimeException("The class " + clazz.getName() + " don't extends " + BaseComponent.class.getName());
            }
            classes.forEach(c -> {
                var id = c.getAnnotation(Component.class).id();
                if(registeredComponents.containsKey(id))
                    throw new RuntimeException("id " + id + " from " + c.getName() + " already taken from " + registeredComponents.get(id).getName());
                registeredComponents.put(id, (Class<? extends BaseComponent>) c);
            });
            return classes.size();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValid(){
        return true;
    }

    @Override
    protected MemoryCategory getMemoryCategory(){
        return MemoryCategory.COMPONENT;
    }

    @Override
    protected String getMemoryInfo() {
        return getClass().getName();
    }

    @Override
    protected void free() {}

}
