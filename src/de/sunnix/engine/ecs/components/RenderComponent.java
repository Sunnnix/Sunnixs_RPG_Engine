package de.sunnix.engine.ecs.components;

import de.sunnix.engine.ecs.GameObject;
import de.sunnix.engine.graphics.Texture;
import de.sunnix.engine.graphics.TextureRenderObject;

public class RenderComponent extends Component {

    private final TextureRenderObject renderObject = new TextureRenderObject(null);

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
