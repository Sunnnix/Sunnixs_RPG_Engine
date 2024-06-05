package de.sunnix.aje.engine.ecs;

import de.sunnix.aje.engine.ecs.components.PhysicComponent;
import de.sunnix.aje.engine.ecs.components.Component;
import de.sunnix.aje.engine.ecs.components.RenderComponent;
import de.sunnix.aje.engine.ecs.systems.RenderSystem;
import de.sunnix.aje.engine.graphics.TestCubeRenderObject;
import de.sunnix.aje.engine.util.BetterJSONObject;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import org.lwjgl.opengl.GL11;
import test.Textures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class World {

    private Map<Long, GameObject> gameObjects = new HashMap<>();
    private List<GameObject> gameObjectsToAdd = new ArrayList<>();

    private TileMap map;

    @Getter
    private GameObject player;

    private boolean inited;

    private TestCubeRenderObject tcro = new TestCubeRenderObject();

    public void init(ZipFile zip, BetterJSONObject config) throws IOException {
        if(inited)
            return;
        var startMapID = config.get("start_map", -1);
        if(startMapID == -1)
            throw new RuntimeException("Invalid start map id: " + startMapID);
        var mapDSO = new DataSaveObject().load(zip.getInputStream(new ZipEntry(String.format("maps\\%04d.map", startMapID))));
        map = new TileMap(mapDSO);

        // Player
        player = new GameObject(.8f, 1.7f);
        player.addComponent(Component.RENDER);
        player.addComponent(new PhysicComponent());
        player.init();
        RenderComponent.TEXTURE.set(player, Textures.ALUNDRA_WALKING);
        player.setName("Player");

        // Load objects
        mapDSO.<DataSaveObject>getList("objects").stream().map(o -> {
            var object = new GameObject(o);
            object.addComponent(Component.RENDER);
            object.addComponent(new PhysicComponent());
            object.init();
            RenderComponent.TEXTURE.set(object, Textures.BOX);
            return object;
        }).forEach(o -> gameObjects.put(o.getID(), o));

        // ################################################################
//        for (int i = 0; i < 200; i++) {
//            tmp = new GameObject(1, 1);
//            tmp.addComponent(Component.RENDER);
//            tmp.addComponent(new PhysicComponent());
//            tmp.init();
//            RenderComponent.TEXTURE.set(tmp, Textures.BOX);
//            tmp.getPosition().set((int)(Math.random() * 50), 0, (int)(Math.random() * 50));
//            tmp.setName(String.format("Box Gen (%s)", i + 1));
//        }

    }

    public void addEntity(GameObject entity){
        gameObjectsToAdd.add(entity);
    }

    public void update(){
        map.update();
        gameObjects.values().forEach(go -> go.update(this));
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
//        gameObjectsToAdd.forEach(GameObject::update);
        gameObjectsToAdd.forEach(o -> gameObjects.put(o.getID(), o));
        gameObjectsToAdd.clear();
        gameObjects.values().stream().filter(GameObject::isToDelete).forEach(o -> gameObjects.remove(o.getID()));
    }

    public GameObject getGameObject(long id){
        return gameObjects.get(id);
    }

    public void onDestroy(){
        map.onDestroy();
    }

}
