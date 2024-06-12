package de.sunnix.srpge.engine.graphics;

import de.sunnix.srpge.engine.Core;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class RectangularRenderObject extends RenderObject{

    public RectangularRenderObject() {
        super(genMesh());
    }

    private static Mesh genMesh(){
        return new Mesh(new int[]{
                0, 1, 3,
                1, 2, 3
        }, new FloatArrayBuffer(
                new float[] { // vertices
                        -.5f, 0f, 0f,
                        -.5f, 1f, 0f,
                         .5f, 1f, 0f,
                         .5f, 0f, 0f
                },
                3, false
        ), new FloatArrayBuffer(
                new float[] { // texture
                        0f, 1f,
                        0f, 0f,
                        1f, 0f,
                        1f, 1f
                },
                2, true
        ));
    }

    @Override
    public void render(Vector3f pos, float object_width, float z_Buffer) {
        super.render(pos, object_width, z_Buffer);
    }

    @Override
    public Vector2f getSize() {
        return new Vector2f(Core.TILE_WIDTH);
    }
}
