package de.sunnix.srpge.engine.graphics;

import org.joml.Vector2f;

public class AutosizeRenderObject extends RectangularRenderObject {

    public AutosizeRenderObject(Texture texture){
        this.texture = texture;
    }

    @Override
    public Vector2f getSize() {
        return new Vector2f(texture.width, texture.height);
    }
}
