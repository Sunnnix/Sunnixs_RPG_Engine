package de.sunnix.srpge.engine.resources;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.audio.AudioResource;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.graphics.Texture;
import de.sunnix.srpge.engine.graphics.TextureAtlas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Resources {

    private final ResourceList<TextureAtlas> textures = new ResourceList<>("images");
    private final ResourceList<Tileset> tilesets = new ResourceList<>("tilesets");
    private final ResourceList<Sprite> sprites = new ResourceList<>("sprites");
    private final ResourceList<AudioResource> audioResources = new ResourceList<>("audio");

    private static Resources instance;

    public static Resources get(){
        if(instance == null)
            instance = new Resources();
        return instance;
    }

    public void loadResources(ZipFile zip) {
        var resFolder = new File("res");

        loadImageResources(zip, resFolder);
        loadTilesets(zip, resFolder);
        loadSprites(zip);
        loadAudioResources(zip, resFolder);

        loadStates(zip);
    }

    private void loadImageResources(ZipFile zip, File res){
        textures.load(zip, dso -> new TextureAtlas(new ByteArrayInputStream(dso.getByteArray("image")), dso.getInt("width", 1), dso.getInt("height", 1)));
    }

    private void loadTilesets(ZipFile zip, File res){
        tilesets.load(zip, Tileset::new);
    }

    private void loadSprites(ZipFile zip){
        sprites.load(zip, Sprite::new);
    }

    private void loadAudioResources(ZipFile zip, File res) {
        audioResources.load(zip, dso -> {
            try {
                return new AudioResource(dso);
            } catch (Exception e){
                GameLogger.logException("Resource Loading", e);
                return null;
            }
        });
    }

    public TextureAtlas getTexture(String category, String texture){
        return textures.getData(category, texture);
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
        return tilesets.getData("default", name);
    }

    public Texture getTilesetTex(String name){
        var ts = getTileset(name);
        if(ts == null)
            return null;
        return ts.getRes();
    }

    public AudioResource getAudio(String category, String name){
        return audioResources.getData(category, name);
    }

    public AudioResource getAudio(String path){
        if(path == null)
            return null;
        var split = path.split("/");
        if(split.length < 2)
            return null;
        return getAudio(split[0], split[1]);
    }

    public Sprite getSprite(String sprite) {
        return this.sprites.getData(sprite);
    }

    private void loadStates(ZipFile zip) {
        try (var stream = zip.getInputStream(new ZipEntry("res/states"))) {
            if(stream == null)
                return;
            var dso = new DataSaveObject().load(stream);
            States.load(dso);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
