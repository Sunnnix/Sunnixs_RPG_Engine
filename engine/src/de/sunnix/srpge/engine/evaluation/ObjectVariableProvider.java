package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;

public class ObjectVariableProvider extends ValueProvider<Number>{

    protected int objectID = -1;
    protected int index;

    public ObjectVariableProvider() {
        super("loc_var", 0);
    }

    @Override
    public Number getValue(World world) {
        var object = world.getGameObject(objectID);
        if(object == null)
            return 0;
        return object.getVariable(index);
    }

    @Override
    public void load(DataSaveObject dso) {
        objectID = dso.getInt("object", -1);
        index = dso.getInt("index", 0);
    }

}
