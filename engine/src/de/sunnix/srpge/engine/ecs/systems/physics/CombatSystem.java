package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.components.CombatComponent;
import de.sunnix.srpge.engine.ecs.systems.MapGrid;
import de.sunnix.srpge.engine.util.ObjChain;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CombatSystem {

    private static List<GameObject> objects = new ArrayList<>();
    private static MapGrid mapGrid;

    public static void init(int width, int height){
        mapGrid = new MapGrid(width, height);
    }

    public static void add(GameObject go){
        objects.add(go);
        mapGrid.initObject(go);
    }

    public static void remove(GameObject go){
        objects.remove(go);
        mapGrid.removeObject(go);
    }

    public static void update(World world){
        for(var obj: objects)
            obj.getComponent(CombatComponent.class).update(world, obj);
    }

    public static void relocateGridObject(Vector3f oldPos, Vector3f newPos, GameObject object) {
        mapGrid.relocateGridObject(oldPos, newPos, object);
    }

    public static List<GameObject> getCollidingObjects(GameObject go){
        var hb = new ObjChain<>(go.getComponent(CombatComponent.class)).next(CombatComponent::getHitbox).orElse(new AABB(0, 0, 0, 0, 0));
        var objects = mapGrid.getMatchingMapGridObjets(go);
        return objects.stream().filter(o -> o.getComponent(CombatComponent.class).getHitbox().intersects(hb)).toList();
    }

}
