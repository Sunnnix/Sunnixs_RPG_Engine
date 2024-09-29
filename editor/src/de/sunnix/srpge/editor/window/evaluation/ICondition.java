package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;

public interface ICondition extends Cloneable{

    void load(DataSaveObject dso);

    default DataSaveObject save(DataSaveObject dso){
        dso.putString("id", getID());
        return dso;
    }

    String getID();

    Object clone();

    String getString(Window window, MapData map, GameObject object);

    Runnable getEditGUI(Window window, MapData map, GameObject object, JPanel content);

}
