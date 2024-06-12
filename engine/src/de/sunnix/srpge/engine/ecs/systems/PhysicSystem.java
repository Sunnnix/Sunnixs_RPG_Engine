package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.ecs.GameObject;

import java.util.ArrayList;
import java.util.List;

public class PhysicSystem {

    private static List<GameObject> objects = new ArrayList<>();

    public static void add(GameObject go){
        objects.add(go);
    }

}
