package de.sunnix.engine.graphics;

import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;

import static org.lwjgl.opengl.GL30.*;
public class FloatArrayBuffer extends MemoryHolder {

    @Getter
    private final int size;
    private int id;

    private float[] _array;

    public FloatArrayBuffer(float[] array, int pattern){
        this.size = array.length / pattern;
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
        var pattern = _array.length / size;
        glVertexAttribPointer(location, pattern, GL_FLOAT, false, pattern * Float.BYTES, 0);
        glEnableVertexAttribArray(location);
        _array = null; // free space
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
