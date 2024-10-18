package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.ComponentManager;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;

public abstract class Component extends MemoryHolder implements Cloneable{

    protected GameObject parent;

    public Component(DataSaveObject dso){
        load(dso);
    }

    protected void load(DataSaveObject dso){}

    public void init(World world, GameObject parent){
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

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
