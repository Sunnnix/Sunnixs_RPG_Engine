package de.sunnix.engine.ecs;

import de.sunnix.engine.Core;
import de.sunnix.engine.ecs.systems.RenderSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    public World(){}

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void update(){
        gameObjects.values().forEach(GameObject::update);
    }

    public void render(){
        RenderSystem.renderObjects();
    }

    public void postUpdate(){
        gameObjectsToAdd.forEach(GameObject::update);
        gameObjectsToAdd.forEach(o -> gameObjects.put(o.getID(), o));
        gameObjectsToAdd.clear();
        gameObjects.values().stream().filter(GameObject::isToDelete).forEach(o -> gameObjects.remove(o.getID()));
    }

    public void onDestroy(){
    }

}
