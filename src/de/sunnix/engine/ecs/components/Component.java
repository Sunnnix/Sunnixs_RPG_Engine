package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.graphics.Texture;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.registry.ISavable;
import de.sunnix.engine.registry.Registry;
import de.sunnix.sdso.DataSaveObject;

public abstract class Component extends MemoryHolder implements ISavable {

    public static RenderComponent RENDER = new RenderComponent();
    protected GameObject parent;

    public static void registerComponents(){
        var registrar = Registry.COMPONENT;
        registrar.register("render", RENDER);
    }

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

    Texture tex;

    @Override
    public void save(DataSaveObject dso) {
        dso.putString("component", Registry.COMPONENT.getRegistryNameOf(this));
        dso.putString("texture", Registry.TEXTURE.getRegistryNameOf(tex));
    }

    @Override
    public void load(DataSaveObject dso) {
        tex = Registry.TEXTURE.get(dso.getString("texture", null));
    }
}
