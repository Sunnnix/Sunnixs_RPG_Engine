package de.sunnix.engine.graphics.gui;

import de.sunnix.engine.graphics.*;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;

@Getter
@Setter
public class TextBoxRenderObject extends MemoryHolder {

    @Setter(value = AccessLevel.NONE)
    protected Mesh mesh;
    protected Shader shader = Shader.DEFAULT_SHADER;
    protected Texture texture = GUITextures.SCROLL;

    @Setter
    private float offsetY = getSize().y * 1.5f;
    @Getter
    private Vector2f pos = new Vector2f(Camera.getSize().x / 2 - getSize().x / 2, Camera.getSize().y * .05f);

    public TextBoxRenderObject(){
        this.mesh = new Mesh(new int[]{
                0, 1, 3,
                1, 2, 3
        }, new FloatArrayBuffer(
                new float[] { // vertices
                        0f, 0f, 0f,
                        0f, 1f, 0f,
                        1f, 1f, 0f,
                        1f, 0f, 0f
                },
                3, false
        ), new FloatArrayBuffer(
                new float[] { // texture
                        0f, 1f,
                        0f, 0f,
                        1f, 0f,
                        1f, 1f
                },
                2, false
        ));
    }

    public void render(){
        if(texture == null)
            return;
        var size = getSize();
        shader.bind();
        texture.bind(0);
        mesh.bind();
//        var model = new Matrix4f().translate(Camera.getSize().x / 2 - size.x / 2, Camera.getSize().y * .05f - offsetX, 0).scale(size.x, size.y, 1);
        var model = new Matrix4f().translate(pos.x, pos.y - offsetY, 0).scale(size.x, size.y, 1);
        var proj = Camera.getProjection();
        var mat = proj.mul(model, new Matrix4f());
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        mesh.unbind();
    }

    public Vector2f getSize(){
        var scrollFactor = (float)texture.getHeight() / texture.getWidth();
        return new Vector2f(Camera.getSize().x * .95f, Camera.getSize().x * .95f * scrollFactor);
    }

    @Override
    public boolean isValid() {
        return mesh.isValid();
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.RENDER_OBJECT;
    }

    @Override
    protected String getMemoryInfo() {
        return "RenderObject";
    }

    @Override
    protected void free() {
        mesh.freeMemory();
    }

}
