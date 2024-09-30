package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class ChangeObjectVariableEvent extends de.sunnix.srpge.engine.ecs.event.ChangeObjectVariableEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", objectID);
        dso.putInt("index", index);
        dso.putInt("value", value);
        dso.putByte("op", (byte) operation.ordinal());
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder();
        sb.append(getVarColoring(String.format("[%01d]", index)));
        sb.append(" of ");
        sb.append(getVarColoring(map.getObject(objectID)));

        switch (operation) {
            case SET -> sb.append(" to ");
            case INC -> sb.append(" increase by ");
            case DEC -> sb.append(" decrease by ");
        }

        sb.append(getVarColoring(value));
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Change Variable";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets.set(3, 3, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 2;
        content.add(new JLabel("Object:"), gbc);
        gbc.gridy++;
        var objectSelect = new JComboBox<>(map.getObjects().toArray(GameObject[]::new));
        content.add(objectSelect, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        content.add(new JLabel("Index:"), gbc);
        gbc.gridx++;
        var indexSpinner = new JSpinner(new SpinnerNumberModel(index, 0, de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1, 1));
        content.add(indexSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Value:"), gbc);
        gbc.gridx++;

        var valueSpinner = new JSpinner(new SpinnerNumberModel(value, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        content.add(valueSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 2;
        var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("Operation"));
        var group = new ButtonGroup();
        var opSet = new JRadioButton("Set (=)");
        var opInc = new JRadioButton("Inc (+)");
        var opDec = new JRadioButton("Dec (-)");
        group.add(opSet);
        group.add(opInc);
        group.add(opDec);

        panel.add(opSet);
        panel.add(opInc);
        panel.add(opDec);
        content.add(panel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        // Set values
        objectSelect.setSelectedItem(objectID == -1 ? currentObject : map.getObject(objectID));
        switch (operation){
            case SET -> opSet.setSelected(true);
            case INC -> opInc.setSelected(true);
            case DEC -> opDec.setSelected(true);
        }

        return () -> {
            objectID = objectSelect.getSelectedIndex() == -1 ? -1 : ((GameObject)objectSelect.getSelectedItem()).ID;
            index = ((Number)indexSpinner.getValue()).intValue();
            value = ((Number)valueSpinner.getValue()).intValue();
            operation = opSet.isSelected() ? Operation.SET : opInc.isSelected() ? Operation.INC : Operation.DEC;
        };
    }
}
