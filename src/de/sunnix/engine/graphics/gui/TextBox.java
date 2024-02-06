package de.sunnix.engine.graphics.gui;

import de.sunnix.engine.Core;
import de.sunnix.engine.graphics.*;
import de.sunnix.engine.graphics.gui.text.Text;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import de.sunnix.engine.util.Utils;
import lombok.Getter;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static de.sunnix.engine.util.Utils.*;

public class TextBox extends MemoryHolder implements IGUIComponent {

    private static Texture tex = new Texture("/assets/textures/gui/message_box.png");

    /** Text Alignment */
    public static final int
    NORTH = 0b1,
    VERTICAL_CENTER = 0b10,
    SOUTH = 0b100,
    WEST = 0b1000,
    HORIZONTAL_CENTER = 0b10000,
    EAST = 0b100000,
    CENTER = VERTICAL_CENTER | HORIZONTAL_CENTER;

    @Getter
    private Text text;
    @Getter
    private int x, y, width, height;
    @Getter
    private int textAlignment;

    private Mesh mesh;

    public TextBox(String text, int x, int y, int width, int height, int textAlignment) {
        GUIManager.add(this);
        var tW = tex.getWidth() / 3;
        var tH = tex.getHeight() / 3;
        this.text = new Text(text);
        if(width + tW < this.text.getWidth())
            width = (int)Math.ceil(this.text.getWidth() + tW);
        if(width < tW)
            width = tW;
        if(height + tH < this.text.getHeight())
            height = (int)Math.ceil(this.text.getHeight() + tH);
        if (height < tH)
            height = tH;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textAlignment = textAlignment == 0 ? CENTER : textAlignment;
        setTextPosition(this.text, x, y, width, height, tW / 2f, tH / 2f, textAlignment);
        this.mesh = createMesh(width, height);
    }

    public TextBox(String text, int x, int y, int width, int height){
        this(text, x, y, width, height, CENTER);
    }

    private void setTextPosition(Text text, float x, float y, float width, float height, float textureWidth, float textureHeight, int textAlignment){
        float tX, tY;
        if(bitcheck(textAlignment, WEST))
            tX = x + textureWidth;
        else if(bitcheck(textAlignment, EAST))
            tX = x + width - text.getWidth() - textureWidth;
        else
            tX = x + width / 2 - text.getWidth() / 2;
        if(bitcheck(textAlignment, NORTH))
            tY = y + textureHeight;
        else if(bitcheck(textAlignment, SOUTH))
            tY = y + height - text.getHeight() - textureHeight;
        else
            tY = y + height / 2 - text.getHeight() / 2;
        text.setPos(tX, tY);
    }

    private Mesh createMesh(int width, int height){
        var toRender = 9;

        var xPix = 1f / width;
        var yPix = 1f / height;

        var cPX = tex.getWidth() / 3f;
        var cPY = tex.getHeight() / 3f;

        var indices = new int[6 * toRender];
        for (int i = 0; i < toRender; i++) {
            indices[i * 6] = i * 4;
            indices[i * 6 + 1] = i * 4 + 1;
            indices[i * 6 + 2] = i * 4 + 3;
            indices[i * 6 + 3] = i * 4 + 1;
            indices[i * 6 + 4] = i * 4 + 2;
            indices[i * 6 + 5] = i * 4 + 3;
        }

        float minX = 0, minY = 0, maxX = 0, maxY = 0;

        var vertices = new float[12 * toRender];

        // top left
        for (int i = 0; i < toRender; i++) {
            switch (i){
                case 6 ->{
                    minX = 0;
                    minY = 0;
                    maxX = xPix * cPX;
                    maxY = yPix * cPY;
                }
                case 7 ->{
                    minX = xPix * cPX;
                    minY = 0;
                    maxX = xPix * (cPX + width - cPX * 2);
                    maxY = yPix * cPY;
                }
                case 8 ->{
                    minX = xPix * (width - cPX);
                    minY = 0;
                    maxX = xPix * width;
                    maxY = yPix * cPY;
                }
                case 3 ->{
                    minX = 0;
                    minY = yPix * cPY;
                    maxX = xPix * cPX;
                    maxY = yPix * (cPY + height - cPY * 2);
                }
                case 4 ->{
                    minX = xPix * cPX;
                    minY = yPix * cPY;
                    maxX = xPix * (cPX + width - cPX * 2);
                    maxY = yPix * (cPY + height - cPY * 2);
                }
                case 5 ->{
                    minX = xPix * (width - cPX);
                    minY = yPix * cPY;
                    maxX = xPix * width;
                    maxY = yPix * (cPY + height - cPY * 2);
                }
                case 0 ->{
                    minX = 0;
                    minY = yPix * (height - cPY);
                    maxX = xPix * cPX;
                    maxY = yPix * height;
                }
                case 1 ->{
                    minX = xPix * cPX;
                    minY = yPix * (height - cPY);
                    maxX = xPix * (cPX + width - cPX * 2);
                    maxY = yPix * height;
                }
                case 2 ->{
                    minX = xPix * (width - cPX);
                    minY = yPix * (height - cPY);
                    maxX = xPix * width;
                    maxY = yPix * height;
                }
            }

            vertices[i * 12] = minX;
            vertices[i * 12 + 1] = minY;
            vertices[i * 12 + 2] = 0;
            vertices[i * 12 + 3] = minX;
            vertices[i * 12 + 4] = maxY;
            vertices[i * 12 + 5] = 0;
            vertices[i * 12 + 6] = maxX;
            vertices[i * 12 + 7] = maxY;
            vertices[i * 12 + 8] = 0;
            vertices[i * 12 + 9] = maxX;
            vertices[i * 12 + 10] = minY;
            vertices[i * 12 + 11] = 0;
        }

        var textures = new float[8 * toRender];

        var third = 1f / 3;

        for (int i = 0; i < toRender; i++) {
            switch (i){
                case 0 -> {
                    minX = 0;
                    minY = 0;
                    maxX = third;
                    maxY = third;
                }
                case 1 -> {
                    minX = third;
                    minY = 0;
                    maxX = third * 2;
                    maxY = third;
                }
                case 2 -> {
                    minX = third * 2;
                    minY = 0;
                    maxX = third * 3;
                    maxY = third;
                }
                case 3 -> {
                    minX = 0;
                    minY = third;
                    maxX = third;
                    maxY = third * 2;
                }
                case 4 -> {
                    minX = third;
                    minY = third;
                    maxX = third * 2;
                    maxY = third * 2;
                }
                case 5 -> {
                    minX = third * 2;
                    minY = third;
                    maxX = third * 3;
                    maxY = third * 2;
                }
                case 6 -> {
                    minX = 0;
                    minY = third * 2;
                    maxX = third;
                    maxY = third * 3;
                }
                case 7 -> {
                    minX = third;
                    minY = third * 2;
                    maxX = third * 2;
                    maxY = third * 3;
                }
                case 8 -> {
                    minX = third * 2;
                    minY = third * 2;
                    maxX = third * 3;
                    maxY = third * 3;
                }
            }
            textures[i * 8] = minX;
            textures[i * 8 + 1] = maxY;
            textures[i * 8 + 2] = minX;
            textures[i * 8 + 3] = minY;
            textures[i * 8 + 4] = maxX;
            textures[i * 8 + 5] = minY;
            textures[i * 8 + 6] = maxX;
            textures[i * 8 + 7] = maxY;
        }

        return new Mesh(indices, new FloatArrayBuffer(vertices, 3), new FloatArrayBuffer(textures, 2));
    }

    @Override
    public void render(){
        if(!isValid())
            return;
        Shader.DEFAULT_SHADER.bind();
        mesh.bind();
        tex.bind(0);
        var model = new Matrix4f().translate(x, Camera.getSize().y - y - height, 0).scale(width, height, 1);
        var proj = Camera.getProjection();
        var mat = proj.mul(model, new Matrix4f());
        Shader.DEFAULT_SHADER.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        mesh.unbind();
    }

    @Override
    public boolean isValid() {
        return text != null && text.isValid() && mesh != null && mesh.isValid();
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.GUI_COMPONENT;
    }

    @Override
    protected String getMemoryInfo() {
        return "TextBox: " + (text == null ? "INVALID" : text.getText());
    }

    @Override
    protected void free() {
        if(text != null)
            text.freeMemory();
        if(mesh != null)
            mesh.freeMemory();
    }
}
