package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.evaluation.Condition;
import de.sunnix.srpge.engine.evaluation.EvaluationRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;

/**
 * The EventList class manages a list of {@link Event} objects for a
 * {@link GameObject} in the game. Events in the list can be run sequentially,
 * and can optionally block certain game processes (like user input or updates).
 * The list provides functionality to {@link #reset() reset}, {@link #run(World) run}, and manage event states.
 */
@Getter
@Setter
public class EventList{

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

    /** List of events managed by this EventList. */
    @Getter(AccessLevel.NONE)
    private final List<Event> events = new ArrayList<>();
    /** List of events that will update parallel to other events. */
    @Getter(AccessLevel.NONE)
    private final List<Event> parallelEvents = new ArrayList<>();
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
    private boolean active;
    /** Flag to determine if the event list should reset after the current event finishes. */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean reset;

    private List<Condition<?>> conditions = new ArrayList<>();

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
        return !isActive() && conditions.stream().allMatch(c -> c.evaluate(world));
    }

    /**
     * Runs the current event in the list. If the current event is finished, it moves
     * to the next event and executes it. If all events are finished, it resets or deactivates
     * the event list as necessary.
     *
     * @param world the {@link World} in which the events are running.
     */
    public void run(World world){
        if(events.isEmpty()) {
            active = !active;
            reset = !reset;
            return;
        }
        Event event = null;
        boolean finished = currentIndex == -1 || (event = events.get(currentIndex)).isFinished(world);
        if(finished){
            if(event != null) {
                event.finish(world);
                if(reset){
                    reset();
                    return;
                }
            }
            do {
                currentIndex++;
                if(currentIndex >= events.size()) {
                    reset();
                    return;
                }
                event = events.get(currentIndex);
                event.prepare(world);
                if(event.isParallel())
                    parallelEvents.add(event);
            } while (event.isParallel());
            if(getCurrentEventBlockType().ordinal() >= BlockType.UPDATE.ordinal())
                world.addBlockingEvent(this);
        }
        parallelEvents.removeIf(e -> {
           e.run(world);
           if(e.isFinished(world)) {
               e.finish(world);
               return true;
           }
           return false;
        });
        event.run(world);
    }

    /** Resets the event list to its initial state, making it ready to run again. */
    public void reset(){
        currentIndex = -1;
        active = false;
        reset = false;
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
