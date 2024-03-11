package de.sunnix.engine.ecs;

import de.sunnix.engine.Core;
import de.sunnix.engine.debug.GameLogger;
import de.sunnix.engine.ecs.components.Component;
import de.sunnix.engine.ecs.data.Data;
import de.sunnix.engine.ecs.data.IntData;
import de.sunnix.engine.ecs.data.StringData;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.stage.GameplayState;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.*;

public class GameObject extends MemoryHolder {

    private static Map<String, Data<Object>> dataHolder = new HashMap<>();

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final List<Component> newComponents = new ArrayList<>();

    @Getter
    private final long ID; // TODO prevent duplicates by World impl.

    private Map<String, Object> data = new HashMap<>();

//    public static Data<Vector3f> position = new Data<>("position", Vector3f::new);
    @Getter
    private Vector3f position = new Vector3f();
    @Getter
    @Setter
    private String name;

    @GameData(key = "test")
    public static final IntData TEST = new IntData("test", 0);

    /**
     * If marked as deleted, this component will be removed at the end of the loop
     */
    @Getter
    private boolean toDelete = false;

    static {
        Data.registerDataToMap(GameObject.class, dataHolder);
    }

    public GameObject(){
        this.ID = (long)((Math.random() * Long.MAX_VALUE * 2) - Long.MAX_VALUE);
        this.name = "Entity " + Long.toHexString(ID);
        data.put("test", 20);
        dataHolder.forEach((k, v) -> v.init(this));
        ((GameplayState)Core.GameState.GAMEPLAY.state).getWorld().addEntity(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T setData(String key, T data){
        return (T) this.data.put(key, data);
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
