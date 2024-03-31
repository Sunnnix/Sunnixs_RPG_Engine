package de.sunnix.aje.engine.ecs;

import de.sunnix.aje.engine.Core;
import de.sunnix.aje.engine.debug.GameLogger;
import de.sunnix.aje.engine.ecs.components.Component;
import de.sunnix.aje.engine.ecs.data.Data;
import de.sunnix.aje.engine.ecs.data.IntData;
import de.sunnix.aje.engine.memory.MemoryCategory;
import de.sunnix.aje.engine.memory.MemoryHolder;
import de.sunnix.aje.engine.stage.GameplayState;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public class GameObject extends MemoryHolder {

    private static final Map<String, Data<Object>> dataHolder = new HashMap<>();

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    @Getter
    private final long ID; // TODO prevent duplicates by World impl.

    private Map<String, Object> data = new HashMap<>();

    @Getter
    private Vector3f position = new Vector3f();
    @Getter
    public final Vector2f size = new Vector2f();
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private float z_pos; // graphical Z-Buffer
    @Getter
    @Setter
    private float z_pos_offset;

    @GameData(key = "test")
    public static final IntData TEST = new IntData("test", 0);

    @Getter
    private boolean inited;
    /**
     * If marked as deleted, this component will be removed at the end of the loop
     */
    @Getter
    private boolean toDelete = false;

    static {
        Data.registerDataToMap(GameObject.class, dataHolder);
    }

    public GameObject(float width, float height){
        this.ID = (long)((Math.random() * Long.MAX_VALUE * 2) - Long.MAX_VALUE);
        this.name = "Entity " + Long.toHexString(ID);
        this.size.set(width, height);
        data.put("test", 20);
        dataHolder.forEach((k, v) -> v.init(this));
        ((GameplayState) Core.GameState.GAMEPLAY.state).getWorld().addEntity(this);
    }

    public GameObject init(){
        if(!inited) {
            this.components.values().forEach(c -> c.init(this));
            this.inited = true;
        }
        return this;
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
        if(inited){
            GameLogger.logW("GameObject", "Can't add component to initialized GameObject");
            return;
        }
        components.put(component.getClass(), component);
    }

    public void update(){
        if(!inited) {
            GameLogger.logI("GameObject", "Object " + name + " updated without initialisation!");
            return;
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
