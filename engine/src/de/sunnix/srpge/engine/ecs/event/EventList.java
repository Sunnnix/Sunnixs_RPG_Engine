package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;

@Getter
@Setter
public class EventList{

    public enum BlockType {
        NONE, USER_INPUT, UPDATE, UPDATE_GRAPHIC
    }

    public static final byte RUN_TYPE_AUTO = 0;

    @Getter(AccessLevel.NONE)
    private final List<Event> events = new ArrayList<>();
    protected String name;
    protected BlockType blockType;
    protected byte runType;

    /**
     * Current index of the running event
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int currentIndex = -1;
    /**
     * Should the list and index be reset on event finish
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean reset;

    public EventList(DataSaveObject dso){
        load(dso);
    }

    public DataSaveObject load(DataSaveObject dso){
        events.addAll(dso.<DataSaveObject>getList("events").stream().map(data -> EventRegistry.createEvent(data.getString("ID", null), data)).toList());
        name = dso.getString("name", null);
        blockType = BlockType.values()[dso.getByte("block", (byte) BlockType.NONE.ordinal())];
        runType = dso.getByte("type", RUN_TYPE_AUTO);
        return dso;
    }

    public void run(World world){
        if(events.isEmpty())
            return;
        Event event = null;
        boolean finished = currentIndex == -1 || (event = events.get(currentIndex)).isFinished(world);
        if(finished){
            if(event != null) {
                event.finish(world);
                if(reset){
                    currentIndex = -1;
                    reset = false;
                    return;
                }
            }
            currentIndex++;
            if(currentIndex >= events.size())
                currentIndex = 0;
            event = events.get(currentIndex);
            event.prepare(world);
            if(bitcheck(event.getBlockingType(), Event.BLOCK_GLOBAL_UPDATE))
                world.addBlockingEvent(event);
        }
        event.run(world);
    }

    public void reset(){
        reset = true;
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    @Override
    public String toString() {
        return name == null ? "Event list" : name;
    }

}
