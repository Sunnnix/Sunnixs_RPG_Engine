package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class WaitEvent extends de.sunnix.srpge.engine.ecs.event.WaitEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("f", frames);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return Language.getString("event.wait.info", frames);
    }

    @Override
    public String getMainColor() {
        return "/ca0a";
    }

    @Override
    public String getEventDisplayName() {
        return Language.getString("event.wait.name");
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        var time = new JSpinner(new SpinnerNumberModel(frames, 1, Integer.MAX_VALUE, 1));

        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets.set(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        content.add(new JLabel(Language.getString("event.wait.dialog.frames")), gbc);
        gbc.gridx++;
        content.add(time, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        return () -> {
            frames = ((Number) time.getValue()).intValue();
        };
    }
}
