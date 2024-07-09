package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.ecs.components.Component;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;

import java.util.*;
import java.util.function.Function;

public class RenderSystem {

    private static final ArrayList<GameObject> objects = new ArrayList<>();

    public static void renderObjects() {
        objects.forEach(go -> {
            var rc = go.getComponent(RenderComponent.class);
            if(rc == null)
                Component.RENDER.render(go); // Old Renderer
            else
                rc.render(go); // New Renderer
        });
    }

    public static void addGO(GameObject go) {
        objects.add(go);
    }

    private static boolean collidingPlane(GameObject o1, GameObject o2){
        var pos1 = o1.getPosition();
        var pos2 = o2.getPosition();
        var size1 = o1.getSize();
        var size2 = o2.getSize();
        return !(pos1.x >= pos2.x + size2.x || pos1.x + size1.x <= pos2.x || pos1.z >= pos2.z + size2.x || pos1.z + size1.x <= pos2.z);
    }

    private static <T> T findFirst(Collection<T> list, Function<T, Boolean> filter){
        for(var o : list)
            if(filter.apply(o))
                return o;
        return null;
    }

    public static void prepareRender() {
        // TODO optimizing with chunks

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
                if(pos1.y >= pos2.y + size2.x)
                    return -1;
                else if(pos2.y >= pos1.y + size1.x)
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
}
