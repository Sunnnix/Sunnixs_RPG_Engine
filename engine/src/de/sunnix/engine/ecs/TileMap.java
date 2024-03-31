package de.sunnix.engine.ecs;

import de.sunnix.engine.graphics.Camera;
import de.sunnix.engine.graphics.Shader;
import de.sunnix.engine.graphics.TextureAtlas;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import test.Textures;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class TileMap {

    public final int width;
    public final int height;

    private final Tile[] tiles;

    private int vertexArray;
    private int verticesID;
    private int texturesID;
    private int elementBuffer;

    private int vertexCount;

    private boolean inited;

    private final TextureAtlas texture = Textures.TILESET_INOA;
    private final Shader shader = Shader.DEFAULT_SHADER;

    private final int bufferSize; // how large is the basic buffer of the Tiles

    public TileMap(){
        width = 150;
        height = 150;
        tiles = new Tile[width * height];
        var offset = 0;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
//                tiles[x + y * width] = new Tile(x, y, x + y * width);
                var t = new Tile(x, y, offset);
                offset += t.create();
                tiles[x + y * width] = t;
            }
        bufferSize = offset;

        vertexArray = glGenVertexArrays();
        glBindVertexArray(vertexArray);

        verticesID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesID);
        glBufferData(GL_ARRAY_BUFFER, (bufferSize * 12L) * Float.BYTES, GL_STATIC_DRAW);
        for(var tile : tiles)
            tile.bufferVertices();
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        texturesID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, texturesID);
        glBufferData(GL_ARRAY_BUFFER, (bufferSize * 8L) * Float.BYTES, GL_DYNAMIC_DRAW);
        for(var tile : tiles)
            tile.bufferTextures();
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);

        elementBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        vertexCount = bufferSize * 6;
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (long) vertexCount * Integer.BYTES, GL_STATIC_DRAW);
        for(var tile : tiles)
            tile.bufferIndices();

        unbind();
        inited = true;
    }

    public void update(){}

    public void bind(){
        glBindVertexArray(vertexArray);
    }

    public void unbind(){
        glBindVertexArray(0);
    }

    public void render(){
        if(!inited)
            return;
        if(texture == null)
            return;
        var size = new Vector2f(24, 16);
        shader.bind();
        texture.bind(0);
        bind();
        var model = new Matrix4f().scale(size.x, size.y, 1);
        var view = Camera.getView();
        var proj = Camera.getProjection();
        var mat = proj.mul(view, new Matrix4f());
        mat.mul(model, mat);
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
    }

}
