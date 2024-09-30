package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

/**
 * The IEvent interface represents an editable version of an engine {@link de.sunnix.srpge.engine.ecs.event.Event Event}
 * within the editor. It extends the basic functionality of events in the
 * game engine by providing additional methods for {@link #save(DataSaveObject) saving} and
 * {@link #createEventEditDialog(Window, GameData, MapData, GameObject, JPanel) editing event data through a graphical user interface (GUI) }.<br>
 * <br>
 * This interface is implemented by event classes in the editor, allowing
 * them to interact with the GUI and provide user-friendly event editing
 * capabilities, while still maintaining compatibility with the underlying
 * engine events.
 */
public interface IEvent extends Cloneable {

    /**
     * Returns the unique identifier for this event.
     *
     * @return the event's unique ID as a String
     */
    String getID();

    /**
     * Saves the current data of the event to a {@link DataSaveObject}.
     *
     * @param dso the data object used for saving event data
     * @return the modified {@link DataSaveObject}
     */
    DataSaveObject save(DataSaveObject dso);

    /**
     * Loads the event's data from a {@link DataSaveObject}.
     *
     * @param dso the data object containing saved event data
     */
    void load(DataSaveObject dso);

    /**
     * Provides a textual description of the event for display in the GUI.
     * This text represents the event's current configuration.
     *
     * @param window the editor main window where the event is being displayed
     * @param map the map data in which the event is defined
     * @return a string containing the event's description in the GUI
     */
    String getGUIText(Window window, MapData map);

    /**
     * Returns the main color used for rendering the event in the GUI.
     * The color can be specified using special commands.<br>
     *
     * Examples include:
     * <ul>
     *     <li>"/c000" for a specific RGB hex color</li>
     *     <li>"/cv00" for a saved color variable</li>
     *     <li>"/cx" for the base color</li>
     *     <li>"" for the default color</li>
     * </ul>
     *
     * @return a string representing the main color of the event
     */
    String getMainColor();

    /**
     * Returns the display name of the event, typically used for identifying
     * the event in the editor GUI.
     *
     * @return the display name of the event
     */
    String getEventDisplayName();

    /**
     * Combines the event's main color and its display name with additional
     * details to provide a formatted string for the event in the GUI.
     *
     * @param window the editor main window where the event is being displayed
     * @param map the map data in which the event is defined
     * @return a string containing a formatted representation of the event
     */
    default String getString(Window window, MapData map){
        return getMainColor() + "/b" + getEventDisplayName() + "/n " + getGUIText(window, map);
    }

    /**
     * Creates the dialog GUI for editing the event's properties.
     * This method is responsible for generating and configuring the
     * UI components that allow users to modify the event.
     *
     * @param window the current editor main window
     * @param currentObject the {@link GameObject} to which the event is attached
     * @param contentPanel the panel that will display the event editing interface
     * @return a {@link Runnable} that is invoked when the event is saved
     */
    Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel);

    /**
     * Opens a dialog to edit the event using the GUI created by
     * {@link #createEventEditDialog(Window, GameData, MapData, GameObject, JPanel)}.
     *
     * @param window the current editor main window
     * @param parent the parent dialog
     * @param gameData the current game data
     * @param map the map data in which the event is defined
     * @param currentObject the {@link GameObject} to which the event is attached
     * @return true if the event was saved successfully, false if the dialog was canceled
     */
    default boolean openDialog(Window window, java.awt.Window parent, GameData gameData, MapData map, GameObject currentObject){
        var panel = new JPanel(new BorderLayout());
        var onSave = createEventEditDialog(window, gameData, map, currentObject, panel);
        var dialog = new EventEditDialog(parent, Language.getString("event_dialog.edit", getEventDisplayName()), panel);
        var saved = dialog.isSaved();
        if(saved && onSave != null)
            onSave.run();
        return saved;
    }

    /**
     * Clones the current event instance.
     *
     * @return a clone of this event
     */
    Object clone();

    /**
     * Formats a string using a predefined color variable and applies
     * additional styling, like bold text and resetting to the base color.
     *
     * @param text the text to be formatted
     * @return the formatted string with color commands
     */
    default String getVarColoring(Object text){
        return "/cv00 /b " + text + " /n /cx";
    }

}
