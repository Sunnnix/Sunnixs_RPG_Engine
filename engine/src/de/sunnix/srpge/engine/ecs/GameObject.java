package de.sunnix.srpge.engine.ecs;

import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.components.Component;
import de.sunnix.srpge.engine.ecs.components.ComponentRegistry;
import de.sunnix.srpge.engine.ecs.event.Event;
import de.sunnix.srpge.engine.ecs.event.EventList;
import de.sunnix.srpge.engine.ecs.event.EventRegistry;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class GameObject extends MemoryHolder {

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    private final HashSet<State> states = new HashSet<>();
    @Getter
    private boolean statesChanged;

    @Getter
    private final long ID; // TODO prevent duplicates by World impl.

    @Getter
    private Vector3f position = new Vector3f();
    @Getter
    private Vector3f velocity = new Vector3f();
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

    @Getter
    private boolean inited;
    /**
     * If marked as deleted, this component will be removed at the end of the loop
     */
    @Getter
    private boolean toDelete = false;

    @Getter
    private List<EventList> eventLists = new ArrayList<>();

    private final List<TrippleConsumer<Vector3f, Vector3f, GameObject>> positionSubscribers = new ArrayList<>();
    private final List<Consumer<GameObject>> markDirtySubscribers = new ArrayList<>();

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

//        eventLists.addAll(dso.getObject("events").<DataSaveObject>getList("list").stream().map(eDSO -> EventRegistry.createEvent(eDSO.getString("ID", null), eDSO)).toList());

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
        if(!inited) {
            GameLogger.logI("GameObject", "Object " + name + " updated without initialisation!");
            return;
        }
        handleEvents(world);
//        position.add(velocity);
//        velocity.set(0);
//        position.y = Math.max(position.y, 0);
//        if(position.y > 0)
//            addState(States.FALLING.id());
//        else
//            removeState(States.FALLING.id());
    }

    public void postRender(){
        statesChanged = false;
    }

    private int currentEvent = -1;

    private void handleEvents(World world) {
        if(eventLists.isEmpty())
            return;
        for(var el: eventLists){
            if(el.getRunType() == EventList.RUN_TYPE_AUTO)
                el.run(world);
        }
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

    public void setToDelete() {
        this.toDelete = true;
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
}
