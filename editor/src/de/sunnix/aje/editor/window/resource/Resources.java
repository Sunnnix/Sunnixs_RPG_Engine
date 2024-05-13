package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.util.LoadingDialog;
import de.sunnix.aje.editor.window.resource.audio.AudioResource;
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
    private final Map<String, Map<String, AudioResource>> audioResources = new HashMap<>();

    public Resources(){
        setup();
    }

    public void reset() {
        imageResources.clear();
        tilesets.clear();
        audioResources.forEach((k,v) -> v.values().forEach(AudioResource::cleanup));
        audioResources.clear();
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

    // ###################    Tileset Resource    ###################

    public Collection<String> tileset_getTilesetnames(){
        return tilesets.keySet();
    }

    public void tileset_add(String name, Tileset tileset){
        tilesets.put(name, tileset);
    }

    public Tileset tileset_get(String tsName) {
        return tilesets.get(tsName);
    }

    public Tileset tileset_remove(String tsName) {
        return tilesets.remove(tsName);
    }

    public BufferedImage tileset_getRaw(String tsName) {
        var ts = tileset_get(tsName);
        if(ts == null)
            return null;
        return image_getRaw(ts.getRes());
    }

    // ###################    Audio Resource    ###################

    public Collection<String> audio_getCategories(){
        return audioResources.keySet();
    }

    public void audio_addCategory(String category){
        audioResources.putIfAbsent(category, new HashMap<>());
    }

    public void audio_addCategory(String category, Map<String, AudioResource> map){
        audioResources.put(category, map);
    }

    public Map<String, AudioResource> audio_removeCategory(String category, boolean freeData){
        var cat = audioResources.remove(category);
        if(freeData)
            cat.values().forEach(AudioResource::cleanup);
        return cat;
    }

    public Map<String, AudioResource> audio_removeCategory(String category){
        return audio_removeCategory(category, false);
    }

    public AudioResource audio_get(String category, String audio){
        return audioResources.getOrDefault(category, Collections.emptyMap()).get(audio);
    }

    public Collection<AudioResource> audio_getAll(String category){
        return audioResources.getOrDefault(category, Collections.emptyMap()).values();
    }

    public void audio_add(String category, AudioResource audio){
        audioResources.computeIfAbsent(category, k -> new HashMap<>()).put(audio.getName(), audio);
    }

    public AudioResource audio_remove(String category, String audio, boolean freeData) {
        var res = audioResources.getOrDefault(category, Collections.emptyMap()).remove(audio);
        if(freeData)
            res.cleanup();
        return res;
    }

    public AudioResource audio_remove(String category, String audio) {
        return audio_remove(category, audio,false);
    }

    public Collection<String> audio_getAllNames(String category) {
        return audioResources.getOrDefault(category, Collections.emptyMap()).keySet();
    }

    // ###################    Load    ###################

    public void loadResources(LoadingDialog dialog, int progress, ZipFile zip) throws Exception {
        reset();
        var resFolder = new File("res");

        loadImageResources(dialog, (int)(progress * .25), zip, resFolder);
        loadTilesets(dialog, (int)(progress * .10), zip, resFolder);
        loadAudioResources(dialog, (int)(progress * .65), zip, resFolder);
    }

    private void loadImageResources(LoadingDialog dialog, int progress, ZipFile zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var entries = zip.entries();
            var files = new ArrayList<ZipEntry>();
            while(entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(imgFolder.getPath()))
                    continue;
                files.add(e);
            }
            var progressPerFile = (int)((double) progress / files.size());
            for(var e: files){
                var category = imageResources.computeIfAbsent(e.getName().substring(imgFolder.getPath().length() + 1), k -> new HashMap<>());
                var images = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList("images");
                for (DataSaveObject image : images) {
                    var imageRes = new ImageResource(image);
                    category.put(imageRes.getName(), imageRes);
                }
                dialog.addProgress(progressPerFile);
            }
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void loadTilesets(LoadingDialog dialog, int progress, ZipFile zip, File res){
        DataSaveObject dso;
        try (var stream = zip.getInputStream(new ZipEntry(new File(res, "tilesets.dat").getPath()))){
            dso = new DataSaveObject().load(stream);
        } catch (Exception e){
            dialog.addProgress(progress);
            return;
        }
        var list = dso.<DataSaveObject>getList("tilesets");
        var progressPerFile = (int)((double) progress / list.size());
        list.forEach(tsData -> {
            var tileset = new Tileset(tsData);
            this.tilesets.put(tileset.getName(), tileset);
            dialog.addProgress(progressPerFile);
        });
    }

    private void loadAudioResources(LoadingDialog dialog, int progress, ZipFile zip, File res) throws Exception{
        var audioFolder = new File(res, "audio");
        List<String> categories;
        try {
            categories = new DataSaveObject().load(zip.getInputStream(new ZipEntry(new File(audioFolder, "audio.dat").getPath()))).getList("categories");
        } catch (Exception e){
            dialog.addProgress(progress);
            return;
        }
        var progressPerCat = (double) progress / categories.size();
        for(var cat : categories) {
            var data = new DataSaveObject().load(zip.getInputStream(new ZipEntry(new File(audioFolder, cat).getPath()))).<DataSaveObject>getList("data");
            var progressPerFile = (int)(progressPerCat / data.size());
            var category = new HashMap<String, AudioResource>();
            for(var audioDSO: data){
                var audio = new AudioResource(audioDSO);
                category.put(audio.getName(), audio);
                dialog.addProgress(progressPerFile);
            }
            this.audioResources.put(cat, category);
        }
    }

    // ###################    Save    ###################

    public void saveResources(LoadingDialog dialog, int progress, ZipOutputStream zip) throws Exception {
        var resFolder = new File("res");

        saveImageResources(dialog, progress, zip, resFolder);
        saveTilesets(dialog, progress, zip, resFolder);
        saveAudioResources(dialog, progress, zip, resFolder);
    }

    private void saveImageResources(LoadingDialog dialog, int progress, ZipOutputStream zip, File res){
        var imgFolder = new File(res, "images");
        try{
            var progressPerFile = (int)((double) progress / imageResources.size());
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
                oStream.close();
                dialog.addProgress(progressPerFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void saveTilesets(LoadingDialog dialog, int progress, ZipOutputStream zip, File res) {
        var dso = new DataSaveObject();
        dso.putList("tilesets", tilesets.values().stream().map(ts -> ts.save(new DataSaveObject())).toList());
        dialog.addProgress(progress);
        try {
            zip.putNextEntry(new ZipEntry(new File(res, "tilesets.dat").toString()));
            dso.save(zip);
        } catch (Exception e){
            new Exception("Error loading tilesets:", e).printStackTrace();
        }
    }

    private void saveAudioResources(LoadingDialog dialog, int progress, ZipOutputStream zip, File res) throws Exception{
        var audioFolder = new File(res, "audio");
        var dso = new DataSaveObject();
        dso.putList("categories", audioResources.keySet().stream().toList());
        zip.putNextEntry(new ZipEntry(new File(audioFolder, "audio.dat").getPath()));
        var oStream = new ByteArrayOutputStream();
        dso.save(oStream);
        zip.write(oStream.toByteArray());
        oStream.close();
        var progressPerCat = (double) progress / audioResources.size();
        for(var entry: audioResources.entrySet()) {
            var progressPerFile = (int)(progressPerCat / entry.getValue().size());
            var audioDataList = entry.getValue().values().stream().map(audio -> {
                var audioDSO = audio.save(new DataSaveObject());
                dialog.addProgress(progressPerFile);
                return audioDSO;
            }).toList();
            var category = new DataSaveObject();
            category.putList("data", audioDataList);
            zip.putNextEntry(new ZipEntry(new File(audioFolder, entry.getKey()).getPath()));
            oStream = new ByteArrayOutputStream();
            category.save(oStream);
            zip.write(oStream.toByteArray());
            oStream.close();
        }
    }
}
