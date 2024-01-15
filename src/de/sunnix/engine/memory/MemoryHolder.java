package de.sunnix.engine.memory;

/**
 * The core class for handling memory
 */
public abstract class MemoryHolder {

    public MemoryHolder(){
        MemoryHandler.create(getMemoryCategory(), getMemoryInfo(), this);
    }

    public final void freeMemory(){
        free();
        MemoryHandler.remove(this);
    }

    public abstract boolean isValid();

    protected abstract MemoryCategory getMemoryCategory();

    protected abstract String getMemoryInfo();

    protected abstract void free();

}
