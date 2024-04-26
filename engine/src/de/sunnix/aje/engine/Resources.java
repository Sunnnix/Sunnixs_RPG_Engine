package de.sunnix.aje.engine;

import de.sunnix.aje.engine.graphics.TextureAtlas;
import de.sunnix.sdso.DataSaveObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class Resources {

    public final Map<String, List<TextureAtlas>> textures = new HashMap<>();

    private static Resources instance;

    public static Resources get(){
        if(instance == null)
            instance = new Resources();
        return instance;
    }

    public void loadResources(ZipFile zip){
        var resFolder = new File("res");

        loadImageResources(zip, resFolder);
    }

    private void loadImageResources(ZipFile zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var entries = zip.entries();
            while (entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(imgFolder.getPath()))
                    continue;
                var category = textures.computeIfAbsent(e.getName().substring(imgFolder.getPath().length() + 1), k -> new ArrayList<>());
                var images = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList("images");
                for (var image : images) {
                    category.add(new TextureAtlas(image.getString("name", "null"), new ByteArrayInputStream(image.getByteArray("image")), image.getInt("width", 1), image.getInt("height", 1)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
