package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NumberVariableProvider extends de.sunnix.srpge.engine.evaluation.NumberVariableProvider implements IValueProvider {

    @Override
    public String getText(Window window, MapData map, GameObject object) {
        return String.format("%03d: %s", index, array == Array.INT ? Variables.getIntName(index) : Variables.getFloatName(index));
    }

    @Override
    public Runnable getEditGUI(Window window, MapData map, GameObject object, JPanel content) {
        content.setLayout(new BorderLayout());

        var groupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        var group = new ButtonGroup();
        var intRadio = new JRadioButton("Integer");
        var floatRadio = new JRadioButton("Floating");
        group.add(intRadio);
        group.add(floatRadio);
        groupPanel.add(intRadio);
        groupPanel.add(floatRadio);
        content.add(groupPanel, BorderLayout.NORTH);

        var indexCombo = new JComboBox<String>();
        var model = (DefaultComboBoxModel<String>) indexCombo.getModel();
        content.add(indexCombo, BorderLayout.CENTER);

        // Set listeners
        intRadio.addActionListener(l -> {
            model.removeAllElements();
            model.addAll(getNamings(true));
        });
        floatRadio.addActionListener(l -> {
            model.removeAllElements();
            model.addAll(getNamings(false));
        });

        // Set values
        if(array == Array.INT)
            intRadio.doClick();
        else
            floatRadio.doClick();
        indexCombo.setSelectedIndex(index);

        return () -> {
            array = intRadio.isSelected() ? Array.INT : Array.FLOAT;
            index = indexCombo.getSelectedIndex();
        };
    }

    private List<String> getNamings(boolean isInt){
        var size = isInt ? Variables.getIntsSize() : Variables.getFloatsSize();
        var names = new ArrayList<String>(size);
        for(var i = 0; i < size; i++)
            names.add(String.format("%03d: %s", i, isInt ? Variables.getIntName(i) : Variables.getFloatName(i)));
        return names;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
