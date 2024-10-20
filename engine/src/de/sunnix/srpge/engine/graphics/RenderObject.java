package de.sunnix.srpge.engine.graphics;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

@Getter
@Setter
public abstract class RenderObject extends MemoryHolder {

    @Setter(value = AccessLevel.NONE)
    protected Mesh mesh;
    protected Shader shader = Shader.DEFAULT_SHADER;
    protected Texture texture;

    public RenderObject(Mesh mesh){
        this.mesh = mesh;
    }

    public void render(Vector3f pos, float object_width, float z_Buffer){
        var size = getSize();
        if(shader.bind())
            shader.uniform4f("globalColoring", Core.getGlobalColoring());
        if(texture == null)
            Texture.unbind();
        else
            texture.bind(0);
        mesh.bind();
        var model = new Matrix4f().translate(pos.x * Core.TILE_WIDTH, (-pos.z - pos.y - object_width / 2) * Core.TILE_HEIGHT, z_Buffer).scale(size.x, size.y, 1);
        var view = Camera.getView();
        var proj = Camera.getProjection();
        var mat = proj.mul(view, new Matrix4f());
        mat.mul(model, mat);
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    public abstract Vector2f getSize();

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
