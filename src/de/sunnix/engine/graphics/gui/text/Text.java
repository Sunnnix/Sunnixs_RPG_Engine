package de.sunnix.engine.graphics.gui.text;

import de.sunnix.engine.Core;
import de.sunnix.engine.graphics.Camera;
import de.sunnix.engine.graphics.FloatArrayBuffer;
import de.sunnix.engine.graphics.Mesh;
import de.sunnix.engine.graphics.Shader;
import de.sunnix.engine.graphics.gui.GUIManager;
import de.sunnix.engine.graphics.gui.IGUIComponent;
import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

public class Text extends MemoryHolder implements IGUIComponent {

    private static final float POINT_IN_PIXEL = 1.33f;
    private static final Shader shader = new Shader("/data/shader/text_shader");

    @Getter
    private String text;
    @Getter
    private final Vector2f pos = new Vector2f();
    private float size = 12;
    private Font font = Font.COMIC_SANS;
    private byte fontStyle = Font.STYLE_NORMAL;
    private final Vector4f color = new Vector4f(1);
    private double point_in_pixel_width;
    private int rows;

    private boolean drawShadow = true;

    private Mesh mesh;

    public Text(String text, Consumer<TextChanger> edit){
        this.text = text;
        var tc = new TextChanger();
        edit.accept(tc);
        if(!tc.commit())
            prepare();
        GUIManager.add(this);
    }

    public Text(String text){
        this(text, e -> {});
    }

    private void prepare(){
        if(drawShadow){
            prepareWithShadow();
            return;
        }
        float minX = 0, maxX, minY = -POINT_IN_PIXEL * (1 - font.cut_vertical_spacing), maxY;
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
                minY -= POINT_IN_PIXEL  * (1 - font.cut_vertical_spacing);
                row++;
                continue;
            }

            var glyph = font.getGlyph(fontStyle, c);

            var ratio = (float) glyph.width() / glyph.height();

            maxX = POINT_IN_PIXEL * ratio + minX;
            maxY = POINT_IN_PIXEL + minY;

            vertices[i * 8] = minX;
            vertices[i * 8 + 1] = minY;
            vertices[i * 8 + 2] = minX;
            vertices[i * 8 + 3] = maxY;
            vertices[i * 8 + 4] = maxX;
            vertices[i * 8 + 5] = maxY;
            vertices[i * 8 + 6] = maxX;
            vertices[i * 8 + 7] = minY;

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

        if(!text.isEmpty())
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

        this.point_in_pixel_width = nextWidth;
    }

    private void prepareWithShadow(){
        float minX = 0, maxX, minY = -POINT_IN_PIXEL * (1 - font.cut_vertical_spacing), maxY;
        var tex = font.getTexture(fontStyle);
        var sX = 1f / tex.getWidth();
        var sY = 1f / tex.getHeight();
        var shadowShift = .045f;

        var cLength = (int) text.chars().filter(c -> c != '\n').count() * 2;

        var vertices = new float[8 * cLength];
        var colors = new float[16 * cLength];
        var textures = new float[8 * cLength];
        var indices = new int[6 * cLength];

        var nextWidth = 0d;

        var row = 0;

        var i = 0;
        for (int ci = 0; ci < text.length(); ci++) {
            var c = text.charAt(ci);
            if(c == '\n'){
                minX = 0;
                minY -= POINT_IN_PIXEL  * (1 - font.cut_vertical_spacing);
                row++;
                continue;
            }

            var glyph = font.getGlyph(fontStyle, c);

            var ratio = (float) glyph.width() / glyph.height();

            maxX = POINT_IN_PIXEL * ratio + minX;
            maxY = POINT_IN_PIXEL + minY;

            vertices[i * 16] = minX + shadowShift;
            vertices[i * 16 + 1] = minY - shadowShift;
            vertices[i * 16 + 2] = minX + shadowShift;
            vertices[i * 16 + 3] = maxY - shadowShift;
            vertices[i * 16 + 4] = maxX + shadowShift;
            vertices[i * 16 + 5] = maxY - shadowShift;
            vertices[i * 16 + 6] = maxX + shadowShift;
            vertices[i * 16 + 7] = minY - shadowShift;

            // shadow
            vertices[i * 16 + 8] = minX;
            vertices[i * 16 + 9] = minY;
            vertices[i * 16 + 10] = minX;
            vertices[i * 16 + 11] = maxY;
            vertices[i * 16 + 12] = maxX;
            vertices[i * 16 + 13] = maxY;
            vertices[i * 16 + 14] = maxX;
            vertices[i * 16 + 15] = minY;

            minX += ((float) glyph.xAdvance() / glyph.height()) * POINT_IN_PIXEL;
            if (minX > nextWidth)
                nextWidth = minX;

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

        if(!text.isEmpty())
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

        this.point_in_pixel_width = nextWidth;
    }

    /**
     * get a TextChanger to set changes and commit all changes together
     */
    public Text change(Consumer<TextChanger> edit){
        var tc = new TextChanger();
        edit.accept(tc);
        tc.commit();
        return this;
    }

    public Text setSize(float newSize){
        this.size = newSize;
        return this;
    }

    public Text setPos(Vector2f vec2){
        pos.set(vec2);
        return this;
    }

    public Text setPos(double x, double y){
        pos.set(x, y);
        return this;
    }

    public Text addPos(Vector2f vec2){
        pos.add(vec2);
        return this;
    }

    public Text addPos(double x, double y){
        pos.add((float)x, (float)y);
        return this;
    }

    @Override
    public void render(){
        if(!isValid())
            return;
        shader.bind();
        mesh.bind();
        font.getTexture(fontStyle).bind(0);
        var model = new Matrix4f().translate(pos.x, Camera.getSize().y - pos.y, 0).scale(size, size, 1);
        var proj = Camera.getProjection();
        var mat = proj.mul(model, new Matrix4f());
        shader.uniformMat4("projection", mat.get(new float[16]));
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
        mesh.unbind();
    }

    public float getWidth(){
        return (float)(point_in_pixel_width * size);
    }

    public float getHeight(){
        return (POINT_IN_PIXEL * rows * size) * (1 - font.cut_vertical_spacing);
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

    public class TextChanger {

        private TextChanger(){}

        private boolean dirty;

        public TextChanger setText(String text){
            if(text == null)
                text = "null";
            if(Text.this.text.equals(text))
                return this;
            dirty = true;
            Text.this.text = text;
            return this;
        }

        public TextChanger setColor(float r, float g, float b, float a){
            if(Text.this.color.equals(r, g, b, a))
                return this;
            dirty = true;
            Text.this.color.set(r, g, b, a);
            return this;
        }

        public TextChanger setFont(Font font){
            if(Text.this.font.equals(font))
                return this;
            dirty = true;
            Text.this.font = font;
            return this;
        }

        public TextChanger setFontStyle(int style){
            if(Text.this.fontStyle == style)
                return this;
            dirty = true;
            Text.this.fontStyle = (byte) style;
            return this;
        }

        public TextChanger setDrawShadow(boolean drawShadow){
            if(Text.this.drawShadow == drawShadow)
                return this;
            dirty = true;
            Text.this.drawShadow = drawShadow;
            return this;
        }

        private boolean commit(){
            if(!dirty)
                return false;
            Text.this.prepare();
            return true;
        }

    }
}
