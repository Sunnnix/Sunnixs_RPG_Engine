package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.graphics.TextureAtlas;
import de.sunnix.srpge.engine.registry.Registry;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.data.StringData;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.graphics.TextureRenderObject;
import org.joml.Vector3f;

public class OldRenderComponent extends Component {

    public static final StringData TEXTURE = new StringData("comp_render", null);

    private final TextureRenderObject renderObject = new TextureRenderObject(null);

    @Override
    public void init(World world, GameObject parent) {
        super.init(world, parent);
        RenderSystem.addGO(parent);
    }

    public void render(GameObject go){
        if(isValid()) {
            var key = TEXTURE.get(go);
            if(key == null)
                return;
            var tex = Registry.TEXTURE.get(key);
            renderObject.setTexture(tex);
            if(tex instanceof TextureAtlas ta) {
                renderObject.getMesh().changeBuffer(1, ta.getTexturePositions(0));
                renderObject.getSize().set((float) ta.getWidth() / ta.getTileWidth(), (float) ta.getHeight() / ta.getTileHeight());
            }
            renderObject.render(go.getPosition().mul(1, -1, 1, new Vector3f()), go.size.x, go.getZ_pos());
            if(tex instanceof TextureAtlas) {
                renderObject.getMesh().changeBuffer(1, new float[]{
                        0f, 1f,
                        0f, 0f,
                        1f, 0f,
                        1f, 1f
                });
            }
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
