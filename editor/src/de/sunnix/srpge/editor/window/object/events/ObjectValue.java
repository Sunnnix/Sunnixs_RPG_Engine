package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ObjectValue implements Cloneable{

    public enum Type {
        ID, GLOBAL_VAR, LOCAL_VAR
    }

    protected Type type = Type.ID;

    protected int object = -1, index;

    public ObjectValue(){}

    public ObjectValue(DataSaveObject dso){
        load(dso);
    }

    public void load(DataSaveObject dso){
        if(dso == null)
            dso = new DataSaveObject();
        type = Type.values()[dso.getByte("t", (byte) Type.ID.ordinal())];
        object = dso.getInt("o", -1);
        index = dso.getInt("i", 0);
    }

    public DataSaveObject save(){
        return new DataSaveObject()
                .putByte("t", (byte) type.ordinal())
                .putInt("o", object)
                .putInt("i", index);
    }

    public void setObject(int object){
        this.type = Type.ID;
        this.object = object;
    }

    public void setGlobalVar(int index){
        this.type = Type.GLOBAL_VAR;
        this.index = index;
    }

    public void setLocalVar(int object, int index){
        this.type = Type.LOCAL_VAR;
        this.object = object;
        this.index = index;
    }

    @Override
    public ObjectValue clone() {
        try {
            return (ObjectValue) super.clone();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getText(Window window, MapData map){
        return switch (type){
            case ID, LOCAL_VAR -> {
                var s = "";
                if(type == Type.LOCAL_VAR)
                    s += "Object of local variable [" + index + "] of ";
                s += Objects.toString(object == -1 ? "Self" : object == 999 ? window.getPlayer() : map.getObject(object));
                yield s;
            }
            case GLOBAL_VAR -> "Object of global variable [" + index + "]";
        };
    }

}
