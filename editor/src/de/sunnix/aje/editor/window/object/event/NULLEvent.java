package de.sunnix.aje.editor.window.object.event;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;

public class NULLEvent extends Event{

    public NULLEvent() {
        super("NULL");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        return dso;
    }

    @Override
    protected String getGUIText(MapData map) {
        return "";
    }

    @Override
    protected String getMainColor() {
        return "/c333";
    }

    @Override
    protected String getEventDisplayName() {
        return "NULL";
    }

    @Override
    protected Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        return () -> {};
    }
}
