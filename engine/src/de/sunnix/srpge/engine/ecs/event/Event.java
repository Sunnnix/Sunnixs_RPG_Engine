package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import lombok.Getter;
import de.sunnix.srpge.engine.ecs.event.EventList.BlockType;

/**
 * The Event class is used to represent actions that {@link GameObject GameObjects} can perform to interact with the game world.
 * Each GameObject can have a list of events that are executed in sequence.
 * Every game tick, the current event's {@link #run(World)} method is called.
 * Once the event signals completion via the {@link #isFinished(World)} method,
 * the next event in the list will be selected and {@link #prepare(World)} is executed.<br>
 * <br>
 * Events can have various behaviors and interact with different game systems,
 * making them a flexible tool for creating complex in-game scenarios.
 */
@Getter
public abstract class Event implements Cloneable {

    /** Unique identifier for this event. */
    public final String ID;
    /** Type of blocking applied by this event (e.g., blocking user input, global updates). */
    protected BlockType blockingType = BlockType.NONE;
    /** Will other events wait for this event to finish */
    protected boolean parallel;

    /**
     * Constructs a new event with the specified ID.
     *
     * @param id the unique ID for this event
     */
    public Event(String id){
        this.ID = id;
    }

    /**
     * Loads the event's data from a {@link DataSaveObject}.
     *
     * @param dso the data object from which to load the event's data
     */
    public abstract void load(DataSaveObject dso);

    /**
     * Prepares the event for execution within the given world.
     *
     * @param world the game world where the event will be executed
     */
    public abstract void prepare(World world);

    /**
     * Executes the event's logic. This method is called each game tick while the event is active.
     *
     * @param world the game world in which the event is executed
     */
    public abstract void run(World world);

    /**
     * Checks whether the event has finished executing.
     *
     * @param world the game world in which the event is being executed
     * @return true if the event is finished, false otherwise
     */
    public abstract boolean isFinished(World world);

    /**
     * Finalizes the event after it has finished execution.
     *
     * @param world the game world in which the event was executed
     */
    public void finish(World world){}

    /**
     * Determines if this object should be processed as an instant event.<br>
     * Instant events are executed and completed within the same frame, meaning
     * they do not delay or pause the {@link EventList event list}.
     *
     * @param world the game world in which the event was executed
     * @return {@code true} if the event runs and finishes within the same frame;
     *         {@code false} otherwise.
     */
    public boolean isInstant(World world){
        return false;
    }

    /**
     * Creates a clone of this event.
     *
     * @return a cloned instance of this event
     */
    @Override
    public Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }


}
