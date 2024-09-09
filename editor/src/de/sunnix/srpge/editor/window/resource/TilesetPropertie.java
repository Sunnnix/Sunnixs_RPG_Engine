package de.sunnix.srpge.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import static de.sunnix.srpge.editor.util.FunctionUtils.shortArrayToList;
import static de.sunnix.srpge.editor.util.FunctionUtils.shortListToArray;

@Getter
@Setter
public class TilesetPropertie {

    private boolean blocking;

    private byte animationTempo = 1;
    private short animationParent = -1;

    private ArrayList<Short> animation;

    public TilesetPropertie(){}

    public TilesetPropertie(DataSaveObject data) {
        load(data);
    }

    private void load(DataSaveObject data){
        blocking = data.getBool("blocking", true);
        animationTempo = data.getByte("anim_tempo", (byte) 1);
        animationParent = data.getShort("anim_parent", (short) -1);
        var anims = data.getShortArray("animation", 0);
        animation = anims.length > 0 ? shortArrayToList(anims) : null;
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
