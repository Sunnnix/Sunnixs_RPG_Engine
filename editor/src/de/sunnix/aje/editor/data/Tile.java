package de.sunnix.aje.editor.data;

import de.sunnix.sdso.DataSaveObject;

public class Tile {

    // ts 0 is no texture and equals -1
    // the index allows for a 64x64 tileset to be loaded.
    private short groundTex = -1; // 4 bit tileset (-1 - 14); 12 bit texture (0 - 4095)
    private short[] wallTex = new short[0];

    private byte groundY, wallHeight;

    public Tile(){}

    public Tile(DataSaveObject dso) {
        loadTile(dso);
    }

    public int[] getTexID(){
        return new int[]{ getTileset(), getTexIndex() };
    }

    public int getTileset(){
        return ((groundTex >> 12) & 0xF) - 1;
    }

    public int getTexIndex(){
        return (groundTex & 0xFFF);
    }

    public void setTexID(int tileset, int index) {
        groundTex = (short) (((tileset + 1) << 12) | (index));
    }

    public void setGroundY(int y){
        groundY = (byte) y;
    }

    public int getgroundY(){
        return Byte.toUnsignedInt(groundY);
    }

    public void setWallHeight(int height){
        var arr = new short[height];
        System.arraycopy(wallTex, 0, arr, 0, Math.min(wallTex.length, arr.length));
        wallTex = arr;
        wallHeight = (byte) height;
    }

    public int getWallHeight(){
        return Byte.toUnsignedInt(wallHeight);
    }

    public void setWall(int pos, int tileset, int index){
        if(pos > wallTex.length)
            return;
        wallTex[pos] = (short) (((tileset + 1) << 12) | (index));
    }

    public int getWallTileset(int pos){
        if(pos > wallTex.length)
            return -1;
        return ((wallTex[pos] >> 12) & 0xF) - 1;
    }

    public int getWallTexIndex(int pos){
        if(pos > wallTex.length)
            return 0;
        return (wallTex[pos] & 0xFFF);
    }

    public void saveTile(DataSaveObject dso) {
        dso.putShort("g-tex", groundTex);
        dso.putArray("w-tex", wallTex);
        dso.putShort("height", (short)((groundY << 8) + wallHeight));
    }

    public void loadTile(DataSaveObject dso){
        groundTex = dso.getShort("g-tex", (short) 0);
        wallTex = dso.getShortArray("w-tex", 0);
        var height = dso.getShort("height", (short) 0);
        groundY = (byte)(height >> 8);
        wallHeight = (byte)(height & 0xFF);
    }

}
