package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.States;
import de.sunnix.srpge.engine.ecs.State;

import javax.swing.*;
import java.awt.*;

public class ChangeStateEvent extends de.sunnix.srpge.engine.ecs.event.ChangeStateEvent implements IEvent {
    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", objectID);
        dso.putString("state", state);
        if(add)
            dso.putBool("add", true);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder(" of ");
        sb.append(getVarColoring(objectID == 999 ? window.getPlayer() : map.getObject(objectID)));
        if(add)
            sb.append(" add ");
        else
            sb.append(" remove ");
        var s = States.getState(state);
        sb.append(getVarColoring(String.format("%s [%s]", s.id(), s.priority())));
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Change State";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        content.add(new JLabel("Object:"), gbc);
        gbc.gridx++;
        var objects = map.getObjects();
        objects.add(0, window.getPlayer());
        var selectObject = new JComboBox<>(objects.toArray(GameObject[]::new));
        content.add(selectObject, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("State:"), gbc);
        gbc.gridx++;
        var selectState = new JComboBox<>(States.getStates().toArray(State[]::new));
        var renderer = selectState.getRenderer();
        selectState.setRenderer(((list, value, index, isSelected, cellHasFocus) -> {
            var comp = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            comp.setText(String.format("%s [%s]", value.id(), value.priority()));
            return comp;
        }));
        content.add(selectState, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        var bGroup = new ButtonGroup();
        var addRB = new JRadioButton("Add");
        var removeRB = new JRadioButton("Remove");
        bGroup.add(addRB);
        bGroup.add(removeRB);
        content.add(addRB, gbc);
        gbc.gridx++;
        content.add(removeRB, gbc);

        // Values
        if(objectID == -1)
            selectObject.setSelectedItem(map.getObject(currentObject.getID()));
        else
            selectObject.setSelectedItem(objectID == 999 ? window.getPlayer() : map.getObject(objectID));

        selectState.setSelectedItem(States.getState(state));

        if(add)
            addRB.setSelected(true);
        else
            removeRB.setSelected(true);

        return () -> {
            objectID = selectObject.getSelectedIndex() == -1 ? -1 : ((GameObject)selectObject.getSelectedItem()).ID;
            state = selectState.getSelectedIndex() == -1 ? null : ((State)selectState.getSelectedItem()).id();
            add = addRB.isSelected();
        };
    }
}