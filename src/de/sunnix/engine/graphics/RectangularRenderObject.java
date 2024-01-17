package de.sunnix.engine.graphics;

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

}
