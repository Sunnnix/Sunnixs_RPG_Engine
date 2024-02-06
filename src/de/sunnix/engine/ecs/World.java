package de.sunnix.engine.ecs;

import de.sunnix.engine.Core;
import de.sunnix.engine.ecs.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

public class World {

    private List<GameObject> gameObjects = new ArrayList<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    public World(){}

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void update(){
        gameObjects.forEach(GameObject::update);
    }

    public void render(){
        RenderSystem.renderObjects();
    }

    public void postUpdate(){
        gameObjectsToAdd.forEach(GameObject::update);
        gameObjects.addAll(gameObjectsToAdd);
        gameObjectsToAdd.clear();
        gameObjects.removeIf(GameObject::isToDelete);
    }

    public void onDestroy(){
    }

}