package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.window.io.BetterJSONObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Getter
@Setter
public class ImageResource {

    private String name;
    private int width, height;
    @Setter(AccessLevel.NONE)
    private BufferedImage image;

    public ImageResource(String name, int width, int height, BufferedImage image){
        this.name = name;
        this.width = width;
        this.height = height;
        this.image = image;
    }


    public ImageResource(BetterJSONObject imageFile) throws IOException, InvocationTargetException, IllegalAccessException {
        this.name = imageFile.get("name", "null");
        this.width = imageFile.get("width", 1);
        this.height = imageFile.get("height", 1);
        this.image = ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(imageFile.getByteArray("image"))));
    }

    public void save(BetterJSONObject imageFile) throws IOException {
        imageFile.put("name", this.name);
        imageFile.put("width", this.width);
        imageFile.put("height", this.height);
        var stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        imageFile.put("image", stream.toByteArray());
    }

}
