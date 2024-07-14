package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

@Getter
public abstract class Event {

    public static final byte BLOCK_GLOBAL_UPDATE = 0b1;
    public static final byte BLOCK_USER_INPUT = 0b10;
    public static final byte BLOCK_UPDATE_GRAPHICS = 0b100;

    public final String ID;

    protected byte blockingType;

    public Event(String id){
        this.ID = id;
    }

    public abstract void load(DataSaveObject dso);

    public abstract void prepare(World world);

    public abstract void run(World world);

    public abstract boolean isFinished(World world);

    public abstract void finish(World world);

    @Override
    public Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


}
