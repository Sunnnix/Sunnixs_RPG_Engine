package de.sunnix.engine.graphics;

import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;

import static org.lwjgl.opengl.GL30.*;

public class Mesh extends MemoryHolder {

    private int vertexArray;
    private int elementBuffer;
    @Getter
    private final int vertexCount;
    private final FloatArrayBuffer[] vertices;

    public Mesh(int[] indices, FloatArrayBuffer... vertices) {
        this.vertices = vertices;
        this.vertexCount = indices.length;

        ContextQueue.addQueue(() -> {
            vertexArray = glGenVertexArrays();
            glBindVertexArray(vertexArray);

            for (int i = 0; i < vertices.length; i++)
                vertices[i].init(i);

            elementBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

            unbind();
        });
    }

    public void bind(){
        glBindVertexArray(vertexArray);
    }

    public void unbind(){
        glBindVertexArray(0);
    }

    @Override
    public boolean isValid() {
        return vertexArray != 0 && elementBuffer != 0;
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.MESH;
    }

    @Override
    protected String getMemoryInfo() {
        return "Mesh";
    }

    @Override
    protected void free() {
        for(var v : vertices)
            v.freeMemory();
        glDeleteBuffers(elementBuffer);
        glDeleteVertexArrays(vertexArray);
    }
}
