package de.sunnix.engine.graphics;

import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryCategory;
import de.sunnix.engine.memory.MemoryHolder;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture extends MemoryHolder {
    private static int latestActiveTexture = -1;

    protected int textureID;
    @Getter
    protected final int width, height;

    public Texture(String path) {
        try {
            var image = loadImage(path);
            this.width = image.getWidth();
            this.height = image.getHeight();
            var buffer = getImagePixelsAsBuffer(image);
            ContextQueue.addQueue(() -> this.textureID = genTexture(buffer, width, height));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Loading texture %s failed!", path), e);
        }
    }

    public static BufferedImage loadImage(String path) throws IOException {
        try (var stream = Objects.requireNonNull(Texture.class.getResourceAsStream(path), "Stream was null, no image file!")) {
            return ImageIO.read(stream);
        }
    }

    public static ByteBuffer getImagePixelsAsBuffer(BufferedImage image) {
        var width = image.getWidth();
        var height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        var buffer = ByteBuffer.allocateDirect(width * height * 4);

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        buffer.flip();

        return buffer;
    }

    public static int genTexture(ByteBuffer buffer, int width, int height) {
        var id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        return id;
    }

    public void bind(int texPos) {
        texPos = GL_TEXTURE0 + texPos;
        if (texPos != latestActiveTexture) {
            latestActiveTexture = texPos;
            glActiveTexture(texPos);
        }
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void bind() {
        bind(0);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public boolean created() {
        return textureID != 0;
    }

    @Override
    protected MemoryCategory getMemoryCategory() {
        return MemoryCategory.TEXTURE;
    }

    @Override
    protected String getMemoryInfo() {
        return "Texture";
    }

    @Override
    public void free() {
        glDeleteTextures(textureID);
    }
}
