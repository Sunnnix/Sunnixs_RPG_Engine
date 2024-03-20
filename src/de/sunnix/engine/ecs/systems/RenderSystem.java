package de.sunnix.engine.ecs.systems;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.ecs.components.Component;

import java.util.ArrayList;

public class RenderSystem {

    private static ArrayList<GameObject> objects = new ArrayList<>();

    public static void renderObjects() {
        objects.forEach(Component.RENDER::render);
    }

    public static void addGO(GameObject go) {
        objects.add(go);
    }
}
