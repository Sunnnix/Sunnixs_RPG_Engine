package de.sunnix.engine.util;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class Utils {

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static ByteBuffer getImagePixelsAsBuffer(BufferedImage image){
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        var buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

        for(int y = 0; y < image.getHeight(); y++)
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }

        buffer.flip();

        return buffer;
    }

}
