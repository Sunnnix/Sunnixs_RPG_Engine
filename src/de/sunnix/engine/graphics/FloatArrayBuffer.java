package de.sunnix.engine.graphics;

import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;

import static org.lwjgl.opengl.GL30.*;
public class FloatArrayBuffer extends MemoryHolder {

    @Getter
    private final int pattern;
    private int id;
    private int latestBufferSize;

    private float[] _array;

    public FloatArrayBuffer(float[] array, int pattern){
        this.pattern = pattern;
        this.latestBufferSize = array.length;
        _array = array;
    }

    /**
     * @param location of shader
     */
    public void init(int location){
        if(isValid())
            throw new RuntimeException("Buffer already initialized!");
        id = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, id);
        glBufferData(GL_ARRAY_BUFFER, _array, GL_STATIC_DRAW);
        glVertexAttribPointer(location, pattern, GL_FLOAT, false, pattern * Float.BYTES, 0);
        glEnableVertexAttribArray(location);
        _array = null; // free space
    }

    public void changeBuffer(float[] buffer) {
        glBindBuffer(GL_ARRAY_BUFFER, id);
        if(buffer.length <= latestBufferSize)
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        else {
            latestBufferSize = buffer.length;
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
    }

    @Override
    public boolean isValid() {
        return id != 0;
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.BUFFER;
    }

    @Override
    protected String getMemoryInfo() {
        return "FloatArrayBuffer";
    }

    @Override
    protected void free() {
        glDeleteBuffers(id);
    }

}
