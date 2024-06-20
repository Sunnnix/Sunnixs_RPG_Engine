package de.sunnix.srpge.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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


    public ImageResource(DataSaveObject imageFile) throws IOException {
        this.name = imageFile.getString("name", "null");
        this.width = imageFile.getInt("width", 1);
        this.height = imageFile.getInt("height", 1);
        this.image = ImageIO.read(new ByteArrayInputStream(imageFile.getByteArray("image")));
    }

    public void save(DataSaveObject imageFile) throws IOException {
        imageFile.putString("name", this.name);
        imageFile.putInt("width", this.width);
        imageFile.putInt("height", this.height);
        var stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        imageFile.putArray("image", stream.toByteArray());
    }

}
