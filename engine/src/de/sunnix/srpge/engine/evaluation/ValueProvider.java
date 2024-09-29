package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;
import lombok.Getter;

public abstract class ValueProvider<T> {

    @Getter
    public final String ID;
    public final T defaultValue;

    public ValueProvider(String id, T defaultValue) {
        this.ID = id;
        this.defaultValue = defaultValue;
    }

    public abstract T getValue(World world);

    public abstract void load(DataSaveObject dso);

}
