package de.sunnix.srpge.editor.data;

import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

import static de.sunnix.srpge.engine.ecs.Tile.SLOPE_DIRECTION_NONE;

public class Tile implements Cloneable{

    private static short getLayer(int layer, int data){
        if(layer == 0)
            return (short)(data & 0xFFFF);
        else
            return (short)(data >> 16);
    }

    private static int getTexIndexOf(boolean layer0, int data){
        data = getLayer(layer0 ? 0 : 1, data);
        return data & 0xFFF;
    }

    private static int getTilesetOf(boolean layer0, int data){
        data = getLayer(layer0 ? 0 : 1, data);
        return ((data >> 12) & 0xF) - 1;
    }

    private static int clearLayer(int layer, int data){
        return data & (layer == 0 ? 0xFFFF0000 : 0x0000FFFF);
    }

    private static int setTex(int layer, int data, int tileset, int index){
        var value = (short) (((tileset + 1) << 12) | (index)) << (layer == 0 ? 0 : 16);
        return clearLayer(layer, data) | value;
    }

    // ts 0 is no texture and equals -1
    // the index allows for a 64x64 tileset to be loaded.
    private int groundTex = 0; // 2 textures a 16 bit | 4 bit tileset (-1 - 14); 12 bit texture (0 - 4095)
    private int[] wallTex = new int[0];

    private byte groundY, wallHeight;

    @Getter
    @Setter
    private boolean blocking;

    @Getter
    private byte slopeDirection = SLOPE_DIRECTION_NONE;

    public Tile(){}

    public Tile(DataSaveObject dso, int[] version) {
        loadTile(dso, version);
    }

    public int[] getGroundTex(){
        return new int[] { getTilesetOf(true, groundTex), getTexIndexOf(true, groundTex), getTilesetOf(false, groundTex), getTexIndexOf(false, groundTex) };
    }

    public int[] getWallTex(int wall){
        var wallData = wallTex[wall];
        return new int[] { getTilesetOf(true, wallData), getTexIndexOf(true, wallData), getTilesetOf(false, wallData), getTexIndexOf(false, wallData) };
    }

    public void setGroundTex(int layer, int tileset, int index) {
        groundTex = setTex(layer, groundTex, tileset, index);
    }

    public void setWallTex(int wall, int layer, int tileset, int index){
        if(wall > wallTex.length)
            return;
        wallTex[wall] = setTex(layer, wallTex[wall], tileset, index);
    }

    public void setGroundY(int y){
        groundY = (byte) y;
    }

    public int getgroundY(){
        return Byte.toUnsignedInt(groundY);
    }

    public void setWallHeight(int height){
        var arr = new int[height];
        System.arraycopy(wallTex, 0, arr, 0, Math.min(wallTex.length, arr.length));
        wallTex = arr;
        wallHeight = (byte) height;
    }

    public int getWallHeight(){
        return Byte.toUnsignedInt(wallHeight);
    }

    public void saveTile(DataSaveObject dso) {
        dso.putInt("g-tex", groundTex);
        dso.putArray("w-tex", wallTex);
        dso.putShort("height", (short)((groundY << 8) + wallHeight));

        dso.putBool("blocking", blocking);

        dso.putByte("slope-dir", slopeDirection);
    }

    public void loadTile(DataSaveObject dso, int[] version){
        if(version[1] < 4){
                loadTile_v0_3(dso);
        } else {
            groundTex = dso.getInt("g-tex", 0);
            wallTex = dso.getIntArray("w-tex", 0);
        }
        var height = dso.getShort("height", (short) 0);
        groundY = (byte)(height >> 8);
        wallHeight = (byte)(height & 0xFF);

        blocking = dso.getBool("blocking", false);

        slopeDirection = dso.getByte("slope-dir", SLOPE_DIRECTION_NONE);
    }

    private void loadTile_v0_3(DataSaveObject dso){
        groundTex = dso.getShort("g-tex", (short) 0);
        var shortArr = dso.getShortArray("w-tex", 0);
        wallTex = new int[shortArr.length];
        for (int i = 0; i < shortArr.length; i++)
            wallTex[i] = shortArr[i];
    }

    public void setSlopeDirection(int slope) {
        this.slopeDirection = (byte) slope;
    }

    @Override
    public Tile clone() {
        try {
            var clone = (Tile) super.clone();
            clone.wallTex = Arrays.copyOf(clone.wallTex, clone.wallTex.length);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
