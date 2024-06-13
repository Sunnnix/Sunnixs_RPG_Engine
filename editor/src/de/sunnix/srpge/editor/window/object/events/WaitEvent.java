package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;
import java.awt.*;

public class WaitEvent extends Event{

    private int frames = 1;

    public WaitEvent() {
        super("wait");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        frames = dso.getInt("f", 1);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("f", frames);
        return dso;
    }

    @Override
    protected String getGUIText(MapData map) {
        return Language.getString("event.wait.info", frames);
    }

    @Override
    protected String getMainColor() {
        return "/ca0a";
    }

    @Override
    protected String getEventDisplayName() {
        return Language.getString("event.wait.name");
    }

    @Override
    protected Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel content) {
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
