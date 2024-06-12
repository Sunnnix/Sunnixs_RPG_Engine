package de.sunnix.srpge.engine.resources;

import de.sunnix.srpge.engine.graphics.TextureAtlas;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class Tileset {

    private String name;
    private TextureAtlas res;

    private int width = 1, height = 1;

    private float[] texturePositions;

    public Tileset(DataSaveObject data) {
        load(data);
    }

    private void load(DataSaveObject data){
        this.name = data.getString("name", null);
        this.res = Resources.get().getTexture(data.getString("res", null));
        this.width = data.getInt("width", 1);
        this.height = data.getInt("height", 1);
        texturePositions = generateTexturePositions();
    }

    protected float[] generateTexturePositions() {
        if(res == null)
            return new float[8];
        float[] positions = new float[width * height * 8];
        float pixelWidth = 1f / res.getWidth();
        float pixelHeight = 1f / res.getHeight();
        float pixelPerTileWidth = 1f / width;
        float pixelPerTileHeight = 1f / height;
        int index = 0;

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                float l = pixelPerTileWidth * x + pixelWidth / 2;
                float r = pixelPerTileWidth * (x + 1) - pixelWidth / 2;
                float t = pixelPerTileHeight * y + pixelHeight / 2;
                float b = pixelPerTileHeight * (y + 1) - pixelHeight / 2;

                positions[index++] = l; // x
                positions[index++] = b; // y
                positions[index++] = l; // x
                positions[index++] = t; // y
                positions[index++] = r; // x
                positions[index++] = t; // y
                positions[index++] = r; // x
                positions[index++] = b; // y
            }
        return positions;
    }

    public float[] getTexturePositions(int id) {
        if(id == -1)
            return new float[]{
                    -1, -1,
                    -1, -1,
                    -1, -1,
                    -1, -1
            };
        if(res == null)
            id = 0;
        id = id % (this.width * this.height); // prevent crash, repeat de.sunnix.game.textures
        return Arrays.copyOfRange(texturePositions, id * 8, id * 8 + 8);
    }

}
