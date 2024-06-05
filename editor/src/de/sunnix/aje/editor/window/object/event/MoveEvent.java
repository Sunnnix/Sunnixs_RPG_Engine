package de.sunnix.aje.editor.window.object.event;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;
import java.awt.*;

public class MoveEvent extends Event {

    private int object = -1;

    private float posX, posZ;
    private float speed = .035f;

    public MoveEvent() {
        super("move");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        object = dso.getInt("object", -1);
        posX = dso.getFloat("x", 0);
        posZ = dso.getFloat("z", 0);
        speed = dso.getFloat("s", .035f);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", object);
        dso.putFloat("x", posX);
        dso.putFloat("z", posZ);
        dso.putFloat("s", speed);
        return dso;
    }

    @Override
    protected String getGUIText(MapData map) {
        return String.format("move /cv00 /b %s /n /cx to /c0f0 /b (%.1f, %.1f) /n /cx speed: /c0f0 /b %.3f /cv00 /b %.2fs", map.getObject(object), posX, posZ, speed, Math.max(Math.abs(posX), Math.abs(posZ)) / speed / 60);
    }

    @Override
    protected String getMainColor() {
        return "/cff8";
    }

    @Override
    protected String getEventDisplayName() {
        return Language.getString("event.name.move");
    }

    @Override
    protected Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        JComboBox<String> objects;
        JSpinner tf_x, tf_z, tf_speed;

        var oList = map.getObjects();
        var current = oList.indexOf(object == -1 ? currentObject : object);

        objects = new JComboBox<>(oList.stream().map(o -> String.format("%03d: %s", o.ID, o.getName() == null ? "" : o.getName())).toArray(String[]::new));
        objects.setSelectedIndex(current);

        tf_x = new JSpinner(new SpinnerNumberModel(posX, -10, 10000, .1f));
        tf_z = new JSpinner(new SpinnerNumberModel(posZ, -10, 10000, .1f));

        tf_speed = new JSpinner(new SpinnerNumberModel(speed < .005 ? .005 : speed, .005, 1, .005));

        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets.set(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        content.add(new JLabel("Object:"), gbc);
        gbc.gridx++;
        content.add(objects, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("X:"), gbc);
        gbc.gridx++;
        content.add(tf_x, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Z:"), gbc);
        gbc.gridx++;
        content.add(tf_z, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Speed:"), gbc);
        gbc.gridx++;
        content.add(tf_speed, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        return () -> {
            var index = objects.getSelectedIndex();
            object = index == -1 ? index : oList.get(index).ID;
            posX = ((Number) tf_x.getValue()).floatValue();
            posZ = ((Number) tf_z.getValue()).floatValue();
            speed = ((Number) tf_speed.getValue()).floatValue();
        };
    }
}
