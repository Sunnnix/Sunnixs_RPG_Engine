package de.sunnix.srpge.engine;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.graphics.*;
import de.sunnix.srpge.engine.memory.MemoryCategory;
import de.sunnix.srpge.engine.memory.MemoryHolder;
import de.sunnix.srpge.engine.resources.Resources;
import lombok.Getter;

import static de.sunnix.srpge.engine.memory.MemoryCategory.PARALLAX;
import static org.lwjgl.opengl.GL11.*;

public class Parallax extends MemoryHolder {

    private static Shader PARALLAX_SHADER;

    protected String textureID;
    @Getter
    protected boolean onTop;
    protected float vSpeed, hSpeed;

    private Mesh mesh;
    private Texture texture;
    private boolean inited;
    private float x, y, movedX, movedY;

    public void load(DataSaveObject dso){
        textureID = dso.getString("tex", null);
        onTop = dso.get("on_top", true);
        var tempo = dso.getFloatArray("tempo", 2);
        vSpeed = tempo[0];
        hSpeed = tempo[1];
    }

    public void init(){
        if(inited)
            return;
        if(PARALLAX_SHADER == null)
            PARALLAX_SHADER = new Shader("/data/shader/parallax_shader");
        texture = Resources.get().getTexture(textureID);
        if(texture == null)
            return;
        inited = true;
        x = Camera.getPos().x + movedX;
        y = Camera.getPos().y + movedY;
        mesh = new Mesh(new int[]{
                0, 1, 3,
                1, 2, 3
        }, new FloatArrayBuffer(new float[]{
                -1f, -1f,
                -1f,  1f,
                 1f,  1f,
                 1f, -1f
        }, 2, false),
                new FloatArrayBuffer(getCurrentTexturePos(), 2, true));
    }

    private float[] getCurrentTexturePos(){
        var iW = (Core.getScreenWidth() / Core.getPixel_scale()) / texture.getWidth();
        var iH = (Core.getScreenHeight() / Core.getPixel_scale()) / texture.getHeight();

        float scalingFactorX = Math.max(1, texture.getHeight() / (float) texture.getWidth());
        float scalingFactorY = Math.max(1, texture.getWidth() / (float) texture.getHeight());

        var x = this.x / (texture.getWidth() / Core.getPixel_scale()) + this.movedX * scalingFactorX;
        var y = this.y / (texture.getHeight() / Core.getPixel_scale()) + this.movedY * scalingFactorY;
        return new float[]{
                x, y + iH,
                x, y,
                x + iW, y,
                x + iW, y + iH
        };
    }

    public void update(){
        if(!inited || texture == null)
            return;
        movedY += vSpeed / 1000;
        movedX += hSpeed / 1000;
        x = Camera.getPos().x / Core.getPixel_scale();
        y = -Camera.getPos().y / Core.getPixel_scale();
        mesh.changeBuffer(1, getCurrentTexturePos());
    }

    public void render(){
        if(texture == null)
            return;
        if(PARALLAX_SHADER.bind())
            PARALLAX_SHADER.uniform4f("globalColoring", Core.getGlobalColoring());
        mesh.bind();
        texture.bind();
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    @Override
    public boolean isValid() {
        return inited && mesh.isValid() && PARALLAX_SHADER.isValid();
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return PARALLAX;
    }

    @Override
    protected String getMemoryInfo() {
        return getClass().getName();
    }

    @Override
    protected void free() {
        if(inited)
            mesh.freeMemory();
    }
}
