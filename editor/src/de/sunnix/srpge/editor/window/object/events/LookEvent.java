package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.ecs.Direction;

import javax.swing.*;
import java.awt.*;

public class LookEvent extends de.sunnix.srpge.engine.ecs.event.LookEvent implements IEvent {

    public LookEvent(){
        objectID = -1;
        lookAtObjID = -1;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putBool("static_look", staticLook);
        dso.putInt("object", objectID);
        if(!staticLook)
            dso.putInt("look_at_obj", lookAtObjID);
        else
            dso.putByte("dir", (byte) direction.ordinal());
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder("at ");
        if(staticLook)
            sb.append(getVarColoring(direction));
        else
            sb.append(getVarColoring(lookAtObjID == 999 ? window.getPlayer() : map.getObject(lookAtObjID)));
        sb.append(" with ").append(getVarColoring(objectID == 999 ? window.getPlayer() : map.getObject(objectID)));
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "/cff8";
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

        var objList = map.getObjects();
        objList.add(0, window.getPlayer());

        gbc.gridwidth = 2;
        content.add(new JLabel("Looking Object"), gbc);
        gbc.gridy++;
        var objCombo = new JComboBox<>(objList.toArray(GameObject[]::new));
        content.add(objCombo, gbc);
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
        var lookCombo = new JComboBox<>(objList.toArray(GameObject[]::new));
        lookCombo.setEnabled(false);
        content.add(dirCombo, gbc);
        gbc.gridx++;
        content.add(lookCombo, gbc);
        gbc.gridx--;
        gbc.gridy++;

        // Listeners
        staticCheck.addChangeListener(l -> dirCombo.setEnabled(staticCheck.isSelected()));
        objectCheck.addChangeListener(l -> lookCombo.setEnabled(objectCheck.isSelected()));

        // Set values
        objCombo.setSelectedItem(objectID == -1 ? currentObject : map.getObject(objectID));
        dirCombo.setSelectedItem(direction);
        lookCombo.setSelectedItem(map.getObject(lookAtObjID));
        if(staticLook)
            staticCheck.setSelected(true);
        else
            objectCheck.setSelected(true);

        return () -> {
            staticLook = staticCheck.isSelected();
            objectID = objCombo.getSelectedIndex() == -1 ? -1 : ((GameObject)objCombo.getSelectedItem()).ID;
            direction = (Direction) dirCombo.getSelectedItem();
            lookAtObjID = lookCombo.getSelectedIndex() == -1 ? -1 : ((GameObject)lookCombo.getSelectedItem()).ID;
        };
    }
}
