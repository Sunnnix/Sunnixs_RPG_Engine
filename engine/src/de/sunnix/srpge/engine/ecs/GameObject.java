package de.sunnix.srpge.engine.ecs;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.components.Component;
import de.sunnix.srpge.engine.ecs.components.ComponentRegistry;
import de.sunnix.srpge.engine.ecs.event.EventList;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

/**
 * The Entity to hold all the data for the objects in the game
 * @see World
 * @see EventList
 * @see de.sunnix.srpge.engine.ecs.event.Event Event
 */
public class GameObject extends MemoryHolder implements Cloneable{

    public static final int localVarCount = 4;

    private Map<Class<? extends Component>, Component> components = new HashMap<>();

    private HashSet<State> states = new HashSet<>();
    @Getter
    private boolean statesChanged;

    @Getter
    private final long ID; // TODO prevent duplicates by World impl.

    @Getter
    private Vector3f position = new Vector3f();
    @Getter
    private Vector3f velocity = new Vector3f();
    @Getter
    public Vector2f size = new Vector2f();
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private float z_pos; // graphical Z-Buffer
    @Getter
    @Setter
    private float z_pos_offset;

    @Getter
    private boolean inited;

    @Getter
    private List<EventList> eventLists = new ArrayList<>();

    private final List<TrippleConsumer<Vector3f, Vector3f, GameObject>> positionSubscribers = new ArrayList<>();
    private final List<Consumer<GameObject>> markDirtySubscribers = new ArrayList<>();

    @Getter
    private Direction facing = Direction.SOUTH;

    private final Set<Byte> startEvents = new HashSet<>();
    private final int[] localVars = new int[localVarCount];
    /** if false, this component does not update and render. */
    @Getter
    private boolean enabled = true;

    private GameObject(World world, long id){
        this.ID = id;
        world.addEntity(this);
    }

    public GameObject(World world, float width, float height){
        this(world, (long)((Math.random() * Long.MAX_VALUE * 2) - Long.MAX_VALUE));
        this.name = "Entity " + Long.toHexString(ID);
        this.size.set(width, height);
    }

    public GameObject(World world, DataSaveObject dso){
        this(world, dso.getInt("ID", -1));
        this.name = dso.getString("name", null);
        this.size.set(dso.getFloat("width", 0), dso.getFloat("height", 0));
        this.setPosition(dso.getFloat("x", 0), dso.getFloat("y", 0), dso.getFloat("z", 0));
        this.facing = Direction.values()[dso.getByte("facing", (byte) Direction.SOUTH.ordinal())];
        enabled = dso.getBool("enabled", true);

        eventLists.addAll(dso.<DataSaveObject>getList("event_lists").stream().map(EventList::new).toList());

        dso.<DataSaveObject>getList("components").forEach(x -> addComponent(ComponentRegistry.build(x)));
    }

    public GameObject init(World world){
        if(!inited) {
            this.components.values().forEach(c -> c.init(world, this));
            this.inited = true;
            markDirty();
        }
        return this;
    }

    public final <T extends Component> T getComponent(Class<T> componentType){
        return componentType.cast(components.get(componentType));
    }

    public final <T extends Component> T addComponent(T component){
        if(inited){
            GameLogger.logW("GameObject", "Can't add component to initialized GameObject");
            return component;
        }
        components.put(component.getClass(), component);
        return component;
    }

    public void update(World world){
        if(!enabled)
            return;
        if(!inited) {
            GameLogger.logI("GameObject", "Object " + name + " updated without initialisation!");
            return;
        }
        handleEvents(world);
    }

    public void postRender(){
        statesChanged = false;
    }

    private void handleEvents(World world) {
        if(eventLists.isEmpty())
            return;
        for(var el: eventLists){
            if(el.canStart(world) && (el.getRunType() == EventList.RUN_TYPE_AUTO || startEvents.stream().anyMatch(rt -> rt == el.getRunType())))
                world.getGameState().startEventList(el, this);
        }
        startEvents.clear();
    }

    public void addState(String id){
        addState(id, true);
    }

    public void addState(String id, boolean trackChange){
        var state = States.getState(id);
        states.add(state);
        if(trackChange) {
            statesChanged = true;
            markDirty();
        }
    }

    public void removeState(String id){
        removeState(id, true);
    }

    public void removeState(String id, boolean trackChange){
        var state = states.remove(States.getState(id));
        if(trackChange) {
            statesChanged = true;
            markDirty();
        }
    }

    public void setPosition(float x, float y, float z){
        var newPos = new Vector3f(x, y, z);
        positionSubscribers.forEach(c -> c.accept(position, newPos, this));
        position.set(newPos);
        markDirty();
    }

    public void addPosition(float x, float y, float z){
        setPosition(position.x + x, position.y + y, position.z + z);
    }

    public void addMarkDirtySubscriber(Consumer<GameObject> subscriber){
        markDirtySubscribers.add(subscriber);
    }

    public void markDirty(){
        for(var markDirtySubscriber: markDirtySubscribers)
            markDirtySubscriber.accept(this);
    }

    public void addPositionSubscriber(TrippleConsumer<Vector3f, Vector3f, GameObject> subscriber){
        positionSubscribers.add(subscriber);
    }

    public Collection<State> getStates(){
        return states.stream().toList();
    }

    public boolean hasState(State state) {
        return states.contains(state);
    }

    public boolean hasState(String id){
        return hasState(States.getState(id));
    }

    public boolean containsEventType(byte runType) {
        return eventLists.stream().anyMatch(el -> el.getRunType() == runType);
    }

    public void startEvent(byte type) {
        startEvents.add(type);
    }

    public int getVariable(int index){
        if(index < 0 || index >= localVarCount)
            return 0;
        return localVars[index];
    }

    public void setVariable(int index, int value){
        if(index < 0 || index >= localVarCount)
            return;
        localVars[index] = value;
    }

    public void runInitEvents(World world){
        for(var el: eventLists){
            if(el.getRunType() == EventList.RUN_TYPE_INIT)
                if(el.canStart(world)) {
                    el.start(this);
                    while (el.isActive())
                        el.run(world);
                }
        }
    }

    public void setEnabled(boolean enabled){
        if(!this.enabled && enabled)
            markDirty();
        this.enabled = enabled;
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

    public interface TrippleConsumer<T, J, K> {

        void accept(T t, J j, K k);

    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        markDirty();
    }

    private static int copyID = 1000;

    public final GameObject copy(){
        var clone = clone();
        try {
            var id_field = getClass().getDeclaredField("ID");
            id_field.setAccessible(true);
            id_field.set(clone, copyID++);
        } catch (Exception e){
            throw new RuntimeException("Exception coping object!", e);
        }
        return clone;
    }

    @Override
    protected GameObject clone() {
        try {
            var clone = (GameObject) super.clone();
            clone.components = new HashMap<>();
            components.forEach((k, v) -> clone.components.put(k, (Component) v.clone()));
            clone.eventLists = new ArrayList<>();
            eventLists.forEach(el -> clone.eventLists.add(el.clone()));
            clone.position = new Vector3f(position);
            clone.velocity = new Vector3f(velocity);
            clone.size = new Vector2f(size);
            clone.states = new HashSet<>(states);
            positionSubscribers.clear();
            markDirtySubscribers.clear();
            clone.inited = false;
            return clone;
        } catch (Exception e){
            throw new RuntimeException("Exception cloning object!", e);
        }
    }
}
