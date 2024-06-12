package de.sunnix.srpge.engine.graphics;

import lombok.Getter;

import java.io.InputStream;
import java.util.Arrays;

public class TextureAtlas extends Texture{

    @Getter
    private final int tileWidth, tileHeight;

    protected final float[] texturePositions;

    /**
     * @param data raw data of image as stream
     * @param width tile width
     * @param height tile height
     */
    public TextureAtlas(InputStream data, int width, int height) {
        super(data);
        this.tileWidth = width;
        this.tileHeight = height;
        this.texturePositions = generateTexturePositions();
    }

    /**
     * @param path texture path
     * @param width tile width
     * @param height tile height
     */
    public TextureAtlas(String path, int width, int height) {
        super(path);
        this.tileWidth = width;
        this.tileHeight = height;
        this.texturePositions = generateTexturePositions();
    }

    protected float[] generateTexturePositions() {
        float[] positions = new float[tileWidth * tileHeight * 8];
        float pixelWidth = 1f / width;
        float pixelHeight = 1f / height;
        float pixelPerTileWidth = 1f / tileWidth;
        float pixelPerTileHeight = 1f / tileHeight;
        int index = 0;

        for (int y = 0; y < tileHeight; y++)
            for (int x = 0; x < tileWidth; x++) {
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
        id = id % (this.tileWidth * this.tileHeight); // prevent crash, repeat de.sunnix.game.textures
        return Arrays.copyOfRange(texturePositions, id * 8, id * 8 + 8);
    }

}
