package de.sunnix.aje.editor.window.resource;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Resources {

    public final Map<String, Map<String, BufferedImage>> imageResources = new HashMap<>();

    public Resources(){
        setup();
    }

    public void reset() {
        imageResources.clear();
        setup();
    }

    private void setup(){
        imageResources.put("default", new HashMap<>());
    }

    public void loadResources(ZipFile zip, JSONObject config){
        var resFolder = new File("res");
        // images
        var imageFolder = new File(resFolder, "images");
        var imageConfig = config.getJSONObject("image_res");
        for (var imageCat: imageConfig.keySet()){
            var catArray = imageConfig.getJSONArray(imageCat);
            var categoryFolder = new File(imageFolder, imageCat);
            var map = imageCat.equals("default") ? imageResources.get("default") : new HashMap<String, BufferedImage>();
            for(var i = 0; i < catArray.length(); i++){
                var imageRes = catArray.getString(i);
                var entry = zip.getEntry(new File(categoryFolder, imageRes + ".png").getPath());
                try(var stream = zip.getInputStream(entry)){
                    map.put(imageRes, ImageIO.read(stream));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(!imageCat.equals("default"))
                imageResources.put(imageCat, map);
        }
    }

    public void saveResources(ZipOutputStream zip, JSONObject config) throws Exception{
        var resFolder = new File("res");
        // images
        var imageFolder = new File(resFolder, "images");
        var imageConfig = new JSONObject();
        for(var imageCat: imageResources.entrySet()){
            var categoryFolder = new File(imageFolder, imageCat.getKey());
            var catConfig = new JSONArray();
            for(var imageRes: imageCat.getValue().entrySet()) {
                var zipEntry = new ZipEntry(new File( categoryFolder,  imageRes.getKey() + ".png").getPath());
                catConfig.put(imageRes.getKey());
                zip.putNextEntry(zipEntry);
                ImageIO.write(imageRes.getValue(), "png", zip);
            }
            imageConfig.put(imageCat.getKey(), catConfig);
        }
        config.put("image_res", imageConfig);
    }

}
