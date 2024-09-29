package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChangeVariableEvent extends de.sunnix.srpge.engine.ecs.event.ChangeVariableEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putByte("array", (byte) array.ordinal());
        dso.putInt("index", index);
        dso.putDouble("value", value.doubleValue());
        dso.putByte("op", (byte) operation.ordinal());
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder();
        sb.append(getVarColoring(switch (array){
            case INT -> String.format("I [%03d] %s", index, Variables.getIntName(index));
            case FLOAT -> String.format("F [%03d] %s", index, Variables.getFloatName(index));
            case BOOL -> String.format("B [%03d] %s", index, Variables.getBoolName(index));
        }));
        if(array == Array.BOOL){
            if(operation == Operation.SET) {
                sb.append(" to ");
                sb.append(getVarColoring(value.intValue() != 0));
            } else
                sb.append(getVarColoring(" invert"));
        } else {
            switch (operation) {
                case SET -> sb.append(" to ");
                case INC -> sb.append(" increase by ");
                case DEC -> sb.append(" decrease by ");
            }
            if(array == Array.INT)
                sb.append(getVarColoring(value.intValue()));
            else
                sb.append(getVarColoring(value.floatValue()));
        }
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
        var panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("Variable type"));
        var group = new ButtonGroup();
        var intRadio = new JRadioButton("Integer");
        var floatRadio = new JRadioButton("Floating");
        var boolRadio = new JRadioButton("Boolean");
        group.add(intRadio);
        group.add(floatRadio);
        group.add(boolRadio);

        panel.add(intRadio);
        panel.add(floatRadio);
        panel.add(boolRadio);
        content.add(panel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        content.add(new JLabel("Index:"), gbc);
        gbc.gridx++;
        var indexNames = new JComboBox<String>();
        content.add(indexNames, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Value:"), gbc);
        gbc.gridx++;

        var valueGBC = gbc.clone();
        var intValue = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        var floatValue = new JSpinner(new SpinnerNumberModel(0f, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        var boolValue = new JComboBox<>(new Boolean[]{ false, true });
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 2;
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("Operation"));
        group = new ButtonGroup();
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

        // Set listeners
        intRadio.addActionListener(l -> {
            if(intRadio.isSelected()) {
                var model = ((DefaultComboBoxModel<String>)indexNames.getModel());
                model.removeAllElements();
                var size = Variables.getIntsSize();
                var list = new ArrayList<String>(size);
                for(var i = 0; i < size; i++)
                    list.add(String.format("%03d: %s", i, Variables.getIntName(i)));
                model.addAll(list);
                indexNames.setSelectedIndex(0);
                content.remove(floatValue);
                content.remove(boolValue);
                content.add(intValue, valueGBC);
                var w = DialogUtils.getWindowForComponent(content);
                w.revalidate();
                w.repaint();
            }
        });
        floatRadio.addActionListener(l -> {
            if(floatRadio.isSelected()) {
                var model = ((DefaultComboBoxModel<String>)indexNames.getModel());
                model.removeAllElements();
                var size = Variables.getFloatsSize();
                var list = new ArrayList<String>(size);
                for(var i = 0; i < size; i++)
                    list.add(String.format("%03d: %s", i, Variables.getFloatName(i)));
                model.addAll(list);
                indexNames.setSelectedIndex(0);
                content.remove(intValue);
                content.remove(boolValue);
                content.add(floatValue, valueGBC);
                var w = DialogUtils.getWindowForComponent(content);
                w.revalidate();
                w.repaint();
            }
        });
        boolRadio.addActionListener(l -> {
            if(boolRadio.isSelected()) {
                var model = ((DefaultComboBoxModel<String>)indexNames.getModel());
                model.removeAllElements();
                var size = Variables.getBoolsSize();
                var list = new ArrayList<String>(size);
                for(var i = 0; i < size; i++)
                    list.add(String.format("%03d: %s", i, Variables.getBoolName(i)));
                model.addAll(list);
                indexNames.setSelectedIndex(0);
                content.remove(intValue);
                content.remove(floatValue);
                content.add(boolValue, valueGBC);
                boolValue.setPreferredSize(intValue.getPreferredSize());
                var w = DialogUtils.getWindowForComponent(content);
                w.revalidate();
                w.repaint();
            }
        });

        // Set values
        switch (array){
            case INT -> intRadio.doClick();
            case FLOAT -> floatRadio.doClick();
            case BOOL -> boolRadio.doClick();
        }
        intValue.setValue(value.intValue());
        floatValue.setValue(value.floatValue());
        boolValue.setSelectedItem(value.intValue() != 0);
        indexNames.setSelectedIndex(index);
        switch (operation){
            case SET -> opSet.setSelected(true);
            case INC -> opInc.setSelected(true);
            case DEC -> opDec.setSelected(true);
        }

        return () -> {
            if(indexNames.getSelectedIndex() == -1){
                JOptionPane.showMessageDialog(content, "Index is not selected!", "Invalid selection", JOptionPane.ERROR_MESSAGE);
                return;
            }
            array = intRadio.isSelected() ? Array.INT : floatRadio.isSelected() ? Array.FLOAT : Array.BOOL;
            index = indexNames.getSelectedIndex();
            switch (array){
                case INT -> value = ((Number) intValue.getValue()).intValue();
                case FLOAT -> value = ((Number) floatValue.getValue()).floatValue();
                case BOOL -> value = ((Boolean) boolValue.getSelectedItem()) ? 1 : 0;
            }
            operation = opSet.isSelected() ? Operation.SET : opInc.isSelected() ? Operation.INC : Operation.DEC;
        };
    }
}
