package de.sunnix.srpge.engine.ecs;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.systems.TileAnimationSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.DebugRenderObject;
import de.sunnix.srpge.engine.resources.Tileset;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.resources.TilesetPropertie;
import lombok.Getter;

import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;
import static org.lwjgl.opengl.GL15.*;

public class Tile {

    public static final byte SLOPE_DIRECTION_NONE = 0;
    public static final byte SLOPE_DIRECTION_SOUTH = 1;
    public static final byte SLOPE_DIRECTION_WEST = 2;
    public static final byte SLOPE_DIRECTION_EAST = 3;
    public static final byte SLOPE_DIRECTION_NORTH = 4;

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

    @Getter
    private int x, y, height, top;
    private int[] indices;
    private float[] vertices;
    private float[] texturesLayer0;
    private float[] texturesLayer1;

    private int bufferOffset;

    @Getter
    private int vertexCount;

    @Getter
    private byte slopeDirection;

    @Getter
    private DebugRenderObject dro;

    @Getter
    private boolean animated;
    private int tex, wallTex[];

    private Tileset tileset;

    public Tile(int x, int y, int bufferOffset){
        this.x = x;
        this.y = y;
        this.bufferOffset = bufferOffset;
    }

    public int create(Tileset tileset, DataSaveObject dso){
        this.tileset = tileset;
        var tex = this.tex = dso.getInt("g-tex", 0);
        var heights = dso.getShort("height", (short) 0);
        slopeDirection = dso.getByte("slope-dir", SLOPE_DIRECTION_NONE);

        var groundY = (byte)(heights >> 8);
        var wallHeight = (byte)(heights & 0xFF);
        height = wallHeight;
        var iOffset = 4 * bufferOffset;
        indices = new int[6 * (wallHeight + 1)];
        vertices = new float[12 * (wallHeight + 1)];
        texturesLayer0 = new float[8 * (wallHeight + 1)];
        texturesLayer1 = new float[8 * (wallHeight + 1)];

        wallTex = dso.getIntArray("w-tex", 0);

        int tex0 = -1, tex1 = -1;

        for (int i = 0; i < height + 1; i++) {
            if(i > 0)
                tex = wallTex[i - 1];

            var ts = getTilesetOf(true, tex);
            if(ts == -1)
                tex0 = -1;
            else
                tex0 = getTexIndexOf(true, tex);
            ts = getTilesetOf(false, tex);
            if(ts == -1)
                tex1 = -1;
            else
                tex1 = getTexIndexOf(false, tex);

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
                vertices[i * 12 + 2] = y + .99f;

                vertices[i * 12 + 3] = -.5f + x;
                vertices[i * 12 + 4] = 0f - y + i - 1;
                vertices[i * 12 + 5] = y + .99f;

                vertices[i * 12 + 6] = .5f + x;
                vertices[i * 12 + 7] = 0f - y + i - 1;
                vertices[i * 12 + 8] = y + .99f;

                vertices[i * 12 + 9] = .5f + x;
                vertices[i * 12 + 10] = -1f - y + i - 1;
                vertices[i * 12 + 11] = y + .99f;
            }
            if(tileset != null) {
                var texArr = tileset.getTexturePositions(tex0);
                System.arraycopy(texArr, 0, texturesLayer0, i * 8, 8);
                texArr = tileset.getTexturePositions(tex1);
                System.arraycopy(texArr, 0, texturesLayer1, i * 8, 8);

                if(!animated) {
                    var prop = tileset.getProperty(tex0);
                    if (prop != null && (prop.getAnimationParent() != -1 || prop.getAnimation() != null))
                        animated = true;
                    else {
                        prop = tileset.getProperty(tex1);
                        if (prop != null && (prop.getAnimationParent() != -1 || prop.getAnimation() != null))
                            animated = true;
                    }
                }
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

        top = groundY;

        if(Core.isDebug()) {
            var hitbox = getHitbox();
            dro = new DebugRenderObject(hitbox.getWidth(), hitbox.getHeight());
        }

        return height + 1;
    }

    public void init(){
        if(animated)
            TileAnimationSystem.addTile(this);
    }

    public int checkAndUpdateAnimation(long animTime) {
        var texArr = new int[2 + wallTex.length * 2];
        for(var i = 0; i <= wallTex.length; i++){
            var tex = i == 0 ? this.tex : wallTex[i - 1];
            int tex0, tex1;
            var ts = getTilesetOf(true, tex);
            if(ts == -1)
                tex0 = -1;
            else
                tex0 = getTexIndexOf(true, tex);
            ts = getTilesetOf(false, tex);
            if(ts == -1)
                tex1 = -1;
            else
                tex1 = getTexIndexOf(false, tex);
            texArr[i * 2] = tex0;
            texArr[i * 2 + 1] = tex1;
        }
        var recalculate = 0;
        for(var i = 0; i < texArr.length; i++){
            var tex = texArr[i];
            var prop = tileset.getProperty(tex);
            if(prop == null || prop.getAnimationParent() == -1 && prop.getAnimation() == null)
                continue;
            TilesetPropertie parent;
            if(prop.getAnimationParent() != -1)
                parent = tileset.getProperty(prop.getAnimationParent());
            else
                parent = prop;

            var preIndex = parent.getAnimationIndex(tex, animTime - 1);
            var newIndex = parent.getAnimationIndex(tex, animTime);
            if(preIndex != newIndex){
                if(i % 2 == 0)
                    recalculate |= 0x1;
                else
                    recalculate |= 0x10;
                texArr[i] = newIndex;
            }
        }
        if(bitcheck(recalculate, 0x1)){
            var textures = new float[8 * texArr.length / 2];
            for(var i = 0; i < texArr.length / 2; i++) {
                var buffer = tileset.getTexturePositions(texArr[i * 2]);
                System.arraycopy(buffer, 0, textures, i * 8, 8);
                texturesLayer0 = textures;
            }
        }
        if(bitcheck(recalculate, 0x10)){
            var textures = new float[8 * texArr.length / 2];
            for(var i = 0; i < texArr.length / 2; i++) {
                var buffer = tileset.getTexturePositions(texArr[i * 2 + 1]);
                System.arraycopy(buffer, 0, textures, i * 8, 8);
                texturesLayer1 = textures;
            }
        }
        return recalculate;
    }

    public AABB.TileAABB getHitbox(){
        return new AABB.TileAABB(x, y, top, slopeDirection);
    }

    public void bufferVertices() {
        if(vertices == null)
            return;
        glBufferSubData(GL_ARRAY_BUFFER, bufferOffset * 12L * Float.BYTES, vertices);
        vertices = null;
    }

    public void bufferTextures0() {
        if(texturesLayer0 == null)
            return;
        glBufferSubData(GL_ARRAY_BUFFER, bufferOffset * 8L * Float.BYTES, texturesLayer0);
        texturesLayer0 = null;
    }

    public void bufferTextures1() {
        if(texturesLayer1 == null)
            return;
        glBufferSubData(GL_ARRAY_BUFFER, bufferOffset * 8L * Float.BYTES, texturesLayer1);
        texturesLayer1 = null;
    }

    public void bufferIndices() {
        if(indices == null)
            return;
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, bufferOffset * 6L * Integer.BYTES, indices);
        indices = null;
    }

    public TilesetPropertie[] getWallProperties(int minY, int maxY){
        if(maxY < minY)
            throw new RuntimeException(String.format("maxY smaller then minY (%s, %s)", maxY, minY));
        if(minY >= wallTex.length)
            return new TilesetPropertie[0];
        if(maxY >= wallTex.length)
            maxY = wallTex.length - 1;
        var properties = new TilesetPropertie[(maxY - minY) * 2];
        for(var i = 0; i < (maxY - minY); i++){
            var y = minY + i;
            properties[i * 2] = tileset.getProperty(getTexIndexOf(true, wallTex[y]));
            properties[i * 2 + 1] = tileset.getProperty(getTexIndexOf(false, wallTex[y]));
        }
        return properties;
    }

}
