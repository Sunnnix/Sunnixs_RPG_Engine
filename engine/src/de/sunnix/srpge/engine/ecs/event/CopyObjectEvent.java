package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.evaluation.Variables;

public class CopyObjectEvent extends Event {

    protected enum SaveIDType {
        NONE, GLOBAL, LOCAL
    }

    protected ObjectValue object;
    protected SaveIDType saveIDType = SaveIDType.NONE;
    protected int variable;
    protected ObjectValue varObject;

    private GameObject obj, varObj;

    public CopyObjectEvent() {
        super("copy_object");
    }

    @Override
    public void load(DataSaveObject dso) {
        object = new ObjectValue(dso.getObject("obj"));
        saveIDType = SaveIDType.values()[dso.getByte("put_on_var", SaveIDType.NONE.ordinal())];
        if(saveIDType != SaveIDType.NONE) {
            variable = dso.getInt("var", 0);
            if(saveIDType == SaveIDType.LOCAL)
                varObject = new ObjectValue(dso.getObject("var_obj"));
        }
    }

    @Override
    public void prepare(World world, GameObject parent) {
        obj = object.getObject(world, parent);
        if(saveIDType == SaveIDType.LOCAL)
            varObj = varObject.getObject(world, parent);
    }

    @Override
    public void run(World world) {
        if(obj == null)
            return;
        var copy = obj.copy();
        switch (saveIDType){
            case GLOBAL -> Variables.setIntVar(variable, (int) copy.getID());
            case LOCAL -> {
                if(varObj != null)
                    varObj.setVariable(variable, (int) copy.getID());
            }
        }
        world.addEntity(copy);
    }

    @Override
    public boolean isFinished(World world) {
        return true;
    }

    @Override
    public boolean isInstant(World world) {
        return true;
    }
}
