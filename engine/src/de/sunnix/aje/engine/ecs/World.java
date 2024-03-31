package de.sunnix.aje.engine.ecs;

import de.sunnix.aje.engine.ecs.components.PhysicComponent;
import de.sunnix.aje.engine.ecs.components.Component;
import de.sunnix.aje.engine.ecs.components.RenderComponent;
import de.sunnix.aje.engine.ecs.systems.RenderSystem;
import de.sunnix.aje.engine.graphics.TestCubeRenderObject;
import lombok.Getter;
import org.lwjgl.opengl.GL11;
import test.Textures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    private TileMap map;

    @Getter
    private GameObject player;

    private boolean inited;

    private TestCubeRenderObject tcro = new TestCubeRenderObject();

    public World(){
        map = new TileMap();
    }

    public void init(){
        if(inited)
            return;
        player = new GameObject(.8f, 1.7f);
        player.addComponent(Component.RENDER);
        player.addComponent(new PhysicComponent());
        player.init();
        RenderComponent.TEXTURE.set(player, Textures.ALUNDRA_WALKING);
        player.setName("Player");


        // ################################################################
        var tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(2, 1, 0);
        tmp.setName("Box 1");

        tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(4, 0, 0);
        tmp.setName("Box 2");

        tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(6, 2, 0);
        tmp.setName("Box 3");


        // ################################################################
        tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(8, 1, -.25);
        tmp.setName("Box 4");

        tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(8, 0, -.5);
        tmp.setName("Box 5");

        tmp = new GameObject(1, 1);
        tmp.addComponent(Component.RENDER);
        tmp.addComponent(new PhysicComponent());
        tmp.init();
        RenderComponent.TEXTURE.set(tmp, Textures.BOX);
        tmp.getPosition().set(8, 2, 0);
        tmp.setName("Box 6");

        for (int i = 0; i < 0; i++) {
            tmp = new GameObject(1, 1);
            tmp.addComponent(Component.RENDER);
            tmp.addComponent(new PhysicComponent());
            tmp.init();
            RenderComponent.TEXTURE.set(tmp, Textures.BOX);
            tmp.getPosition().set((int)(Math.random() * 50), 0, (int)(Math.random() * 50));
            tmp.setName(String.format("Box Gen (%s)", i + 1));
        }

    }

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void update(){
        map.update();
        gameObjects.values().forEach(GameObject::update);
        RenderSystem.prepareRender();
    }

    public void render(){
        map.render();
        RenderSystem.renderObjects();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        tcro.render();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void postUpdate(){
        gameObjectsToAdd.forEach(GameObject::update);
        gameObjectsToAdd.forEach(o -> gameObjects.put(o.getID(), o));
        gameObjectsToAdd.clear();
        gameObjects.values().stream().filter(GameObject::isToDelete).forEach(o -> gameObjects.remove(o.getID()));
    }

    public void onDestroy(){
        map.onDestroy();
    }

}
