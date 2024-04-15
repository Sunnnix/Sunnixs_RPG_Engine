package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.util.BetterJSONObject;
import de.sunnix.sdso.DataSaveObject;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
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
            var entries = zip.entries();
            while (entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(imgFolder.getPath()))
                    continue;
                var category = imageResources.computeIfAbsent(e.getName().substring(imgFolder.getPath().length() + 1), k -> new ArrayList<>());
                var images = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList("images");
//                var images = new JSONArray(new String(zip.getInputStream(e).readAllBytes()));
                for(var i = 0; i < images.size(); i++)
                    category.add(new ImageResource(images.get(i)));
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
            for(var category : imageResources.entrySet()){
                var images = new DataSaveObject();
                var list = new ArrayList<DataSaveObject>();
                for(var imageRes: category.getValue()){
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
