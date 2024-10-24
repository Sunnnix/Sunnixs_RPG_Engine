package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.ObjectPicker;
import de.sunnix.srpge.engine.ecs.Direction;

import javax.swing.*;
import java.awt.*;

public class LookEvent extends de.sunnix.srpge.engine.ecs.event.LookEvent implements IEvent {

    private ObjectValue objectID = new ObjectValue();
    private ObjectValue lookAtObjID = new ObjectValue();

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        objectID.load(dso.getObject("obj"));
        if(!staticLook)
            lookAtObjID.load(dso.getObject("look_at_obj"));
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putBool("static_look", staticLook);
        dso.putObject("obj", objectID.save());
        if(!staticLook)
            dso.putObject("look_at_obj", lookAtObjID.save());
        else
            dso.putByte("dir", (byte) direction.ordinal());
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder("at ");
        if(staticLook)
            sb.append(varText(direction));
        else
            sb.append(varText(lookAtObjID.getText(window, map)));
        sb.append(" with ").append(varText(objectID.getText(window, map)));
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "#ff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Look";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.set(3, 3, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 2;
        content.add(new JLabel("Looking Object"), gbc);
        gbc.gridy++;
        var objSelect = new ObjectPicker(window, map, true, currentObject, objectID.clone());
        content.add(objSelect, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        var btnGroup = new ButtonGroup();
        var staticCheck = new JRadioButton("Static");
        var objectCheck = new JRadioButton("Object");
        btnGroup.add(staticCheck);
        btnGroup.add(objectCheck);
        content.add(staticCheck, gbc);
        gbc.gridx++;
        content.add(objectCheck, gbc);
        gbc.gridx--;
        gbc.gridy++;

        content.add(new JLabel("Direction"), gbc);
        gbc.gridx++;
        content.add(new JLabel("Object"), gbc);
        gbc.gridx--;
        gbc.gridy++;
        var dirCombo = new JComboBox<>(Direction.values());
        dirCombo.setEnabled(false);
        var lookObjSelect = new ObjectPicker(window, map, true, currentObject, lookAtObjID.clone());
        lookObjSelect.setEnabled(false);
        content.add(dirCombo, gbc);
        gbc.gridx++;
        content.add(lookObjSelect, gbc);
        gbc.gridx--;
        gbc.gridy++;

        // Listeners
        staticCheck.addChangeListener(l -> dirCombo.setEnabled(staticCheck.isSelected()));
        objectCheck.addChangeListener(l -> lookObjSelect.setEnabled(objectCheck.isSelected()));

        // Set values
        dirCombo.setSelectedItem(direction);
        if(staticLook)
            staticCheck.setSelected(true);
        else
            objectCheck.setSelected(true);

        return () -> {
            staticLook = staticCheck.isSelected();
            objectID = objSelect.getNewValue();
            direction = (Direction) dirCombo.getSelectedItem();
            lookAtObjID = lookObjSelect.getNewValue();
        };
    }
}
