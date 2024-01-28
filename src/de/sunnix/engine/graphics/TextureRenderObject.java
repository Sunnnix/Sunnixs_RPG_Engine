package de.sunnix.engine.graphics;

import org.joml.Vector2f;

public class TextureRenderObject extends RectangularRenderObject{

    public TextureRenderObject(Texture texture){
        setTexture(texture);
        size.set(texture.getWidth(), texture.getHeight());
    }

    private Vector2f size = new Vector2f();

    @Override
    public Vector2f getSize() {
        return size;
    }

}
