package de.sunnix.srpge.editor.window.resource;

import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.util.LoadingDialog;
import de.sunnix.srpge.editor.window.customswing.DefaultValueComboboxModel;
import de.sunnix.srpge.editor.window.resource.audio.AudioResource;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Getter
public final class Resources {

    public final ResourceList<ImageResource> images = new ResourceList<>("images");
    public final ResourceList<Tileset> tilesets = new ResourceList<>("tilesets");
    public final ResourceList<AudioResource> audio = new ResourceList<>("audio", AudioResource::cleanup);

    public Resources(){}

    public void reset() {
        images.reset();
        tilesets.reset();
        tilesets.addCategory("default");
        audio.reset();
    }

    // ###################    Image Resource    ###################

    public BufferedImage image_getRaw(String path){
        var res = images.getData(path);
        if(res == null)
            return null;
        return res.getImage();
    }

    public String[] image_showSelectDialog(JComponent parent, String title, String current){
        String cat = null, img = null;
        if(current != null) {
            var split = current.split("/");
            if (split.length > 1) {
                cat = split[0];
                img = split[1];
            }
        }
        return image_showSelectDialog(parent, title, cat, img);
    }

    public String[] image_showSelectDialog(JComponent parent, String title, String category, String image){
        var categories = new JComboBox<>(new DefaultValueComboboxModel<>("none", images.getCategoryNames().toArray(String[]::new)));
        var images = new JComboBox<String>();
        categories.addActionListener(a -> {
            images.removeAllItems();
            this.images.getDataNames((String) categories.getSelectedItem()).forEach(images::addItem);
        });
        categories.setSelectedItem(category);
        images.setSelectedItem(image);
        if(!DialogUtils.showMultiInputDialog(parent, title, null, new String[] { "Category", "Image" }, new JComponent[]{ categories, images }))
            return null;
        if(categories.getSelectedIndex() == 0)
            return new String[2];
        return new String[] { (String) categories.getSelectedItem(), (String) images.getSelectedItem()};
    }

    // ###################    Tileset Resource    ###################

    public Collection<String> tileset_getTilesetnames(){
        return tilesets.getDataNames("default");
    }

    public void tileset_add(String name, Tileset tileset){
        tilesets.addData("default", name, tileset);
    }

    public Tileset tileset_get(String tsName) {
        return tilesets.getData("default", tsName);
    }

    public Tileset tileset_remove(String tsName) {
        return tilesets.removeData("default", tsName);
    }

    public BufferedImage tileset_getRaw(String tsName) {
        var ts = tileset_get(tsName);
        if(ts == null)
            return null;
        return image_getRaw(ts.getRes());
    }

    // ###################    Load    ###################

    public void loadResources(LoadingDialog dialog, int progress, ZipFile zip, int[] version) throws Exception {
        reset();
        var resFolder = new File("res");

        loadImageResources(dialog, (int)(progress * .25), zip);
        loadTilesets(dialog, (int)(progress * .10), zip, version);
        loadAudioResources(dialog, (int)(progress * .65), zip, version);
    }

    private void loadImageResources(LoadingDialog dialog, int progress, ZipFile zip){
        images.load(dialog, progress, zip, dso -> {
            try {
                return new ImageResource(dso);
            } catch (IOException e) {
                return null;
            }
        });
    }

    private void loadTilesets(LoadingDialog dialog, int progress, ZipFile zip, int[] version){
        if(version[1] > 5)
            tilesets.load(dialog, progress, zip, Tileset::new);
        else {
            DataSaveObject dso;
            try (var stream = zip.getInputStream(new ZipEntry(new File("res", "tilesets.dat").getPath()))) {
                dso = new DataSaveObject().load(stream);
            } catch (Exception e) {
                dialog.addProgress(progress);
                return;
            }
            var list = dso.<DataSaveObject>getList("tilesets");
            var progressPerFile = (int) ((double) progress / list.size());
            list.forEach(tsData -> {
                var tileset = new Tileset(tsData);
                this.tilesets.addData("default", tileset.getName(), tileset);
                dialog.addProgress(progressPerFile);
            });
        }
    }

    private void loadAudioResources(LoadingDialog dialog, int progress, ZipFile zip, int[] version) throws Exception {
        if(version[1] > 5){
            audio.load(dialog, progress, zip, dso -> {
                try {
                    return new AudioResource(dso);
                } catch (Exception e) {
                    return null;
                }
            });
        } else {
            var audioFolder = new File("res", "audio");
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
                for(var audioDSO: data){
                    var audio = new AudioResource(audioDSO);
                    dialog.addProgress(progressPerFile);
                    this.audio.addData(cat, audio.getName(), audio);
                }
            }
        }
    }

    // ###################    Save    ###################

    public void saveResources(LoadingDialog dialog, int progress, ZipOutputStream zip) {
        saveImageResources(dialog, progress, zip);
        saveTilesets(dialog, progress, zip);
        saveAudioResources(dialog, progress, zip);
    }

    private void saveImageResources(LoadingDialog dialog, int progress, ZipOutputStream zip){
        images.save(dialog, progress, zip, (dso, image) -> {
            try {
                image.save(dso);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveTilesets(LoadingDialog dialog, int progress, ZipOutputStream zip) {
        tilesets.save(dialog, progress, zip, (dso, tileset) -> tileset.save(dso));
    }

    private void saveAudioResources(LoadingDialog dialog, int progress, ZipOutputStream zip){
        audio.save(dialog, progress, zip, (dso, audio) -> audio.save(dso));
    }
}
