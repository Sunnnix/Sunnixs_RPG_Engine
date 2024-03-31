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

import static de.sunnix.engine.graphics.gui.GUITextures.*;

@Getter
@Setter
public class NameBoxRenderObject extends MemoryHolder {

    @Setter(value = AccessLevel.NONE)
    protected Mesh mesh;
    protected Shader shader = Shader.DEFAULT_SHADER;
    protected Texture texture;

    @Setter
    private float offsetX = Camera.getSize().x;
    @Getter
    private Vector2f pos;

    public NameBoxRenderObject(TextBoxRenderObject textBox, int size){
        pos = new Vector2f(textBox.getPos()).add(100, textBox.getSize().y);
        texture = size == 0 ? NAME_SCROLL : size == 1 ? NAME_SCROLL_MEDIUM : NAME_SCROLL_LARGE;
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
                2, true
        ));
    }

    public void render(){
        if(texture == null)
            return;
        var size = getSize();
        shader.bind();
        texture.bind(0);
        mesh.bind();
//        var model = new Matrix4f().translate(0, 0, 0).scale(size.x, size.y, 1);
        var model = new Matrix4f().translate(pos.x + offsetX, pos.y, 0).scale(size.x, size.y, 1);
        var proj = Camera.getProjection();
        var mat = proj.mul(model, new Matrix4f());
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        mesh.unbind();
    }

    public Vector2f getSize(){
        var scrollFactor = (float)texture.getHeight() / texture.getWidth();
        var wScaling = (float)texture.getWidth() / SCROLL.getWidth();
        return new Vector2f(Camera.getSize().x * .95f * wScaling, Camera.getSize().x * .95f * scrollFactor * wScaling);
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
