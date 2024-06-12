package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;

public class NULLEvent extends Event {

    public NULLEvent() {
        super("NULL");
    }

    @Override
    public void load(DataSaveObject dso) {}

    @Override
    public void prepare(World world) {}

    @Override
    public void run(World world) {}

    @Override
    public boolean isFinished(World world) {
        return true;
    }

    @Override
    public void finish(World world) {}

}
