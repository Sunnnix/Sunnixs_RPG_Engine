package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.registry.Registry;

public abstract class Component extends MemoryHolder {

    protected GameObject parent;

    public void init(GameObject parent){
        this.parent = parent;
        ComponentManager.addComponent(this);
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
