package de.sunnix.aje.editor.data;

import de.sunnix.sdso.DataSaveObject;

public class Tile {

    private byte tsID = -1;
    private short texID = -1;

    public Tile(){}

    public Tile(DataSaveObject dso) {
        loadTile(dso);
    }

    public int[] getTexID(){
        return new int[]{ tsID, texID };
    }

    public void setTexID(int tileset, int index) {
        tsID = (byte)tileset;
        texID = (short)index;
    }

    public void saveTile(DataSaveObject dso) {
        dso.putByte("tsID", tsID);
        dso.putShort("texID", texID);
    }

    public void loadTile(DataSaveObject dso){
        this.tsID = dso.getByte("tsID", (byte) -1);
        this.texID = dso.getShort("texID", (short) -1);
    }
}
