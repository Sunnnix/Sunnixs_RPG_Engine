package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.Tile;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.util.Tuple.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem.MoveDirection.*;

public class PhysicSystem {
    public static final float EPSILON = 1e-4f;

    private static List<GameObject> objects = new ArrayList<>();
//    private static RegionGrid regonGrid = new RegionGrid();

//    public static void reloadRegions(Collection<GameObject> objects){
//        regonGrid.clear();
//        for (var obj : objects) {
//            regonGrid.addObject(obj);
//        }
//    }

    public static void add(GameObject go){
        objects.add(go);
    }

    public static void update(World world) {
//        reloadRegions(objects);
        for(var obj : objects){
            var comp = obj.getComponent(PhysicComponent.class);
            if(!comp.isFlying() && comp.isFalling()){
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
            if(!vel.equals(0, 0, 0)) {
                move(world, obj, vel.x, vel.y, vel.z);
                vel.set(0);
            }
            var comp = obj.getComponent(PhysicComponent.class);
            comp.setGroundPos(calculateGround(world, obj));
            if(!comp.isFlying())
                comp.setFalling(obj.getPosition().y > comp.getGroundPos() + EPSILON);
        };
    }

    public static void renderShadows(){
        for(var obj: objects){
            if(obj.getComponent(RenderComponent.class) == null)
                continue;
            var comp = obj.getComponent(PhysicComponent.class);
            var shadow = comp.getShadow();
            var pos = obj.getPosition().mul(1, 0, 1, new Vector3f()).add(0, -comp.getGroundPos(), 0); // set y to ground pos
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

    enum MoveDirection{
        SOUTH, EAST, WEST, NORTH, UP, DOWN
    }

    private static void move(World world, GameObject go, float dx, float dy, float dz) {
        var comp = go.getComponent(PhysicComponent.class);
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
            pPos.set(hitbox.getX(), hitbox.getY(), hitbox.getZ());
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

        var distanceMoved = tuple.t4();
        var comp = go.getComponent(PhysicComponent.class);

        if(comp.isPlatform() && !comp.isFalling() && !distanceMoved.equals(0, 0, 0)){
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

        return new Tuple2<>(moved, hitbox);
    }

    private static List<AABB> getHitboxes(World world, GameObject go, AABB sHB, AABB nHB){
        var list = new ArrayList<>(objects.stream().filter(obj -> !obj.equals(go)).map(obj -> obj.getComponent(PhysicComponent.class).getHitbox()).toList());
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
    private static Tuple4<Boolean, Boolean, AABB, Vector3f> checkNewPosition(List<AABB> hitboxes, AABB sHB, AABB nHB, MoveDirection dir){
        var moved = false;
        var check = true;
        var fromTile = false;
        while (check) {
            check = false;
            moved = true;
            for (var other : hitboxes) {
                if (nHB.intersects(other)) {
                    fromTile = other instanceof AABB.TileAABB;
                    AABB tmpHB = null;
                    var breakLoop = false;
                    switch (dir){
                        case SOUTH -> {
                            tmpHB = nHB.alignBottom(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                            }
                        }
                        case EAST -> {
                            tmpHB = nHB.alignRight(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                            }
                        }
                        case WEST -> {
                            tmpHB = nHB.alignLeft(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                            }
                        }
                        case NORTH -> {
                            tmpHB = nHB.alignTop(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                            }
                        }
                        case UP -> {
                            tmpHB = nHB.alignUp(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
                            }
                        }
                        case DOWN -> {
                            tmpHB = nHB.alignDown(other);
                            if (tmpHB.equals(sHB)) {
                                moved = false;
                                breakLoop = true;
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
        var distanceMoved = sHB.getDistance(nHB);
        return new Tuple4<>(moved, fromTile, moved ? nHB : sHB, distanceMoved);
    }

    private static float correctMovement(World world, AABB hitbox, MoveDirection dir, float speed){
        var cTiles = new ArrayList<AABB>();
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

    private static float getYOnSlope(Tile tile, float x, float z) {
        float localX = x % 1;
        float localZ = z % 1;

        return switch (tile.getSlopeDirection()) {
            case Tile.SLOPE_DIRECTION_NORTH:
                yield tile.getHeight() * localZ;
            case Tile.SLOPE_DIRECTION_EAST:
                yield tile.getHeight() * (1 - localX);
            case Tile.SLOPE_DIRECTION_SOUTH:
                yield tile.getHeight() * (1 - localZ);
            case Tile.SLOPE_DIRECTION_WEST:
                yield tile.getHeight() * localX;
            default:
                throw new IllegalStateException("Unexpected tile slope direction: " + tile.getSlopeDirection());
        };
    }

    private static float calculateGround(World world, GameObject go) {
        var toCheck = getPlainHitboxes(world, go);
        var oY = go.getPosition().y;
        var ground = 0f;
        for(var hb: toCheck){
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
        for(var obj: objects){
            if(obj.equals(go))
                continue;
            var oHB = obj.getComponent(PhysicComponent.class).getHitbox();
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
        for(var obj: objects){
            if(obj.equals(go))
                continue;
            var comp = obj.getComponent(PhysicComponent.class);
            if(comp.isFalling())
                continue;
            var oHB = comp.getHitbox();
            if(oHB.intersects(hitbox))
                if(oHB.getMinY() <= hitbox.getMaxY() + EPSILON)
                    if(oHB.getMinY() >= hitbox.getMaxY() - .1f)
                        list.add(obj);
        }
        return list;
    }

}
