package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class MoveEvent extends de.sunnix.srpge.engine.ecs.event.MoveEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", object);
        if(posX != 0)
            dso.putFloat("x", posX);
        if(posY != 0)
            dso.putFloat("y", posY);
        if(posZ != 0)
            dso.putFloat("z", posZ);
        dso.putFloat("s", speed);
        return dso;
    }

    @Override
    public String getGUIText(MapData map) {
        return Language.getString("event.move.info", map.getObject(object), posX, posY, posZ, speed, Math.max(Math.abs(posX), Math.abs(posZ)) / speed / 60);
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return Language.getString("event.move.name");
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        JComboBox<String> objects;
        JSpinner tf_x, tf_y, tf_z, tf_speed;

        var current = -1;
        var oList = map.getObjects();
        for(var i = 0; i < oList.size(); i++)
            if(oList.get(i).ID == object) {
                current = i;
                break;
            }
        if(current == -1)
            current = oList.indexOf(currentObject);

        objects = new JComboBox<>(oList.stream().map(o -> String.format("%03d: %s", o.ID, o.getName() == null ? "" : o.getName())).toArray(String[]::new));
        objects.setSelectedIndex(current);

        tf_x = new JSpinner(new SpinnerNumberModel(posX, -10, 10000, .1f));
        tf_y = new JSpinner(new SpinnerNumberModel(posY, 0, 10000, .1f));
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

        content.add(new JLabel(Language.getString("event.move.dialog.object")), gbc);
        gbc.gridx++;
        content.add(objects, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("X:"), gbc);
        gbc.gridx++;
        content.add(tf_x, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Y:"), gbc);
        gbc.gridx++;
        content.add(tf_y, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Z:"), gbc);
        gbc.gridx++;
        content.add(tf_z, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel(Language.getString("event.move.dialog.speed")), gbc);
        gbc.gridx++;
        content.add(tf_speed, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        return () -> {
            var index = objects.getSelectedIndex();
            object = index == -1 ? index : oList.get(index).ID;
            posX = ((Number) tf_x.getValue()).floatValue();
            posY = ((Number) tf_y.getValue()).floatValue();
            posZ = ((Number) tf_z.getValue()).floatValue();
            speed = ((Number) tf_speed.getValue()).floatValue();
        };
    }
}
