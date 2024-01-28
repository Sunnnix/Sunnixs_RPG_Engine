package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.graphics.Texture;
import de.sunnix.engine.graphics.TextureRenderObject;

@Component(id = 2383108866725584896L)
public class RenderComponent extends BaseComponent {

    private TextureRenderObject renderObject;

    public RenderComponent(GameObject parent, Texture texture) {
        super(parent);
        this.renderObject = new TextureRenderObject(texture);
    }

    @Override
    public void init() {

    }

    public void render(){
        if(isValid())
            renderObject.render(parent.getPosition());
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
