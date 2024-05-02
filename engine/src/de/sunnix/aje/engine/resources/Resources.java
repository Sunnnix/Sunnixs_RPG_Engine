package de.sunnix.aje.engine.resources;

import de.sunnix.aje.engine.graphics.Texture;
import de.sunnix.aje.engine.graphics.TextureAtlas;
import de.sunnix.sdso.DataSaveObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Resources {

    private final Map<String, Map<String, TextureAtlas>> textures = new HashMap<>();
    private final Map<String, Tileset> tilesets = new HashMap<>();

    private static Resources instance;

    public static Resources get(){
        if(instance == null)
            instance = new Resources();
        return instance;
    }

    public void loadResources(ZipFile zip){
        var resFolder = new File("res");

        loadImageResources(zip, resFolder);
        loadTilesets(zip, resFolder);
    }

    private void loadImageResources(ZipFile zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var entries = zip.entries();
            while (entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(imgFolder.getPath()))
                    continue;
                var category = textures.computeIfAbsent(e.getName().substring(imgFolder.getPath().length() + 1), k -> new HashMap<>());
                var images = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList("images");
                for (var image : images) {
                    category.put(image.getString("name", "null"), new TextureAtlas(new ByteArrayInputStream(image.getByteArray("image")), image.getInt("width", 1), image.getInt("height", 1)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void loadTilesets(ZipFile zip, File res){
        DataSaveObject dso;
        try (var stream = zip.getInputStream(new ZipEntry(new File(res, "tilesets.dat").getPath()))){
            dso = new DataSaveObject().load(stream);
        } catch (Exception e){
            return;
        }
        dso.<DataSaveObject>getList("tilesets").forEach(tsData -> {
            var tileset = new Tileset(tsData);
            this.tilesets.put(tileset.getName(), tileset);
        });
    }

    public TextureAtlas getTexture(String category, String texture){
        var cat = textures.get(category);
        if(cat == null)
            return null;
        return cat.get(texture);
    }

    public TextureAtlas getTexture(String path){
        if(path == null)
            return null;
        var split = path.split("/");
        if(split.length < 2)
            return null;
        return getTexture(split[0], split[1]);
    }

    public Tileset getTileset(String name){
        return tilesets.get(name);
    }

    public Texture getTilesetTex(String name){
        var ts = getTileset(name);
        if(ts == null)
            return null;
        return ts.getRes();
    }

}
