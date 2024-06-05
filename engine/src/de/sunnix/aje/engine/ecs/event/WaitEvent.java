package de.sunnix.aje.engine.ecs.event;

import de.sunnix.aje.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;

public class WaitEvent extends Event{

    private int frames, framesLeft;

    public WaitEvent() {
        super("wait");
    }

    @Override
    public void load(DataSaveObject dso) {
        frames = dso.getInt("f", 1);
    }

    @Override
    public void prepare(World world) {
        framesLeft = frames;
    }

    @Override
    public void run(World world) {
        framesLeft--;
    }

    @Override
    public boolean isFinished(World world) {
        return framesLeft == 0;
    }

    @Override
    public void finish(World world) {

    }
}
