package de.sunnix.aje.engine.ecs;

import de.sunnix.aje.engine.graphics.TextureAtlas;
import de.sunnix.aje.engine.resources.Tileset;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import test.Textures;

import static org.lwjgl.opengl.GL15.*;

public class Tile {
    private int x, y, height;
    private int[] indices;
    private float[] vertices;
    private float[] textures;

    private int bufferOffset;

    @Getter
    private int vertexCount;

    public Tile(int x, int y, int bufferOffset){
        this.x = x;
        this.y = y;
        this.bufferOffset = bufferOffset;
    }

    public int create(Tileset tileset, DataSaveObject dso){
        var texID = dso.getShort("g-tex", (short) -1) & 0xFFF;
        var heights = dso.getShort("height", (short) 0);
        var groundY = (byte)(heights >> 8);
        var wallHeight = (byte)(heights & 0xFF);
        height = wallHeight;
        var iOffset = 4 * bufferOffset;
        indices = new int[6 * (wallHeight + 1)];
        vertices = new float[12 * (wallHeight + 1)];
        textures = new float[8 * (wallHeight + 1)];

        var wallTex = dso.getShortArray("w-tex", 0);

        for (int i = 0; i < height + 1; i++) {
            if(i > 0){
                var ts = ((wallTex[i - 1] >> 12) & 0xF) - 1;
                if(ts == -1)
                    continue;
                texID = wallTex[i - 1] & 0xFFF;
            }
            indices[i * 6] = iOffset;
            indices[i * 6 + 1] = iOffset + 1;
            indices[i * 6 + 2] = iOffset + 3;
            indices[i * 6 + 3] = iOffset + 1;
            indices[i * 6 + 4] = iOffset + 2;
            indices[i * 6 + 5] = iOffset + 3;

            iOffset += 4;

            if(i == 0) {
                vertices[0] = -.5f + x;
                vertices[1] = -1f - y + groundY;
                vertices[2] = y;

                vertices[3] = -.5f + x;
                vertices[4] = 0f - y + groundY;
                vertices[5] = y;

                vertices[6] = .5f + x;
                vertices[7] = 0f - y + groundY;
                vertices[8] = y;

                vertices[9] = .5f + x;
                vertices[10] = -1f - y + groundY;
                vertices[11] = y;
            } else {
                vertices[i * 12] = -.5f + x;
                vertices[i * 12 + 1] = -1f - y + i - 1;
                vertices[i * 12 + 2] = y + 1;

                vertices[i * 12 + 3] = -.5f + x;
                vertices[i * 12 + 4] = 0f - y + i - 1;
                vertices[i * 12 + 5] = y + 1;

                vertices[i * 12 + 6] = .5f + x;
                vertices[i * 12 + 7] = 0f - y + i - 1;
                vertices[i * 12 + 8] = y + 1;

                vertices[i * 12 + 9] = .5f + x;
                vertices[i * 12 + 10] = -1f - y + i - 1;
                vertices[i * 12 + 11] = y + 1;
            }
            if(tileset != null) {
                var tex = tileset.getTexturePositions(texID);
                System.arraycopy(tex, 0, textures, i * 8, 8);
            }
        }

        var s = new StringBuilder("Tile (" + x + ", " + y + ")\n");
        for (int i = 0; i < vertices.length / 12; i++){
            for (int j = 0; j < 12; j += 3) {
                s.append(String.format("%s, %s, %s\n", vertices[i * 12 + j], vertices[i * 12 + j + 1], vertices[i * 12 + j + 2]));
            }
            s.append("--------------------------------\n");
        }
        s.append("================================");

//        System.out.println(s);

//        vertices = new float[] { // vertices
//                -.5f + x, -1f - y + height, y + 1,
//                -.5f + x,  0f - y + height, y + 1,
//                .5f + x,  0f - y + height, y + 1,
//                .5f + x, -1f - y + height, y + 1
//        };

//        de.sunnix.game.textures = Textures.TILESET_INOA.getTexturePositions((int)(Math.random() * 4) + (Textures.TILESET_INOA.getTileWidth() * (int)(Math.random() * 4)));

//        de.sunnix.game.textures = Textures.TILESET_INOA.getTexturePositions(x + y * Textures.TILESET_INOA.getTileWidth());
        this.vertexCount = indices.length;
        return height + 1;
    }

    public void bufferVertices() {
        glBufferSubData(GL_ARRAY_BUFFER, bufferOffset * 12L * Float.BYTES, vertices);
    }

    public void bufferTextures() {
        glBufferSubData(GL_ARRAY_BUFFER, bufferOffset * 8L * Float.BYTES, textures);
    }

    public void bufferIndices() {
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, bufferOffset * 6L * Integer.BYTES, indices);
    }
}
