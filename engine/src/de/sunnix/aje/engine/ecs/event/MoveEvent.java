package de.sunnix.aje.engine.ecs.event;

import de.sunnix.aje.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;

public class MoveEvent extends Event{

    private int object = -1;

    private float posX, posZ, init_posX, init_posZ;
    private float speed = .035f;

    public MoveEvent() {
        super("move");
    }

    @Override
    public void load(DataSaveObject dso) {
        object = dso.getInt("object", -1);
        init_posX = dso.getFloat("x", 0);
        init_posZ = dso.getFloat("z", 0);
        speed = dso.getFloat("s", .035f);
    }

    @Override
    public void prepare(World world) {
        posX = init_posX;
        posZ = init_posZ;
    }

    @Override
    public void run(World world) {
        var go = world.getGameObject(object);
        if(go != null){
            var velX = posX < 0 ? Math.max(posX, -speed) : Math.min(posX, speed);
            var velZ = posZ < 0 ? Math.max(posZ, -speed) : Math.min(posZ, speed);
            go.getPosition().add(velX, 0, velZ);
        }
        if(posX < 0)
            posX -= Math.max(posX, -speed);
        else
            posX -= Math.min(posX, speed);
        if(posZ < 0)
            posZ -= Math.max(posZ, -speed);
        else
            posZ -= Math.min(posZ, speed);
    }

    @Override
    public boolean isFinished(World world) {
        return posX == 0 && posZ == 0;
    }

    @Override
    public void finish(World world) {}

}
