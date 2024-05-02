package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.data.Tile;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Tileset {

    @Getter
    private String name;
    @Getter
    private String res;

    @Getter
    private int width = 1, height = 1;
    private TilesetPropertie[] properties = new TilesetPropertie[] { new TilesetPropertie() };

    public Tileset(String name, String res, Resources resources) throws IndexOutOfBoundsException{
        this.name = name;
        this.res = res;
        var image = resources.image_getRaw(res);
        if(image != null) {
            width = image.getWidth() / 24;
            height = image.getHeight() / 16;
        }
        if(width * height >= 0xFFF)
            // The tiles of a map store their textures in a short, with 4 bits dedicated to the tileset ID and the remaining 12 bits to the texture ID of that tileset
            throw new IndexOutOfBoundsException("The tileset is to big!\nMaximum tiles are " + (0xFFF + 1) + " but " + (width * height) + " was given!");
        properties = new TilesetPropertie[width * height];
        for(var i = 0; i < properties.length; i++)
            properties[i] = new TilesetPropertie();
    }

    public Tileset(DataSaveObject data) {
        load(data);
    }

    private void load(DataSaveObject data){
        this.name = data.getString("name", null);
        this.res = data.getString("res", null);
        this.width = data.getInt("width", 1);
        this.height = data.getInt("height", 1);
        this.properties = data.<DataSaveObject>getList("properties").stream().map(TilesetPropertie::new).toArray(TilesetPropertie[]::new);
    }

    public DataSaveObject save(DataSaveObject data){
        data.putString("name", name);
        data.putString("res", res);
        data.putInt("width", width);
        data.putInt("height", height);
        data.putList("properties", Arrays.stream(properties).map(p -> p.save(new DataSaveObject())).toList());
        return data;
    }

    public ImageResource getImageResource(Window window){
        return window.getSingleton(Resources.class).image_get(res);
    }


    public BufferedImage getImage(Window window){
        return window.getSingleton(Resources.class).image_getRaw(res);
    }

    public TilesetPropertie getPropertie(int id){
        if(id < 0 || id >= properties.length)
            return null;
        return properties[id];
    }

    public TilesetPropertie getPropertie(int x, int y){
        return getPropertie(x + y * width);
    }

}
