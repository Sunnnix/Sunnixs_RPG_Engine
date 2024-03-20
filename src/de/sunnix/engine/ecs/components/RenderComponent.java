package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.ecs.data.StringData;
import de.sunnix.engine.ecs.systems.RenderSystem;
import de.sunnix.engine.graphics.TextureRenderObject;
import de.sunnix.engine.registry.Registry;
import org.joml.Vector3f;

public class RenderComponent extends Component {

    public static final StringData TEXTURE = new StringData("comp_render", null);

    private final TextureRenderObject renderObject = new TextureRenderObject(null);

    @Override
    public void init(GameObject parent) {
        super.init(parent);
        RenderSystem.addGO(parent);
    }

    public void render(GameObject go){
        if(isValid()) {
            var key = TEXTURE.get(go);
            if(key == null)
                return;
            var tex = Registry.TEXTURE.get(key);
            renderObject.setTexture(tex);
            renderObject.render(go.getPosition().mul(1, -1, 1, new Vector3f()));
        }
    }

    @Override
    public boolean isValid() {
        return renderObject.isValid();
    }

    @Override
    protected void free() {
        renderObject.freeMemory();
    }

}
