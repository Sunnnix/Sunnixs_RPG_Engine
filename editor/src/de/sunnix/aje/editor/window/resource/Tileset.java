package de.sunnix.aje.editor.window.resource;

import de.sunnix.aje.editor.window.Window;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

public class Tileset {

    @Getter
    private String name;
    @Getter
    private String res;

    public Tileset(String name, String res){
        this.name = name;
        this.res = res;
    }

    public Tileset(DataSaveObject data) {
        load(data);
    }

    private void load(DataSaveObject data){
        this.name = data.getString("name", null);
        this.res = data.getString("res", null);
    }

    public void save(DataSaveObject data){
        data.putString("name", name);
        data.putString("res", res);
    }

    public ImageResource getImage(Window window, String path){
        return window.getSingleton(Resources.class).image_get(path);
    }

}
