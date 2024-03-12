package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.ecs.data.Data;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.registry.Registry;
import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Component extends MemoryHolder {

    public static RenderComponent RENDER = new RenderComponent();

    private final Map<String, Data<Object>> dataHolder = new HashMap<>();
    protected GameObject parent;

    private boolean firstInit = true;

    public static void registerComponents(){
        var registrar = Registry.COMPONENT;
        registrar.register("render", RENDER);
    }

    public void init(GameObject parent){
        this.parent = parent;
        if(firstInit){
            firstInit = false;
            Data.registerDataToMap(getClass(), dataHolder);
        }
        ComponentManager.addComponent(this);
        dataHolder.forEach((k, d) -> d.init(parent));
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

    public void save(GameObject go, DataSaveObject dso) {
        dataHolder.values().forEach(data -> data.save(go, dso));
    }

    public void load(GameObject go, DataSaveObject dso) {
        dataHolder.values().forEach(data -> data.load(go, dso));
    }
}
