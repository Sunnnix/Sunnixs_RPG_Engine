package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.systems.MapGrid;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.util.Tuple.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem.MoveDirection.*;
import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;

public class PhysicSystem {
    public static final float EPSILON = 1e-4f;
    public static final float STEP_AMOUNT = .2f;

    enum MoveDirection{
        SOUTH, EAST, WEST, NORTH, UP, DOWN
    }

    private static List<GameObject> objects;

    private static MapGrid mapGrid;

    private static final int MOVE_EVENT_FLAG_HIT_SOUTH = 0b1;
    private static final int MOVE_EVENT_FLAG_HIT_EAST = 0b10;
    private static final int MOVE_EVENT_FLAG_HIT_WEST = 0b100;
    private static final int MOVE_EVENT_FLAG_HIT_NORTH = 0b1000;
    private static final int MOVE_EVENT_FLAG_HIT_TOP = 0b10000;
    private static final int MOVE_EVENT_FLAG_HIT_BOTTOM = 0b100000;
    private static final int MOVE_EVENT_FLAG_HIT_TILE = 0b1000000;

    public static void init(int width, int height){
        objects = new ArrayList<>();
        mapGrid = new MapGrid(width, height);
    }

    public static void add(GameObject go){
        objects.add(go);
    }

    public static void update(World world) {
        for(var obj : objects){
            var climb = obj.hasState(States.CLIMB);
            if(climb) {
                var vel = obj.getVelocity();
                var comp = obj.getComponent(PhysicComponent.class);
                if(vel.x != 0 || comp.isJumped()) {
                    obj.removeState(States.CLIMB.id());
                    obj.removeState(States.CLIMBING_UP.id());
                    obj.removeState(States.CLIMBING_DOWN.id());
                } else {
                    vel.y -= vel.z * .5f;
                    vel.z = 0;
                    if (obj.hasState(States.MOVING)) {
                        obj.removeState(States.MOVING.id());
                        if(vel.y > 0)
                            obj.addState(States.CLIMBING_UP.id());
                        else
                            obj.addState(States.CLIMBING_DOWN.id());
                    } else {
                        obj.removeState(States.CLIMBING_UP.id());
                        obj.removeState(States.CLIMBING_DOWN.id());
                    }
                }
            }
            var comp = obj.getComponent(PhysicComponent.class);
            if(!comp.isFlying() && !climb && comp.isFalling()){
                var fallSpeed = comp.getFallSpeed();
                obj.getVelocity().add(0, fallSpeed, 0);
                var weight = comp.getWeight();
                fallSpeed -= weight / 36;
                if(fallSpeed < -weight)
                    fallSpeed = -weight;
                comp.setFallSpeed(fallSpeed);
            } else
                comp.setFallSpeed(0);
        }
        for(var obj : objects) {
            var vel = obj.getVelocity();
            if (!vel.equals(0, 0, 0)) {
                move(world, obj, vel.x, vel.y, vel.z);
                vel.set(0);
            }
        }
        var toCheck = mapGrid.getDirtyObjects();
        for(var obj: toCheck){
            var comp = obj.getComponent(PhysicComponent.class);
            var groundPos = calculateGround(world, obj);
            comp.setGroundPos(groundPos);
            if(!comp.isFlying() && !obj.hasState(States.CLIMB)) {
                // if object run of slope, push it to the ground
                if(!comp.isFalling() && comp.getHitbox().getY() - STEP_AMOUNT <= groundPos) {
                    obj.getPosition().y = groundPos;
                    comp.reloadHitbox();
                }
                else
                    comp.setFalling(obj.getPosition().y > comp.getGroundPos() + EPSILON);
            }
        };
    }

    public static void renderShadows(){
        final var camSize = Camera.getSize().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1.5f);
        final var camPos = Camera.getPos().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1, -1).sub(camSize.div(2, new Vector2f()));

        for(var obj: objects){
            if(obj.getComponent(RenderComponent.class) == null)
                continue;
            var comp = obj.getComponent(PhysicComponent.class);
            var pos = obj.getPosition().mul(1, 0, 1, new Vector3f()).add(0, -comp.getGroundPos(), 0); // set y to ground pos
            var goSize = obj.size;
            if(!comp.isHasShadow() || pos.x > camPos.x + camSize.x || pos.x + goSize.x < camPos.x || pos.z + pos.y > camPos.y + camSize.y || pos.z + goSize.x < camPos.y)
                continue;
            var shadow = comp.getShadow();
            pos.y -= (obj.size.x * Core.TILE_HEIGHT / 2 - shadow.getSize().y / 2) / Core.TILE_HEIGHT; // center texture if shadow is smaller then object
            comp.getShadow().render(pos, obj.size.x, obj.getZ_pos());
        }
    }

    public static void renderHitboxes(){
        DebugRenderObject.setColor(0, 0, 1, .25f);
        for(var o : objects){
            var comp = o.getComponent(PhysicComponent.class);
            var hb = comp.getHitbox();
            var dro = comp.getDro();
            dro.prepareRender();
            dro.render(new Vector3f(hb.getX(), hb.getY(), hb.getZ()), new Vector2f(hb.getWidth(), hb.getHeight()));
        };
    }

    private static void move(World world, GameObject go, float dx, float dy, float dz) {
        var comp = go.getComponent(PhysicComponent.class);
        if(!comp.isCollision()){
            go.addPosition(dx, dy, dz);
            comp.reloadHitbox();
            return;
        }
        var hitbox = comp.getHitbox();
        var moved = false;

        if(dx != 0){
            var tuple = moveHorizontal(world, go, hitbox, dx, dz == 0);
            moved = tuple.t1();
            hitbox = tuple.t2();
        }

        if(dz != 0){
            var tuple = moveVertical(world, go, hitbox, dz, dx == 0);
            if(!moved)
                moved = tuple.t1();
            hitbox = tuple.t2();
        }

        if(dy != 0){
            var tuple = moveHeight(world, go, hitbox, dy);
            if(!moved)
                moved = tuple.t1();
            if(dy > 0 && !moved) // if the object hits its head, stop jumping
                comp.setFallSpeed(0);
            hitbox = tuple.t2();
        }

        if (moved) {
            var pPos = go.getPosition();
            go.setPosition(hitbox.getX(), hitbox.getY(), hitbox.getZ());
            if (pPos.y < 0)
                pPos.y = 0;
            comp.reloadHitbox();
        }
    }

    private static Tuple2<Boolean, AABB> moveHorizontal(World world, GameObject go, AABB hitbox, float speed, boolean correctMovement){
        boolean moved;

        var nHB = hitbox.transform(speed, 0, 0);
        var toCheck = getHitboxes(world, go, hitbox, nHB);

        var tuple = checkNewPosition(toCheck, hitbox, nHB, speed > 0 ? EAST : WEST);
        moved = tuple.t1();
        hitbox = tuple.t3();

        var distanceMoved = tuple.t4();
        var comp = go.getComponent(PhysicComponent.class);

        if(comp.isPlatform() && !comp.isFalling() && !distanceMoved.equals(0, 0, 0)){
            var objects = getObjectsOnTop(go, hitbox);
            for(var obj: objects)
                move(world, obj, distanceMoved.x, distanceMoved.y, distanceMoved.z);
        }

        if (correctMovement && !moved && tuple.t2()) {
            var tuple2 = moveVertical(world, go, hitbox, correctMovement(world, hitbox, speed > 0 ? EAST : WEST, speed), false);
            moved = tuple2.t1();
            hitbox = tuple2.t2();
        }
        return new Tuple2<>(moved, hitbox);
    }

    private static Tuple2<Boolean, AABB> moveVertical(World world, GameObject go, AABB hitbox, float speed, boolean correctMovement){
        boolean moved;

        var nHB = hitbox.transform(0, 0, speed);
        var toCheck = getHitboxes(world, go, hitbox, nHB);

        var tuple = checkNewPosition(toCheck, hitbox, nHB, speed > 0 ? SOUTH : NORTH);
        moved = tuple.t1();
        hitbox = tuple.t3();
        var flag = tuple.t5();

        var comp = go.getComponent(PhysicComponent.class);

        // if the object moves horizontal (correctMovement == false) then don't climb
        if(correctMovement && comp.isCanClimb() && bitcheck(flag, MOVE_EVENT_FLAG_HIT_TILE) && bitcheck(flag, MOVE_EVENT_FLAG_HIT_NORTH)){
            var tZ = (int) Math.floor(hitbox.getMinZ() - .1f);
            var ladder = false;
            for(var tX = (int) hitbox.getMinX(); tX < Math.ceil(hitbox.getMaxX()); tX++){
                var tile = world.getTile(tX, tZ);
                if(tile == null)
                    continue;
                var wallProps = tile.getWallProperties((int)hitbox.getMinY(), (int) Math.ceil(hitbox.getMaxY()));
                for(var prop: wallProps)
                    if(prop != null && prop.isLadder()){
                        ladder = true;
                        break;
                    }
                if(ladder) {
                    correctMovement = false;
                    go.addState(States.CLIMB.id());
                    comp.setFalling(false);
                    break;
                }
            }
            if(!ladder){
                go.removeState(States.CLIMB.id());
                go.removeState(States.CLIMBING_UP.id());
                go.removeState(States.CLIMBING_DOWN.id());
            }
        }

        var distanceMoved = tuple.t4();

        if(!go.hasState(States.CLIMB) && comp.isPlatform() && !comp.isFalling() && !distanceMoved.equals(0, 0, 0)){
            var objects = getObjectsOnTop(go, hitbox);
            for(var obj: objects)
                move(world, obj, distanceMoved.x, distanceMoved.y, distanceMoved.z);
        }

        if (correctMovement && !moved && tuple.t2()){
            var tuple2 = moveHorizontal(world, go, hitbox, correctMovement(world, hitbox, speed > 0 ? SOUTH : NORTH, speed), false);
            moved = tuple2.t1();
            hitbox = tuple2.t2();
        }
        return new Tuple2<>(moved, hitbox);
    }

    private static Tuple2<Boolean, AABB> moveHeight(World world, GameObject go, AABB hitbox, float speed){
        boolean moved;

        var nHB = hitbox.transform(0, speed, 0);
        var toCheck = getHitboxes(world, go, hitbox, nHB);

        var tuple = checkNewPosition(toCheck, hitbox, nHB, speed > 0 ? UP : DOWN);
        moved = tuple.t1();
        hitbox = tuple.t3();
        var flag = tuple.t5();

        var comp = go.getComponent(PhysicComponent.class);

        if(go.hasState(States.CLIMB)){
            if(bitcheck(flag, MOVE_EVENT_FLAG_HIT_TILE) && bitcheck(flag, MOVE_EVENT_FLAG_HIT_BOTTOM)){
                go.removeState(States.CLIMB.id());
                go.removeState(States.CLIMBING_DOWN.id());
            } else {
                var tZ = (int) Math.floor(hitbox.getMinZ() - .1f);
                var ladder = false;

                var sMinY = (int) hitbox.getMinY() - 1;
                var sMaxY = (int) Math.ceil(hitbox.getMaxY()) - 1;

                for (var tX = (int) hitbox.getMinX(); tX < Math.ceil(hitbox.getMaxX()); tX++) {
                    var tile = world.getTile(tX, tZ);
                    if (tile == null)
                        continue;
                    var wallProps = tile.getWallProperties(sMinY, sMaxY);
                    for (var prop : wallProps)
                        if (prop != null && prop.isLadder()) {
                            ladder = true;
                            break;
                        }
                    if(ladder)
                        break;
                }
                if (!ladder) {
                    go.removeState(States.CLIMB.id());
                    go.removeState(States.CLIMBING_UP.id());
                    go.removeState(States.CLIMBING_DOWN.id());
                }
            }
        }

        return new Tuple2<>(moved, hitbox);
    }

    private static List<AABB> getHitboxes(World world, GameObject go, AABB sHB, AABB nHB){
        var list = new ArrayList<>(mapGrid.getMatchingMapGridObjets(go).stream().filter(obj -> !obj.equals(go) && obj.getComponent(PhysicComponent.class).isCollision()).map(obj -> obj.getComponent(PhysicComponent.class).getHitbox()).toList());
        var tiles = new ArrayList<AABB>();
        for(var x = (int) Math.min(sHB.getMinX(), nHB.getMinX()); x <= Math.max(Math.ceil(sHB.getMaxX()), Math.ceil(nHB.getMaxX())); x++)
            for(var z = (int) Math.min(sHB.getMinZ(), nHB.getMinZ()); z <= Math.max(Math.ceil(sHB.getMaxZ()), Math.ceil(nHB.getMaxZ())); z++){
                var tile = world.getTile(x, z);
                if(tile == null)
                    continue;
                tiles.add(tile.getHitbox());
            }
        list.addAll(tiles);
        return list;
    }

    /**
     *
     * @param hitboxes
     * @param sHB
     * @param nHB
     * @param dir
     * @return tuple of<br>
     * - Boolean (moved)
     * - Boolean (fromTile)
     * - AABB (new Hitbox)
     * - moved distance
     */
    private static Tuple5<Boolean, Boolean, AABB, Vector3f, Integer> checkNewPosition(List<AABB> hitboxes, AABB sHB, AABB nHB, MoveDirection dir){
        var moved = false;
        var check = true;
        var fromTile = false;
        var preNHB = nHB;

        var upshiftTest = false;
        var upshift = 0f;

        var flag = 0;

        while (check) {
            check = false;
            moved = true;
            for (var other : hitboxes) {
                if(other instanceof AABB.TileAABB tile)
                    tile.prepare(nHB);
                if (nHB.intersects(other)) {
                    var yDiff = other.getMaxY() - nHB.getY();
                    if(dir != DOWN && yDiff > upshift && yDiff < STEP_AMOUNT){
                        upshiftTest = true;
                        upshift = yDiff;
                        continue;
                    }
                    fromTile = other instanceof AABB.TileAABB;
                    AABB tmpHB = null;
                    var breakLoop = false;
                    switch (dir){
                        case SOUTH -> {
                            if(other instanceof AABB.TileAABB) { // to prevent stair glitching
                                tmpHB = sHB;
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            } else {
                                tmpHB = nHB.alignBottom(other);
                            }
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_SOUTH;
                            }
                        }
                        case EAST -> {
                            if(other instanceof AABB.TileAABB) {
                                tmpHB = sHB;
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            } else
                                tmpHB = nHB.alignRight(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_EAST;
                            }
                        }
                        case WEST -> {
                            if(other instanceof AABB.TileAABB) {
                                tmpHB = sHB;
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            } else
                                tmpHB = nHB.alignLeft(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_WEST;
                            }
                        }
                        case NORTH -> {
                            if(other instanceof AABB.TileAABB) {
                                tmpHB = sHB;
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            } else
                                tmpHB = nHB.alignTop(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_NORTH;
                            }
                        }
                        case UP -> {
                            if(other instanceof AABB.TileAABB)
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            tmpHB = nHB.alignUp(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_TOP;
                            }
                        }
                        case DOWN -> {
                            if(other instanceof AABB.TileAABB)
                                flag |= MOVE_EVENT_FLAG_HIT_TILE;
                            tmpHB = nHB.alignDown(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                                flag |= MOVE_EVENT_FLAG_HIT_BOTTOM;
                            }
                        }
                    }
                    if(breakLoop)
                        break;
                    nHB = tmpHB;
                    check = true;
                    break;
                }
            }
        }
        // check if object can step up the height without collision
        if(upshiftTest){
            preNHB = preNHB.transform(0, upshift, 0);
            var success = true;
            for (var other : hitboxes) {
                if(other instanceof AABB.TileAABB tile)
                    tile.prepare(preNHB);
                if(preNHB.intersects(other)){
                    success = false;
                    break;
                }
            }
            if(success){
                nHB = preNHB;
                moved = true;
            }
        }
        var distanceMoved = sHB.getDistance(nHB);
        return new Tuple5<>(moved, fromTile, moved ? nHB : sHB, distanceMoved, flag);
    }

    private static float correctMovement(World world, AABB hitbox, MoveDirection dir, float speed){
        var cTiles = new ArrayList<AABB.TileAABB>();
        return switch (dir){
            case EAST, WEST -> {
                var x = 0;
                if(dir == EAST)
                    x = (int)(hitbox.getMaxX() + .6);
                else
                    x = (int)(hitbox.getMinX() - .4);
                for(var z = (int) hitbox.getMinZ(); z <= Math.ceil(hitbox.getMaxZ()); z++){
                    var tile = world.getTile(x, z);
                    if(tile == null)
                        continue;
                    cTiles.add(tile.getHitbox());
                }
                for(var tile: cTiles){
                    tile.prepare(hitbox);
                    if(tile.getMaxZ() < hitbox.getMaxZ() && tile.getMaxY() - EPSILON < hitbox.getMinY()){
                        yield -Math.abs(speed) / 2;
                    } else if(tile.getMinZ() > hitbox.getMinZ() && tile.getMaxY() - EPSILON < hitbox.getMinY()){
                        yield Math.abs(speed) / 2;
                    }
                }
                yield 0;
            }
            case SOUTH, NORTH -> {
                var z = 0;
                if(dir == SOUTH)
                    z = (int)(hitbox.getMaxZ() + .6);
                else
                    z = (int)(hitbox.getMinZ() - .4);
                for(var x = (int) hitbox.getMinX(); x <= Math.ceil(hitbox.getMaxX()); x++){
                    var tile = world.getTile(x, z);
                    if(tile == null)
                        continue;
                    cTiles.add(tile.getHitbox());
                }
                for(var tile: cTiles){
                    tile.prepare(hitbox);
                    if(tile.getMaxX() < hitbox.getMaxX() && tile.getMaxY() - EPSILON < hitbox.getMinY()){
                        yield -Math.abs(speed) / 2;
                    } else if(tile.getMinX() > hitbox.getMinX() && tile.getMaxY() - EPSILON < hitbox.getMinY()){
                        yield Math.abs(speed) / 2;
                    }
                }
                yield 0;
            }
            default -> {
                yield 0;
            }
        };
    }

    private static float calculateGround(World world, GameObject go) {
        var toCheck = getPlainHitboxes(world, go);
        var goHB = go.getComponent(PhysicComponent.class).getHitbox();
        var oY = go.getPosition().y;
        var ground = 0f;
        for(var hb: toCheck){
            if(hb instanceof AABB.TileAABB tile)
                tile.prepare(goHB);
            if(hb.getMaxY() - EPSILON > oY)
                continue;
            if(hb.getMaxY() - EPSILON > ground)
                ground = hb.getMaxY();
        }
        return ground;
    }

    private static ArrayList<AABB> getPlainHitboxes(World world, GameObject go){
        var list = new ArrayList<AABB>();
        var hb = go.getComponent(PhysicComponent.class).getHitbox();
        hb = hb.transform(0, -hb.getY(), 0).resize(hb.getWidth(), Float.POSITIVE_INFINITY);
        var objects = mapGrid.getMatchingMapGridObjets(go);
        for(var obj: objects){
            if(obj.equals(go))
                continue;
            var comp = obj.getComponent(PhysicComponent.class);
            if(!comp.isCollision())
                continue;
            var oHB = comp.getHitbox();
            if(oHB.intersects(hb))
                list.add(oHB);
        }
        for(var x = (int) Math.min(hb.getMinX(), hb.getMinX()); x <= Math.max(Math.ceil(hb.getMaxX()), Math.ceil(hb.getMaxX())); x++)
            for(var z = (int) Math.min(hb.getMinZ(), hb.getMinZ()); z <= Math.max(Math.ceil(hb.getMaxZ()), Math.ceil(hb.getMaxZ())); z++){
                var tile = world.getTile(x, z);
                if(tile == null)
                    continue;
                var thb = tile.getHitbox();
                if(thb.intersects(hb))
                    list.add(thb);
            }
        return list;
    }

    private static List<GameObject> getObjectsOnTop(GameObject go, AABB hitbox){
        var list = new ArrayList<GameObject>();
        hitbox = hitbox.transform(0, .1f, 0);
        var objects = mapGrid.getMatchingMapGridObjets(go);
        for(var obj: objects){
            if(obj.equals(go))
                continue;
            var comp = obj.getComponent(PhysicComponent.class);
            if(!comp.isCollision() || comp.isFalling())
                continue;
            var oHB = comp.getHitbox();
            if(oHB.intersects(hitbox))
                if(oHB.getMinY() <= hitbox.getMaxY() + EPSILON)
                    if(oHB.getMinY() >= hitbox.getMaxY() - .1f)
                        list.add(obj);
        }
        return list;
    }

    public static void relocateGridObject(Vector3f prePos, Vector3f newPos, GameObject object) {
        mapGrid.relocateGridObject(prePos, newPos, object);
    }

    public static void markDirty(GameObject object) {
        mapGrid.markDirty(object);
    }

    public static List<GameObject> getCollidingObjects(float x, float z) {
        return mapGrid.getObjectFieldOf(x, z);
    }

}
