package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;

import static de.sunnix.srpge.engine.ecs.Direction.*;

public class MoveEvent extends Event{

    protected int object = -1;

    protected float posX, posY, posZ;
    private float rPosX, rPosY, rPosZ; // remaining path
    protected float speed = .035f;

    public MoveEvent() {
        super("move");
    }

    @Override
    public void load(DataSaveObject dso) {
        object = dso.getInt("object", -1);
        posX = dso.getFloat("x", 0);
        posY = dso.getFloat("y", 0);
        posZ = dso.getFloat("z", 0);
        speed = dso.getFloat("s", .035f);
    }

    @Override
    public void prepare(World world) {
        rPosX = posX;
        rPosY = posY;
        rPosZ = posZ;
        var go = world.getGameObject(object);
        if(go == null)
            return;
        go.addState(States.MOVING.id());
    }

    @Override
    public void run(World world) {
        var go = world.getGameObject(object);
        if(go != null){
            var velX = rPosX < 0 ? Math.max(rPosX, -speed) : Math.min(rPosX, speed);
            var velY = rPosY < 0 ? Math.max(rPosY, -speed) : Math.min(rPosY, speed);
            var velZ = rPosZ < 0 ? Math.max(rPosZ, -speed) : Math.min(rPosZ, speed);
            go.getVelocity().add(velX, velY, velZ);
            var render = go.getComponent(RenderComponent.class);
            if(render != null){
                var direction = SOUTH;
                var maxValue = 0f;
                if(velX != 0) {
                    if(velX > 0)
                        direction = EAST;
                    else
                        direction = WEST;
                    maxValue = Math.abs(velX);
                }
                if(velZ != 0){
                    var tmp = Math.abs(velZ);
                    if(tmp > maxValue) {
                        if(velZ > 0)
                            direction = SOUTH;
                        else
                            direction = NORTH;
                    }
                }
                go.setFacing(direction);
            }
        }
        if(rPosX < 0)
            rPosX -= Math.max(rPosX, -speed);
        else
            rPosX -= Math.min(rPosX, speed);
        if(rPosY < 0)
            rPosY -= Math.max(rPosY, -speed);
        else
            rPosY -= Math.min(rPosY, speed);
        if(rPosZ < 0)
            rPosZ -= Math.max(rPosZ, -speed);
        else
            rPosZ -= Math.min(rPosZ, speed);
    }

    @Override
    public boolean isFinished(World world) {
        return rPosX == 0 && rPosY == 0 && rPosZ == 0;
    }

    @Override
    public void finish(World world) {
        var go = world.getGameObject(object);
        if(go == null)
            return;
        go.removeState(States.MOVING.id());
    }

}
