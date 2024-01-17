package de.sunnix.engine.graphics;

import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static org.lwjgl.opengl.GL11.*;

@Getter
@Setter
public abstract class RenderObject extends MemoryHolder {

    @Setter(value = AccessLevel.NONE)
    protected Mesh mesh;
    protected Shader shader;
    protected Texture texture;

    public RenderObject(Mesh mesh){
        this.mesh = mesh;
    }

    public void render(){
        if(shader != null)
            shader.bind();
        if(texture != null)
            texture.bind(0);
        mesh.bind();
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
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
