package de.sunnix.engine.graphics;

import de.sunnix.engine.Core;
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
                        -.5f, -.5f, 0f,
                        -.5f,  .5f, 0f,
                         .5f,  .5f, 0f,
                         .5f, -.5f, 0f
                },
                3
        ), new FloatArrayBuffer(
                new float[] { // texture
                        0f, 1f,
                        0f, 0f,
                        1f, 0f,
                        1f, 1f
                },
                2
        ));
    }

    @Override
    public Vector2f getSize() {
        return new Vector2f(Core.TILE_WIDTH);
    }
}
