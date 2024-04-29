package de.sunnix.aje.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Resources {

    private final Map<String, HashMap<String, ImageResource>> imageResources = new HashMap<>();
    private final Map<String, Tileset> tilesets = new HashMap<>();

    public Resources(){
        setup();
    }

    public void reset() {
        imageResources.clear();
        tilesets.clear();
        setup();
    }

    private void setup(){
        imageResources.put("default", new HashMap<>());
    }

    // ###################    Image Resource    ###################

    public ImageResource image_get(String path){
        var split = path.split("/");
        if(split.length < 2)
            return null;
        return image_get(split[0], split[1]);
    }

    public ImageResource image_get(String category, String image){
        var cat = imageResources.get(category);
        if(cat == null)
            return null;
        return cat.get(image);
    }

    public BufferedImage image_getRaw(String path){
        var res = image_get(path);
        if(res == null)
            return null;
        return res.getImage();
    }

    public BufferedImage image_getRaw(String category, String resName){
        var res = image_get(category, resName);
        if(res == null)
            return null;
        return res.getImage();
    }

    public Collection<String> image_getAllCategories(){
        return imageResources.keySet();
    }

    public Collection<String> image_getCategoryContent(String category){
        var cat = imageResources.get(category);
        if(cat == null)
            return Collections.emptyList();
        return cat.keySet();
    }

    public void image_createCategory(String name){
        if(imageResources.containsKey(name))
            return;
        imageResources.put(name, new HashMap<>());
    }

    public HashMap<String, ImageResource> image_setCategory(String name, HashMap<String, ImageResource> category){
        return imageResources.put(name, category);
    }

    public HashMap<String, ImageResource> image_removeCategory(String name){
        return imageResources.remove(name);
    }

    public boolean image_containsCategory(String name){
        return imageResources.containsKey(name);
    }

    public ImageResource image_addResource(String category, ImageResource res){
        return imageResources.computeIfAbsent(category, e -> new HashMap<>()).put(res.getName(), res);
    }

    public ImageResource image_removeResource(String category, String resName){
        var cat = imageResources.get(category);
        if(cat == null)
            return null;
        return cat.remove(resName);
    }

    public boolean image_containsResource(String category, String resName){
        var cat = imageResources.get(category);
        if(cat == null)
            return false;
        return cat.containsKey(resName);
    }

    // ###################    Load    ###################

    public void loadResources(ZipFile zip){
        reset();
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
                var category = imageResources.computeIfAbsent(e.getName().substring(imgFolder.getPath().length() + 1), k -> new HashMap<>());
                var images = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList("images");
                for (DataSaveObject image : images) {
                    var imageRes = new ImageResource(image);
                    category.put(imageRes.getName(), imageRes);
                }
            }
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
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
        var tilesets = dso.<DataSaveObject>getList("tilesets");
        for(var ts: tilesets) {
            var tileset = new Tileset(ts);
            this.tilesets.put(tileset.getName(), tileset);
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
            for(var category : imageResources.entrySet()){
                var images = new DataSaveObject();
                var list = new ArrayList<DataSaveObject>();
                for(var imageRes: category.getValue().values()){
                    var image = new DataSaveObject();
                    imageRes.save(image);
                    list.add(image);
                }
                images.putList("images", list);
                zip.putNextEntry(new ZipEntry(new File(imgFolder, category.getKey()).getPath()));
                var oStream = new ByteArrayOutputStream();
                images.save(oStream);
                zip.write(oStream.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
