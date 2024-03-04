package de.sunnix.engine.ecs;

import de.sunnix.engine.Core;
import de.sunnix.engine.ecs.components.Component;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.stage.GameplayState;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameObject extends MemoryHolder {

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final List<Component> newComponents = new ArrayList<>();

    @Getter
    private final long ID; // TODO prevent duplicates by World impl.

    @Getter
    private Vector3f position = new Vector3f();
    @Getter
    @Setter
    private String name;

    /**
     * If marked as deleted, this component will be removed at the end of the loop
     */
    @Getter
    private boolean toDelete = false;

    public GameObject(){
        this.ID = (long)((Math.random() * Long.MAX_VALUE * 2) - Long.MAX_VALUE);
        this.name = "Entity " + Long.toHexString(ID);
        ((GameplayState)Core.GameState.GAMEPLAY.state).getWorld().addEntity(this);
    }

    public final <T extends Component> T getComponent(Class<T> componentType){
        return componentType.cast(components.get(componentType));
    }

    public final <T extends Component> void addComponent(T component){
        components.put(component.getClass(), component);
        newComponents.add(component);
    }

    public final <T extends Component> void removeComponent(Class<T> componentType){
        components.remove(componentType);
    }

    public void update(){
        if(newComponents.size() > 0){
            newComponents.forEach(c -> c.init(this));
            newComponents.clear();
        }
    }

    public void setToDelete() {
        this.toDelete = true;
    }

    @Override
    public boolean isValid() {
        return components.values().stream().allMatch(Component::isValid);
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.ENTITY;
    }

    @Override
    protected String getMemoryInfo() {
        return name;
    }

    @Override
    protected void free() {
        components.values().forEach(Component::freeMemory);
    }
}
