package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.window.io.BetterJSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Resources {

    public final Map<String, List<ImageResource>> imageResources = new HashMap<>();

    public Resources(){
        setup();
    }

    public void reset() {
        imageResources.clear();
        setup();
    }

    private void setup(){
        imageResources.put("default", new ArrayList<>());
    }

    // ###################    Load    ###################

    public void loadResources(ZipFile zip){
        reset();
        var resFolder = new File("res");

        loadImageResources(zip, resFolder);
    }

    private void loadImageResources(ZipFile zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var config = new BetterJSONObject(new String(zip.getInputStream(new ZipEntry(imgFolder.getPath())).readAllBytes()));
            var keys = config.keySet();
            for(var categoryName : keys){
                var category = imageResources.computeIfAbsent(categoryName, k -> new ArrayList<>());
                var images = config.getJSONArray(categoryName);
                for(var i = 0; i < images.length(); i++)
                    category.add(new ImageResource(new BetterJSONObject(images.getJSONObject(i))));
            }
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // ###################    Save    ###################

    public void saveResources(ZipOutputStream zip){
        var resFolder = new File("res");

        saveImageResources(zip, resFolder);
    }

    private void saveImageResources(ZipOutputStream zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var config = new BetterJSONObject();
            for(var category : imageResources.entrySet()){
                var images = new JSONArray();
                for(var imageRes: category.getValue()){
                    var image = new BetterJSONObject();
                    imageRes.save(image);
                    images.put(image);
                }
                config.put(category.getKey(), images);
            }
            zip.putNextEntry(new ZipEntry(imgFolder.getPath()));
            zip.write(config.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
