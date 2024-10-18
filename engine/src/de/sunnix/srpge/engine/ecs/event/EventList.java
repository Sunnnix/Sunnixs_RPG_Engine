package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.evaluation.Condition;
import de.sunnix.srpge.engine.evaluation.EvaluationRegistry;
import de.sunnix.srpge.engine.util.ObjChain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The EventList class manages a list of {@link Event} objects for a
 * {@link GameObject} in the game. Events in the list can be run sequentially,
 * and can optionally block certain game processes (like user input or updates).
 * The list provides functionality to {@link #reset(World) reset}, {@link #run(World) run}, and manage event states.
 */
@Getter
@Setter
public class EventList implements Cloneable{

    /**
     * {@code BlockType} defines how events can block game processes during execution.
     */
    public enum BlockType {
        /** No processes are blocked. */
        NONE,
        /** User input is blocked while the event is running. */
        USER_INPUT,
        /** The world update is blocked while the event is running. */
        UPDATE,
        /** Graphic updates are blocked while the event is running. */
        UPDATE_GRAPHIC
    }

    /** The default run type indicating automatic execution of events. */
    public static final byte RUN_TYPE_AUTO = 0;
    /**
     * This run type is executed once during the initialization of the map.<br>
     * Unlike other run types, using this type will be processed consecutively in a single frame until completion.<br>
     * Disabled objects still execute init event lists
     * <font color="#F66">Be cautious: if an event with this run type contains an endless loop, the game will softlock.</font>
     */
    public static final byte RUN_TYPE_INIT = 1;

    /** List of events managed by this EventList. */
    @Getter(AccessLevel.NONE)
    private List<Event> events = new ArrayList<>();
    /** List of events that will update parallel to other events. */
    @Getter(AccessLevel.NONE)
    private List<Event> parallelEvents = new ArrayList<>();
    /** The name of this event list. */
    protected String name;
    /** The type of blocking this event list enforces. */
    protected BlockType blockType;
    /** The run type for this event list, defining how it executes its events. */
    protected byte runType;

    /** The index of the current event being run from the list. */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int currentIndex = -1;
    /** Indicates whether the event list has been started and is active. */
    @Setter(AccessLevel.NONE)
    private boolean active;
    /** Flag to determine if the event list should reset after the current event finishes. */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean reset;

    private List<Condition<?>> conditions = new ArrayList<>();

    private GameObject owner;

    public EventList(DataSaveObject dso){
        load(dso);
    }

    public DataSaveObject load(DataSaveObject dso){
        events.addAll(dso.<DataSaveObject>getList("events").stream().map(data -> EventRegistry.createEvent(data.getString("ID", null), data)).toList());
        blockType = BlockType.values()[dso.getByte("block", (byte) BlockType.NONE.ordinal())];
        events.forEach(e -> e.blockingType = blockType.ordinal() > e.blockingType.ordinal() ? blockType : e.blockingType);
        name = dso.getString("name", null);
        runType = dso.getByte("type", RUN_TYPE_AUTO);
        conditions = new ArrayList<>(dso.<DataSaveObject>getList("conditions").stream().map(cDSO -> EvaluationRegistry.createCondition(cDSO.getString("id", null), cDSO)).toList());
        return dso;
    }

    /**
     * Determines, if this event list is ready to start<br>
     * This only happens, if this event list is not active and all {@link #conditions} evaluate true
     * @return is this event list ready to start
     */
    public boolean canStart(World world){
        return !isActive() && !events.isEmpty() && conditions.stream().allMatch(c -> c.evaluate(world));
    }

    /**
     * Executes the current event list. If the current event is finished,
     * it moves to the next event and prepares or executes it as needed. If all
     * events are finished, the list resets or deactivates itself as necessary.<br>
     * <br>
     * This method ensures that parallel and instant events are handled appropriately,
     * running them immediately or adding them to the list of parallel events. The
     * event list will block the game if the event type requires it.
     *
     * @param world the {@link World} in which the events are running.
     */
    public void run(World world){
        if(events.isEmpty()) {
            active = !active;
            reset = !reset;
            return;
        }

        // Run parallel events and remove finished ones
        parallelEvents.removeIf(e -> {
            e.run(world);
            if (e.isFinished(world)) {
                e.finish(world);
                return true;
            }
            return false;
        });

        // Start the next event if no event is currently active
        if(currentIndex == -1)
            nextEvent(world, owner);
        if(!active)
            return;

        // Run the current event
        var event = events.get(currentIndex);
        event.run(world);

        // Check if the current event is finished, and move to the next
        if(event.isFinished(world)){
            event.finish(world);
            nextEvent(world, owner);
        }
    }

    /**
     * Moves to the next event in the list and prepares it for execution. If the
     * next event is of a type that requires blocking (e.g., an update event),
     * the event list is added as a blocking event to the world.
     *
     * @param world  the {@link World} in which the events are running.
     * @param parent The parent object of this event list. Can be null if run elsewhere!
     */
    private void nextEvent(World world, GameObject parent){
        prepareNextEvent(world, parent);
        if (active && getCurrentEventBlockType().ordinal() >= BlockType.UPDATE.ordinal())
            world.addBlockingEvent(this);
    }

    /**
     * Prepares the next event in the list. If the event is instant or parallel, it
     * either executes the instant event immediately or adds parallel events to the
     * list of parallel events. If there are no more events to run, the event list
     * will be reset.
     *
     * @param world  the {@link World} in which the events are running.
     * @param parent The parent object of this event list. Can be null if run elsewhere!
     */
    private void prepareNextEvent(World world, GameObject parent){
        if(++currentIndex >= events.size()){
            reset(world);
            return;
        }
        var event = events.get(currentIndex);
        event.prepare(world, parent);

        // Handle instant and parallel events
        if(event.isInstant(world) || event.isParallel()){
            if(event.isInstant(world)) {
                // Completely run instant events immediately
                do
                    event.run(world);
                while (!event.isFinished(world));
                event.finish(world);
            } else {
                parallelEvents.add(event);
                event.run(world);
            }
            if(currentIndex + 1 >= events.size()){
                reset(world);
                return;
            }
            // If it's not a normal event recursively prepare the next event until a normal event is found,
            // or no more events are present
            prepareNextEvent(world, parent);
        }
    }

    /** Resets the event list to its initial state, making it ready to run again. */
    public void reset(World world){
        currentIndex = -1;
        active = false;
        reset = false;
        parallelEvents.forEach(e -> e.finish(world));
        parallelEvents.clear();
    }

    /**
     * Checks if the event list is empty, i.e., contains no events.
     *
     * @return true if the event list has no events, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Returns the current highest blocking type of the event list and the current event
     * @return the highest blocking type
     */
    public BlockType getCurrentEventBlockType(){
        if(!active)
            return BlockType.NONE;
        var event = events.get(currentIndex);
        return blockType.ordinal() > event.getBlockingType().ordinal() ? blockType : event.blockingType;
    }

    /**
     * Prepares the event list, sets a parent and set {@link #active} to true.
     * @param parent The parent object of this event list. Can be null if run elsewhere!
     */
    public void start(GameObject parent){
        GameLogger.logD("EventList","Start event list %s for object %s", name, new ObjChain<>(parent).next(p -> String.format("[%s] %s", p.getID(), p.getName())).get());
        owner = parent;
        active = true;
    }

    @Override
    public EventList clone() {
        try {
            var clone = (EventList) super.clone();
            clone.events = new ArrayList<>();
            clone.parallelEvents = new ArrayList<>();
            events.forEach(e -> clone.events.add((Event) e.clone()));
            clone.currentIndex = -1;
            clone.active = false;
            clone.reset = false;
            return clone;
        } catch (CloneNotSupportedException e){
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a string representation of the event list, which is either the name of the list
     * or a default string if the name is not set.
     *
     * @return a string representation of the event list.
     */
    @Override
    public String toString() {
        return name == null ? "Event list" : name;
    }

}
