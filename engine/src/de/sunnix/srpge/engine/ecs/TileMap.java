package de.sunnix.srpge.engine.ecs;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.ecs.systems.TileAnimationSystem;
import de.sunnix.srpge.engine.ecs.systems.physics.DebugRenderObject;
import de.sunnix.srpge.engine.memory.ContextQueue;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.Shader;
import de.sunnix.sdso.DataSaveObject;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class TileMap {

    public int width;
    public int height;

    private Tile[] tiles;

    private int vertexArray;
    private int verticesID;
    private int textures0ID;
    private int textures1ID;
    private int elementBuffer;

    private int vertexCount;

    private boolean inited;
    private static final Shader shader = new Shader("/data/shader/tile_shader");

    private int bufferSize; // how large is the basic buffer of the Tiles

    public TileMap(DataSaveObject data){
        loadMapFromFile(data);

        ContextQueue.addQueue(() -> {
        vertexArray = glGenVertexArrays();
        glBindVertexArray(vertexArray);

        if(tileset != null) {
            verticesID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, verticesID);
            glBufferData(GL_ARRAY_BUFFER, (bufferSize * 12L) * Float.BYTES, GL_STATIC_DRAW);
            for (var tile : tiles)
                tile.bufferVertices();
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            textures0ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, textures0ID);
            glBufferData(GL_ARRAY_BUFFER, (bufferSize * 8L) * Float.BYTES, GL_DYNAMIC_DRAW);
            for (var tile : tiles)
                tile.bufferTextures0();
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            glEnableVertexAttribArray(1);

            textures1ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, textures1ID);
            glBufferData(GL_ARRAY_BUFFER, (bufferSize * 8L) * Float.BYTES, GL_DYNAMIC_DRAW);
            for (var tile : tiles)
                tile.bufferTextures1();
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            glEnableVertexAttribArray(2);

            elementBuffer = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
            vertexCount = bufferSize * 6;
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, (long) vertexCount * Integer.BYTES, GL_STATIC_DRAW);
            for (var tile : tiles)
                tile.bufferIndices();
        }

        unbind();
        });
        inited = true;
    }

    public String tileset;

    private void loadMapFromFile(DataSaveObject dso) {
        width = dso.getInt("width", 0);
        height = dso.getInt("height", 0);
        var tilesets = dso.getArray("tilesets", String[]::new);
        if(tilesets.length > 0)
            tileset = tilesets[0];
        tiles = new Tile[width * height];
        if(tileset != null) {
            var tilesetData = Resources.get().getTileset(tileset);
            var offset = 0;
            var tileList = dso.<DataSaveObject>getList("tiles");
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++) {
                    var t = new Tile(x, y, offset);
                    offset += t.create(tilesetData, tileList.get(x + y * width));
                    tiles[x + y * width] = t;
                }
            bufferSize = offset;
        }
    }

    public void update(){}

    public Tile getTile(int x, int y){
        if(x < 0 || x >= width || y < 0 || y >= height)
            return null;
        return tiles[x + y * width];
    }

    public void bind(){
        glBindVertexArray(vertexArray);
    }

    public void bindTextures(int layer){
        glBindBuffer(GL_ARRAY_BUFFER, layer == 0 ? textures0ID : textures1ID);
    }

    public void unbind(){
        glBindVertexArray(0);
    }

    public void render(){
        if(!inited)
            return;
        if(tileset == null)
            return;
        var TW = Core.TILE_WIDTH;
        var TH = Core.TILE_HEIGHT;
        shader.bind();
        shader.uniform4f("globalColoring", Core.getGlobalColoring());
        var tsTex = Resources.get().getTilesetTex(tileset);
        if(tsTex == null)
            return;
        tsTex.bind(0);
        bind();
        var model = new Matrix4f().scale(TW, TH, 1).translate(0, .5f, 0);
        var view = Camera.getView();
        var proj = Camera.getProjection();
        var mat = proj.mul(view, new Matrix4f());
        mat.mul(model, mat);
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
    }

    public void onDestroy() {
        glBindVertexArray(vertexArray);
        glDeleteBuffers(verticesID);
        glDeleteBuffers(textures0ID);
        glDeleteBuffers(textures1ID);
        glDeleteBuffers(elementBuffer);
        glDeleteVertexArrays(vertexArray);
    }

    public void drawHitbixes() {
        DebugRenderObject.setColor(0, 1, 0, .25f);
        for(var tile : tiles){
            var hb = tile.getHitbox();
            var dro = tile.getDro();
            dro.prepareRender();
            dro.render(new Vector3f(hb.getX(), hb.getY(), hb.getZ()), new Vector2f(hb.getWidth(), hb.getHeight()));
        }
    }

    public void init() {
        TileAnimationSystem.clear();
        for(var tile:  tiles)
            tile.init();
    }
}
