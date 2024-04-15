package de.sunnix.aje.editor.data;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tile {

    private int[] texID = new int[] { -1, -1 };

    public Tile(){}

    public Tile(DataSaveObject dso) {
        loadTile(dso);
    }

    public void setTexID(int tileset, int index) {
        texID[0] = tileset;
        texID[1] = index;
    }

    public void saveTile(DataSaveObject dso) {
        dso.putArray("texID", texID);
    }

    public void loadTile(DataSaveObject dso){
        texID = dso.getIntArray("texID", 2);
    }
}
