package de.sunnix.aje.editor.window.object.event;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
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
        return String.format("/cv00 /b %s /n /cx frames", frames);
    }

    @Override
    protected String getMainColor() {
        return "/ca0a";
    }

    @Override
    protected String getEventDisplayName() {
        return "Wait";
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

        content.add(new JLabel("Frames:"), gbc);
        gbc.gridx++;
        content.add(time, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        return () -> {
            frames = ((Number) time.getValue()).intValue();
        };
    }
}
