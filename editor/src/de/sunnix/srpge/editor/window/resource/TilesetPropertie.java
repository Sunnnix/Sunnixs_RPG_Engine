package de.sunnix.srpge.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import static de.sunnix.srpge.engine.util.FunctionUtils.shortArrayToList;
import static de.sunnix.srpge.engine.util.FunctionUtils.shortListToArray;

@Getter
@Setter
public class TilesetPropertie extends de.sunnix.srpge.engine.resources.TilesetPropertie {

    public TilesetPropertie(){
        super(new DataSaveObject());
    }

    public TilesetPropertie(DataSaveObject data) {
        super(data);
    }

    public DataSaveObject save(DataSaveObject data){
        data.putBool("blocking", blocking);
        if(animationParent != -1)
            data.putShort("anim_parent", animationParent);
        if(animation != null) {
            data.putArray("animation", shortListToArray(animation));
            data.putByte("anim_tempo", animationTempo);
        }
        return data;
    }

    public void addAnimation(int child) {
        if(animation == null)
            animation = new ArrayList<>();
        if(!animation.contains((short) child))
            animation.add((short) child);
    }

    public void removeAnimation(int child) {
        if(animation == null)
            return;
        animation.remove(Short.valueOf((short) child));
        if(animation.size() == 1 || animation.isEmpty())
            animation = null;
    }

    public void setAnimationParent(int animationParent) {
        this.animationParent = (short) animationParent;
    }

}
