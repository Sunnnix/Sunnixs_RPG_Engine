package de.sunnix.aje.engine.ecs.event;

import de.sunnix.aje.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

public abstract class Event {

    public static final byte BLOCK_UPDATE = 0b1;
    public static final byte BLOCK_RENDERING = 0b10;

    public final String ID;

    @Getter
    protected byte blockingType;

    public Event(String id){
        this.ID = id;
    }

    public abstract void load(DataSaveObject dso);

    public abstract void prepare(World world);

    public abstract void run(World world);

    public abstract boolean isFinished(World world);

    public abstract void finish(World world);


}
