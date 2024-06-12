package de.sunnix.srpge.engine.memory;

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

    @Override
    public String toString() {
        return String.format("%s%s - %s -- (%s)", isValid() ? "" : "INVALID ", getMemoryInfo(), getMemoryCategory(), super.toString());
    }
}
