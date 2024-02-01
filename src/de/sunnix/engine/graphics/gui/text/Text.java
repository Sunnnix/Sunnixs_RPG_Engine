package de.sunnix.engine.graphics.gui.text;

import de.sunnix.engine.graphics.Camera;
import de.sunnix.engine.graphics.FloatArrayBuffer;
import de.sunnix.engine.graphics.Mesh;
import de.sunnix.engine.graphics.Shader;
import de.sunnix.engine.graphics.gui.GUIManager;
import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class Text extends MemoryHolder {

    private static final float POINT_IN_PIXEL = 1.33f;
    private static final Shader shader = new Shader("/data/shader/text_shader");

    private String text;
    @Getter
    private final Vector2f position = new Vector2f();
    private float size = 24;
    private Font font = Font.COMIC_SANS;
    private byte fontStyle = Font.STYLE_NORMAL;
    private final Vector4f color = new Vector4f(1);
    private int width;
    private int rows;

    private boolean genShadow = true;

    private Mesh mesh;

    public Text(String text, int x, int y, float size, Font font, int style, float r, float g, float b, float a, boolean shadow){
        this.text = text;
        this.position.set(x, y);
        this.size = size;
        this.font = font;
        this.fontStyle = (byte) style;
        this.color.set(r, g, b, a);
        this.genShadow = shadow;
        GUIManager.add(this);
        prepare();
    }

    public Text(String text, int x, int y){
        this.text = text;
        this.position.set(x, y);
        GUIManager.add(this);
        prepare();
    }

    public Text(String text, int x, int y, float size, byte style){
        this.text = text;
        this.position.set(x, y);
        this.size = size;
        this.fontStyle = style;
        GUIManager.add(this);
        prepare();
    }

    private void prepare(){
        if(genShadow){
            prepareWithShadow();
            return;
        }
        float minX = 0, maxX, minY = 0, maxY;
        var tex = font.getTexture(fontStyle);
        var sX = 1f / tex.getWidth();
        var sY = 1f / tex.getHeight();

        var cLength = (int) text.chars().filter(c -> c != '\n').count();

        var vertices = new float[8 * cLength];
        var colors = new float[16 * cLength];
        var textures = new float[8 * cLength];
        var indices = new int[6 * cLength];

        var nextWidth = 0;

        var row = 0;

        var i = 0;
        for (int ci = 0; ci < text.length(); ci++) {
            var c = text.charAt(ci);
            if(c == '\n'){
                minX = 0;
                row++;
                continue;
            }

            var glyph = font.getGlyph(fontStyle, c);

            var ratio = (float) glyph.width() / glyph.height();

            maxX = POINT_IN_PIXEL * ratio + minX;
            maxY = POINT_IN_PIXEL + minY;

            vertices[i * 8] = minX;
            vertices[i * 8 + 1] = minY - row * POINT_IN_PIXEL;
            vertices[i * 8 + 2] = minX;
            vertices[i * 8 + 3] = maxY - row * POINT_IN_PIXEL;
            vertices[i * 8 + 4] = maxX;
            vertices[i * 8 + 5] = maxY - row * POINT_IN_PIXEL;
            vertices[i * 8 + 6] = maxX;
            vertices[i * 8 + 7] = minY - row * POINT_IN_PIXEL;

            minX += ((float) glyph.xAdvance() / glyph.height()) * POINT_IN_PIXEL;
            if (minX > nextWidth)
                nextWidth = (int) minX;

            for (int j = 0; j < 4; j++) {
                colors[i * 16 + j * 4] = color.x;
                colors[i * 16 + j * 4 + 1] = color.y;
                colors[i * 16 + j * 4 + 2] = color.z;
                colors[i * 16 + j * 4 + 3] = color.w;
            }

            float tMinX, tMaxX, tMinY, tMaxY;
            tMinX = glyph.x() * sX;
            tMaxX = tMinX + glyph.width() * sX;
            tMinY = glyph.y() * sY;
            tMaxY = tMinY + glyph.height() * sY;

            textures[i * 8] = tMinX;
            textures[i * 8 + 1] = tMaxY;
            textures[i * 8 + 2] = tMinX;
            textures[i * 8 + 3] = tMinY;
            textures[i * 8 + 4] = tMaxX;
            textures[i * 8 + 5] = tMinY;
            textures[i * 8 + 6] = tMaxX;
            textures[i * 8 + 7] = tMaxY;

            indices[i * 6] = 4 * i;
            indices[i * 6 + 1] = 4 * i + 1;
            indices[i * 6 + 2] = 4 * i + 3;
            indices[i * 6 + 3] = 4 * i + 1;
            indices[i * 6 + 4] = 4 * i + 2;
            indices[i * 6 + 5] = 4 * i + 3;

            i++;
        }

        this.rows = row + 1;

        if(mesh == null)
            this.mesh = new Mesh(indices,
                    new FloatArrayBuffer(vertices, 2),
                    new FloatArrayBuffer(colors, 4),
                    new FloatArrayBuffer(textures, 2));
        else {
            ContextQueue.addQueue(() -> {
                mesh.changeIndices(indices);
                mesh.changeBuffer(0, vertices);
                mesh.changeBuffer(1, colors);
                mesh.changeBuffer(2, textures);
            });
        }

        this.width = (int)(nextWidth * size);
    }

    private void prepareWithShadow(){
        float minX = 0, maxX, minY = 0, maxY;
        var tex = font.getTexture(fontStyle);
        var sX = 1f / tex.getWidth();
        var sY = 1f / tex.getHeight();
        var shadowShift = .045f;

        var cLength = (int) text.chars().filter(c -> c != '\n').count() * 2;

        var vertices = new float[8 * cLength];
        var colors = new float[16 * cLength];
        var textures = new float[8 * cLength];
        var indices = new int[6 * cLength];

        var nextWidth = 0;

        var row = 0;

        var i = 0;
        for (int ci = 0; ci < text.length(); ci++) {
            var c = text.charAt(ci);
            if(c == '\n'){
                minX = 0;
                row++;
                continue;
            }

            var glyph = font.getGlyph(fontStyle, c);

            var ratio = (float) glyph.width() / glyph.height();

            maxX = POINT_IN_PIXEL * ratio + minX;
            maxY = POINT_IN_PIXEL + minY;

            vertices[i * 16] = minX + shadowShift;
            vertices[i * 16 + 1] = minY - row * POINT_IN_PIXEL - shadowShift;
            vertices[i * 16 + 2] = minX + shadowShift;
            vertices[i * 16 + 3] = maxY - row * POINT_IN_PIXEL - shadowShift;
            vertices[i * 16 + 4] = maxX + shadowShift;
            vertices[i * 16 + 5] = maxY - row * POINT_IN_PIXEL - shadowShift;
            vertices[i * 16 + 6] = maxX + shadowShift;
            vertices[i * 16 + 7] = minY - row * POINT_IN_PIXEL - shadowShift;

            // shadow
            vertices[i * 16 + 8] = minX;
            vertices[i * 16 + 9] = minY - row * POINT_IN_PIXEL;
            vertices[i * 16 + 10] = minX;
            vertices[i * 16 + 11] = maxY - row * POINT_IN_PIXEL;
            vertices[i * 16 + 12] = maxX;
            vertices[i * 16 + 13] = maxY - row * POINT_IN_PIXEL;
            vertices[i * 16 + 14] = maxX;
            vertices[i * 16 + 15] = minY - row * POINT_IN_PIXEL;

            minX += ((float) glyph.xAdvance() / glyph.height()) * POINT_IN_PIXEL;
            if (minX > nextWidth)
                nextWidth = (int) minX;

            for (int j = 0; j < 4; j++) {
                colors[i * 32 + j * 4] = 0;
                colors[i * 32 + j * 4 + 1] = 0;
                colors[i * 32 + j * 4 + 2] = 0;
                colors[i * 32 + j * 4 + 3] = .7f;
            }
            for (int j = 0; j < 4; j++) {
                colors[i * 32 + 16 + j * 4] = color.x;
                colors[i * 32 + 16 + j * 4 + 1] = color.y;
                colors[i * 32 + 16 + j * 4 + 2] = color.z;
                colors[i * 32 + 16 + j * 4 + 3] = color.w;
            }

            float tMinX, tMaxX, tMinY, tMaxY;
            tMinX = glyph.x() * sX;
            tMaxX = tMinX + glyph.width() * sX;
            tMinY = glyph.y() * sY;
            tMaxY = tMinY + glyph.height() * sY;

            for (int j = 0; j < 2; j++) {
                textures[i * 16 + j * 8] = tMinX;
                textures[i * 16 + j * 8 + 1] = tMaxY;
                textures[i * 16 + j * 8 + 2] = tMinX;
                textures[i * 16 + j * 8 + 3] = tMinY;
                textures[i * 16 + j * 8 + 4] = tMaxX;
                textures[i * 16 + j * 8 + 5] = tMinY;
                textures[i * 16 + j * 8 + 6] = tMaxX;
                textures[i * 16 + j * 8 + 7] = tMaxY;

            }

            indices[i * 12] = 8 * i;
            indices[i * 12 + 1] = 8 * i + 1;
            indices[i * 12 + 2] = 8 * i + 3;
            indices[i * 12 + 3] = 8 * i + 1;
            indices[i * 12 + 4] = 8 * i + 2;
            indices[i * 12 + 5] = 8 * i + 3;

            indices[i * 12 + 6] = 8 * i + 4;
            indices[i * 12 + 7] = 8 * i + 5;
            indices[i * 12 + 8] = 8 * i + 7;
            indices[i * 12 + 9] = 8 * i + 5;
            indices[i * 12 + 10] = 8 * i + 6;
            indices[i * 12 + 11] = 8 * i + 7;

            i++;
        }

        this.rows = row + 1;

        if(mesh == null)
            this.mesh = new Mesh(indices,
                    new FloatArrayBuffer(vertices, 2),
                    new FloatArrayBuffer(colors, 4),
                    new FloatArrayBuffer(textures, 2));
        else {
            ContextQueue.addQueue(() -> {
                mesh.changeIndices(indices);
                mesh.changeBuffer(0, vertices);
                mesh.changeBuffer(1, colors);
                mesh.changeBuffer(2, textures);
            });
        }

        this.width = (int)(nextWidth * size);
    }

    public void setText(String text){
        if(text == null)
            text = "null";
        if(this.text.equals(text))
            return;
        this.text = text;
        prepare();
    }

    public void setFont(Font font){
        if(this.font.equals(font))
            return;
        this.font = font;
        prepare();
    }

    public void setColor(float r, float g, float b, float a){
        if(color.equals(r, g, b, a))
            return;
        color.set(r, g, b, a);
        prepare();
    }

    public void setFontStyle(int style){
        if(this.fontStyle == style)
            return;
        this.fontStyle = (byte) style;
        prepare();
    }

    public void setSize(float newSize){
        this.size = newSize;
    }

    public void changeData(String text, Font font, float r, float b, float g, float a){
        boolean nT = false, nF = false, nC = false;
        if(text == null)
            text = "null";
        if(!this.text.equals(text)){
            nT = true;
            this.text = text;
        }
        if(this.font.equals(font)){
            nF = true;
            this.font = font;
        }
        if(color.equals(r, g, b, a)){
            nC = true;
            color.set(r, g, b, a);
        }
        if(nT || nF || nC)
            prepare();
    }

    public void changeData(String text, Font font){
        boolean nT = false, nF = false;
        if(text == null)
            text = "null";
        if(!this.text.equals(text)){
            nT = true;
            this.text = text;
        }
        if(this.font.equals(font)){
            nF = true;
            this.font = font;
        }
        if(nT || nF)
            prepare();
    }

    public void changeData(String text, float r, float b, float g, float a){
        boolean nT = false, nC = false;
        if(text == null)
            text = "null";
        if(!this.text.equals(text)){
            nT = true;
            this.text = text;
        }
        if(color.equals(r, g, b, a)){
            nC = true;
            color.set(r, g, b, a);
        }
        if(nT || nC)
            prepare();
    }

    public void changeData(Font font, float r, float b, float g, float a){
        boolean nF = false, nC = false;
        if(this.font.equals(font)){
            nF = true;
            this.font = font;
        }
        if(color.equals(r, g, b, a)){
            nC = true;
            color.set(r, g, b, a);
        }
        if(nF || nC)
            prepare();
    }

    public void render(){
        if(!isValid())
            return;
        shader.bind();
        mesh.bind();
        font.getTexture(fontStyle).bind(0);
        var model = new Matrix4f().translate(position.x, position.y, 0).scale(size, size, 1);
        var proj = Camera.getProjection();
        var mat = proj.mul(model, new Matrix4f());
        shader.uniformMat4(shader.getUNIFORM_PROJECTION(), mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    public float getWidth(){
        return width * size;
    }

    public float getHeight(){
        return POINT_IN_PIXEL * rows * size;
    }

    @Override
    public boolean isValid() {
        return mesh != null && mesh.isValid();
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.TEXT;
    }

    @Override
    protected String getMemoryInfo() {
        return "Text: " + text;
    }

    @Override
    protected void free() {
        if(mesh != null)
            mesh.freeMemory();
    }
}
