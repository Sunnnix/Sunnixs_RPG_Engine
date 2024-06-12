package de.sunnix.srpge.editor.window.object.event;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public abstract class Event implements Cloneable {

    public static final byte BLOCK_UPDATE = 0b1;
    public static final byte BLOCK_RENDERING = 0b10;

    @Getter
    protected byte blockingType = 0;

    public final String ID;

    public Event(String id){
        this.ID = id;
    }

    public final DataSaveObject _load(DataSaveObject dso){
        return load(dso);
    }

    public abstract DataSaveObject load(DataSaveObject dso);

    public final DataSaveObject _save(DataSaveObject dso){
        return save(dso);
    }

    public abstract DataSaveObject save(DataSaveObject dso);

    protected abstract String getGUIText(MapData map);

    /**
     * html color command<br>
     * something like:<br>
     * - "/c000" for Hex-Color (RGB)<br>
     * - "/cv00" for saved color variable<br>
     * - "/cx" for base color<br>
     * - "" for default color
     */
    protected abstract String getMainColor();

    protected abstract String getEventDisplayName();

    public final String getString(MapData map){
        return getMainColor() + "/b" + getEventDisplayName() + "/n " + getGUIText(map);
    }

    /**
     * Creates the GUI component of the dialog for editing the event
     *
     * @param currentObject current GameObject
     * @param contentPanel  the panel to display this event
     * @return called when the event is saved
     */
    protected abstract Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel);

    /**
     * Opens a Dialog to edit this event
     * @return if the dialog was canceled / not saved
     */
    public final boolean openDialog(JDialog parent, GameData gameData, MapData map, GameObject currentObject){
        var panel = new JPanel(new BorderLayout());
        var onSave = createEventEditDialog(gameData, map, currentObject, panel);
        var dialog = new EventEditDialog(parent, Language.getString("event_dialog.edit", getEventDisplayName()), panel);
        var saved = dialog.isSaved();
        if(saved && onSave != null)
            onSave.run();
        return saved;
    }

    @Override
    public Event clone() {
        try {
            return (Event) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
