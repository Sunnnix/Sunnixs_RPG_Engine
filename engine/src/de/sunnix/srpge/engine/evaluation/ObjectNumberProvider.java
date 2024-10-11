package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.World;

public class ObjectNumberProvider extends ValueProvider<Number>{

    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;
    public static final int OBJ_ID = 3;
    public static final int OBJ_FACE = 4;

    private int objectID;
    private int providerType = POS_X;

    public ObjectNumberProvider() {
        super("object_num", 0);
    }

    @Override
    public Number getValue(World world) {
        var object = world.getGameObject(objectID);
        if(object == null)
            return defaultValue;
        return switch (providerType) {
            case POS_X -> object.getPosition().x;
            case POS_Y -> object.getPosition().y;
            case POS_Z -> object.getPosition().z;
            case OBJ_ID -> objectID;
            case OBJ_FACE -> object.getFacing().ordinal();
            default -> 0;
        };
    }

    @Override
    public void load(DataSaveObject dso) {
        this.objectID = dso.getInt("object", -1);
        this.providerType = dso.getInt("providerType", POS_X);
    }

}
