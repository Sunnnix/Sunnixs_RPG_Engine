package de.sunnix.srpge.engine.resources;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

import static de.sunnix.srpge.engine.util.FunctionUtils.shortArrayToList;

@Getter
@Setter
public class TilesetPropertie {

    protected boolean blocking;

    protected byte animationTempo = 1;
    protected short animationParent = -1;

    protected ArrayList<Short> animation;

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

    public int getAnimationIndex(int startIndex, long animTime){
        var offset = animation.indexOf((short)(startIndex));
        var index = (int) (((animTime / animationTempo) + offset) % animation.size());
        if(index < 0)
            return startIndex;
        return animation.get(index);
    }

}
