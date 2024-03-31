package de.sunnix.aje.engine.graphics;

import de.sunnix.aje.engine.memory.ContextQueue;
import de.sunnix.aje.engine.memory.MemoryCategory;
import de.sunnix.aje.engine.memory.MemoryHolder;
import lombok.Getter;

import static org.lwjgl.opengl.GL30.*;

public class Mesh extends MemoryHolder {

    private int vertexArray;
    private int elementBuffer;
    @Getter
    private int vertexCount;
    private int vertexMaxCount; // how large is the buffer of openGL
    private final FloatArrayBuffer[] buffers;

    public Mesh(int[] indices, FloatArrayBuffer... buffers) {
        this.buffers = buffers;
        this.vertexMaxCount = this.vertexCount = indices.length;

        ContextQueue.addQueue(() -> {
            vertexArray = glGenVertexArrays();
            glBindVertexArray(vertexArray);

            for (int i = 0; i < buffers.length; i++) {
                buffers[i].init(i);
            }

            elementBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

            unbind();
        });
    }

    public void changeIndices(int[] indices) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        if(indices.length <= vertexMaxCount)
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indices);
        else {
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
            vertexMaxCount = indices.length;
        }
        vertexCount = indices.length;
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void changeBuffer(int location, float[] buffer) {
        buffers[location].changeBuffer(buffer);
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
        for(var v : buffers)
            v.freeMemory();
        glDeleteBuffers(elementBuffer);
        glDeleteVertexArrays(vertexArray);
    }

}
