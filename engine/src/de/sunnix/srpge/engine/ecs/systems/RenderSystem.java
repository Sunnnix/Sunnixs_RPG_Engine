package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.graphics.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;

import static de.sunnix.srpge.engine.util.FunctionUtils.*;

public class RenderSystem {

    private static ArrayList<GameObject> objects;
    private static MapGrid mapGrid;

    public static void init(int width, int height){
        objects = new ArrayList<>();
        mapGrid = new MapGrid(width, height);
    }

    public static void renderObjects() {
        final var camSize = Camera.getSize().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1.5f);
        final var camPos = Camera.getPos().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1, -1).sub(camSize.div(2, new Vector2f()));

        for(var go: objects) {
            if(!go.isEnabled())
                continue;
            var goPos = go.getPosition();
            var goSize = go.size;
            if(goPos.x > camPos.x + camSize.x || goPos.x + goSize.x < camPos.x || goPos.z - goPos.y > camPos.y + camSize.y || goPos.z + goSize.x < camPos.y)
                continue;
            go.getComponent(RenderComponent.class).render(go);
        }
    }

    public static void addGO(GameObject go) {
        objects.add(go);
    }

    private static boolean collidingPlane(GameObject o1, GameObject o2){
        var pos1 = o1.getPosition();
        var pos2 = o2.getPosition();
        var size1 = o1.getSize();
        var size2 = o2.getSize();

        var hb1 = new AABB(pos1.x, 0, pos1.z, size1.x, 1);
        var hb2 = new AABB(pos2.x, 0, pos2.z, size2.x, 1);

        return hb1.intersects(hb2);
    }

    private static <T> T findFirst(Collection<T> list, Function<T, Boolean> filter){
        for(var o : list)
            if(filter.apply(o))
                return o;
        return null;
    }

    public static void update(){
        final var camSize = Camera.getSize().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1.5f);
        final var camPos = Camera.getPos().div(Core.TILE_WIDTH, Core.TILE_HEIGHT, new Vector2f()).mul(1, -1).sub(camSize.div(2, new Vector2f()));
        for(var go: objects){
            var goPos = go.getPosition();
            var goSize = go.size;
            if(goPos.x > camPos.x + camSize.x || goPos.x + goSize.x < camPos.x || goPos.z - goPos.y > camPos.y + camSize.y || goPos.z + goSize.x < camPos.y)
                continue;
            go.getComponent(RenderComponent.class).update(go);
        }
    }

    public static void prepareRender() {
        var objects = mapGrid.getDirtyObjects();
        if(objects.isEmpty())
           return;
        objects.removeIf(go -> !go.isEnabled());
        objects.forEach(go -> go.setZ_pos(go.getPosition().z + go.size.x));

        var colliding = new ArrayList<Set<GameObject>>();

        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                var go = objects.get(i);
                var go2 = objects.get(j);
                if(go.equals(go2) || !collidingPlane(go, go2))
                    continue;
                var list = findFirst(colliding, o -> o.contains(go) || o.contains(go2));
                if(list == null){
                    list = new HashSet<>();
                    list.add(go);
                    list.add(go2);
                    colliding.add(list);
                } else {
                    list.add(go);
                    list.add(go2);
                }
            }
        }

        for(var cList : colliding){
            var sorted = cList.stream().sorted((o1, o2) -> {
                var pos1 = o1.getPosition();
                var size1 = o1.getSize();
                var pos2 = o2.getPosition();
                var size2 = o2.getSize();
                if(pos1.y + size1.y > pos2.y + EPSILON || pos1.y >= pos2.y + size2.x)
                    return -1;
                else if(pos2.y + size2.y > pos1.y + EPSILON || pos2.y >= pos1.y + size1.x)
                    return 1;
                else return -Float.compare(o1.getZ_pos(), o2.getZ_pos());
            }).toList();

            for(int n = 0; n < sorted.size(); n++)
                for (int i = 0; i < sorted.size(); i++) {
                    var go = sorted.get(i);
                    var next = i + 1 == sorted.size() ? null : sorted.get(i + 1);
                    if(next != null)
                        if(go.getZ_pos() <= next.getZ_pos()) {
                            go.setZ_pos(next.getZ_pos());
                            next.setZ_pos(next.getZ_pos() - .001f);
                        }
                }
        }
    }

    public static void relocateGridObject(Vector3f prePos, Vector3f newPos, GameObject object) {
        mapGrid.relocateGridObject(prePos, newPos, object);
    }

    public static void markDirty(GameObject object) {
        mapGrid.markDirty(object);
    }
}
