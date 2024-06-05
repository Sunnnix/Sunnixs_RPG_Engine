package de.sunnix.aje.editor.window.object.event;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;
import java.awt.*;

public class MessageEvent extends Event{

    private String message = "";

    public MessageEvent() {
        super("message");
        super.blockingType = BLOCK_UPDATE;
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        message = dso.getString("msg", "");
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("msg", message);
        return dso;
    }

    @Override
    protected String getGUIText(MapData map) {
        return String.format("show /cv00 /b %s", message.length() > 20 ? message.substring(0, 80).trim() + "..." : message);
    }

    @Override
    protected String getMainColor() {
        return "/c8c3";
    }

    @Override
    protected String getEventDisplayName() {
        return "Message";
    }

    @Override
    protected Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        contentPanel.setLayout(new BorderLayout());
        var text = new JTextArea(message, 4, 30);
        contentPanel.add(new JScrollPane(text), BorderLayout.CENTER);

        return () -> {
            message = text.getText();
        };
    }
}
