package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;

import javax.swing.*;

public class NULLEvent extends de.sunnix.srpge.engine.ecs.event.NULLEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        return dso;
    }

    @Override
    public String getGUIText(MapData map) {
        return "";
    }

    @Override
    public String getMainColor() {
        return "/c333";
    }

    @Override
    public String getEventDisplayName() {
        return "NULL";
    }

    @Override
    public Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        return () -> {};
    }
}
