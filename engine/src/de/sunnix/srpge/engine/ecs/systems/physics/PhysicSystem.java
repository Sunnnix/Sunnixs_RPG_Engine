package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.Tile;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
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
        for(var obj : objects) {
            var vel = obj.getVelocity();
            if(vel.equals(0, 0, 0))
                continue;
            move(world, obj, vel.x, vel.y, vel.z);
            vel.set(0);
        };
    }

    public static void drawHitboxes(){
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

        // Bewegung in Y-Richtung
        if (dy != 0) {
            float stepY = Math.signum(dy) * 0.1f; // Kleine Schritte in der Y-Richtung
            while (Math.abs(dy) > EPSILON) {
                float moveY = Math.abs(dy) < Math.abs(stepY) ? dy : stepY;
                var nHB = hitbox.transform(0, moveY, 0);
                if (nHB.getMinY() < 0)
                    nHB = new AABB(nHB.getX(), 0, nHB.getZ(), nHB.getWidth(), nHB.getHeight());
                if (!isCollision(world, go, nHB)) {
                    hitbox = nHB;
                    dy -= moveY;
                    moved = true;
                } else {
                    dy = 0;
                    break;
                }
            }
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

        if (correctMovement && !moved && tuple.t2()){
            var tuple2 = moveHorizontal(world, go, hitbox, correctMovement(world, hitbox, speed > 0 ? SOUTH : NORTH, speed), false);
            moved = tuple2.t1();
            hitbox = tuple2.t2();
        }
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
     */
    private static Tuple3<Boolean, Boolean, AABB> checkNewPosition(List<AABB> hitboxes, AABB sHB, AABB nHB, MoveDirection dir){
        var moved = false;
        var check = true;
        var fromTile = false;
        while (check) {
            check = false;
            moved = true;
            for (var other : hitboxes) {
                if (nHB.intersects(other)) {
                    fromTile = other instanceof AABB.TileAABB;
                    AABB tmpHB = null; // TODO (remove)
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

                        }
                        case DOWN -> {

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
        return new Tuple3<>(moved, fromTile, moved ? nHB : sHB);
    }

    private static float correctMovement(World world, AABB hitbox, MoveDirection dir, float speed){
        var cTiles = new ArrayList<AABB>();
        return switch (dir){
            case EAST, WEST -> {
                var x = 0;
                if(dir == EAST)
                    x = (int)(hitbox.getMaxX() + .9);
                else
                    x = (int)(hitbox.getMinX() - .9);
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
                    z = (int)(hitbox.getMaxZ() + .9);
                else
                    z = (int)(hitbox.getMinZ() - .9);
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

    private static boolean isCollision(World world, GameObject go, AABB hitbox) {
        boolean collision = false;
        boolean onSlope = false;

        int startX = (int)Math.floor(hitbox.getMinX());
        int endX = (int)Math.ceil(hitbox.getMaxX());
        int startZ = (int)Math.floor(hitbox.getMinZ());
        int endZ = (int)Math.ceil(hitbox.getMaxZ());

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                Tile tile = world.getTile(x, z);
                if (tile != null) {
                    AABB tileAABB = tile.getHitbox();
                    if (tile.getSlopeDirection() != 0) {
                        if (hitbox.intersects(tileAABB)) {
                            // Kollision mit Schräge
                            float slopeY = getYOnSlope(tile, hitbox.getMinX(), hitbox.getMinZ());
                            if (hitbox.getMinY() < slopeY + EPSILON && hitbox.getMaxY() > slopeY - EPSILON) {
                                onSlope = true;
                            }
                        }
                    } else {
                        if (hitbox.intersects(tileAABB)) {
                            // Kollision mit solidem Block
                            collision = true;
                            break;
                        }
                    }
                }
            }
            if (collision) break;
        }

        if (!collision) {
            for (var obj : objects) {
                var other = obj.getComponent(PhysicComponent.class).getHitbox();
                if (!obj.equals(go) && hitbox.intersects(other)) {
                    collision = true;
                    break;
                }
            }
        }

        return collision;
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
}
