package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.evaluation.Variables;
import de.sunnix.srpge.engine.util.ObjChain;

public class ObjectValue implements Cloneable{

    private enum Type {
        ID, GLOBAL_VAR, LOCAL_VAR
    }

    private final Type type;

    private final int object, index;

    public ObjectValue(DataSaveObject dso){
        if(dso == null)
            dso = new DataSaveObject();
        type = Type.values()[dso.getByte("t", (byte) Type.ID.ordinal())];
        object = dso.getInt("o", -1);
        index = dso.getInt("i", 0);
    }

    public GameObject getObject(World world, GameObject parent){
        return world.getGameObject(switch (type){
            case ID -> object == -1 ? parent.getID() : object;
            case GLOBAL_VAR -> Variables.getInt(index);
            case LOCAL_VAR -> new ObjChain<>(object == -1 ? parent : world.getGameObject(object)).next(obj -> obj.getVariable(index)).get();
        });
    }

    @Override
    public ObjectValue clone() throws CloneNotSupportedException {
        return (ObjectValue) super.clone();
    }
}
