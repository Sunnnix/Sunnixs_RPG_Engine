package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public interface IEvent extends Cloneable {

    String getID();

    DataSaveObject save(DataSaveObject dso);

    void load(DataSaveObject dso);

    String getGUIText(MapData map);

    /**
     * html color command<br>
     * something like:<br>
     * - "/c000" for Hex-Color (RGB)<br>
     * - "/cv00" for saved color variable<br>
     * - "/cx" for base color<br>
     * - "" for default color
     */
    String getMainColor();

    String getEventDisplayName();

    default String getString(MapData map){
        return getMainColor() + "/b" + getEventDisplayName() + "/n " + getGUIText(map);
    }

    /**
     * Creates the GUI component of the dialog for editing the event
     *
     * @param currentObject current GameObject
     * @param contentPanel  the panel to display this event
     * @return called when the event is saved
     */
    Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel);

    /**
     * Opens a Dialog to edit this event
     * @return if the dialog was canceled / not saved
     */
    default boolean openDialog(JDialog parent, GameData gameData, MapData map, GameObject currentObject){
        var panel = new JPanel(new BorderLayout());
        var onSave = createEventEditDialog(gameData, map, currentObject, panel);
        var dialog = new EventEditDialog(parent, Language.getString("event_dialog.edit", getEventDisplayName()), panel);
        var saved = dialog.isSaved();
        if(saved && onSave != null)
            onSave.run();
        return saved;
    }

    Object clone();

}
