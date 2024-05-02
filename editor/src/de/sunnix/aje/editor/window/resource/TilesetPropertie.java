package de.sunnix.aje.editor.window.resource;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TilesetPropertie {

    private boolean blocking;

    public TilesetPropertie(){}

    public TilesetPropertie(DataSaveObject data) {
        load(data);
    }

    private void load(DataSaveObject data){
        blocking = data.getBool("blocking", true);
    }

    public DataSaveObject save(DataSaveObject data){
        data.putBool("blocking", blocking);
        return data;
    }


}
