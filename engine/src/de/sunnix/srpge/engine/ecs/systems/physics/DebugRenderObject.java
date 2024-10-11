package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.graphics.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class DebugRenderObject {

    private static final Shader DEBUG = new Shader("/data/shader/debug_shader");
    private Mesh mesh;

    public DebugRenderObject(float width, float height) {
        var hRatio = height / (width + height);
        this.mesh = new Mesh(
                new int[]{
                        0, 1, 3,
                        1, 2, 3,

                        4, 5, 7,
                        5, 6, 7
                }, new FloatArrayBuffer(
                new float[] {
                        -.5f, hRatio, 0,
                        -.5f, 1, 0,
                        .5f, 1, 0,
                        .5f, hRatio, 0,

                        -.5f, 0f, 1,
                        -.5f, hRatio, 1,
                        .5f, hRatio, 1,
                        .5f, 0f, 1
                },
                3, false
        )
        );
    }

    public static void setColor(float r, float g, float b, float a){
        DEBUG.bind();
        DEBUG.uniform4f("color", r, g, b, a);
    }

    public void prepareRender(){
        DEBUG.bind();
        mesh.bind();
    }

    public void render(Vector3f pos, Vector2f size) {
        if(!mesh.isValid())
            return;
        var TW = Core.TILE_WIDTH;
        var TH = Core.TILE_HEIGHT;
        var model = new Matrix4f().translate(pos.x * TW, (-pos.z + pos.y - size.x / 2) * TH, 0).scale(size.x * TW, (size.y + size.x) * TH, 1);
        var view = Camera.getView();
        var proj = Camera.getProjection();
        var mat = proj.mul(view, new Matrix4f());
        mat.mul(model, mat);
        DEBUG.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    public void freeMemory(){
        mesh.freeMemory();
    }

}
