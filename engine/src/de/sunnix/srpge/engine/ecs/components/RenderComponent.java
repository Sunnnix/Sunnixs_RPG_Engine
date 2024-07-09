package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.RenderSystem;
import de.sunnix.srpge.engine.graphics.TextureRenderObject;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.resources.Sprite;
import lombok.Setter;
import org.joml.Vector3f;

public class RenderComponent extends Component{

    private String sSprite;
    private Sprite sprite;

    private long animTimer;
    private int animPos = -1;

    @Setter
    private int direction;

    private TextureRenderObject renderObject;

    public RenderComponent(DataSaveObject dso){
        sSprite = dso.getString("sprite", null);
    }

    @Override
    public void init(World world, GameObject parent) {
        super.init(world, parent);
        sprite = Resources.get().getSprite(sSprite);
        if(sprite == null)
            return;
        var tex = sprite.getTexture();
        renderObject = new TextureRenderObject(tex);
        renderObject.getSize().set((float) tex.getWidth() / tex.getTileWidth(), (float) tex.getHeight() / tex.getTileHeight());
        RenderSystem.addGO(parent);
    }

    public void render(GameObject go) {
        if(!isValid() || sprite == null)
            return;
        animTimer++;
        var index = sprite.getTextureIndexForAnimation(animTimer, direction);
        if(index != animPos){
            animPos = index;
            var texture = sprite.getTexture();
            if(texture != null){
                var texPos = texture.getTexturePositions(index);
                renderObject.getMesh().changeBuffer(1, texPos);
            }
        }

        renderObject.render(go.getPosition().mul(1, -1, 1, new Vector3f()), go.size.x, go.getZ_pos());
    }

    @Override
    public boolean isValid() {
        return renderObject != null && renderObject.isValid();
    }
}
