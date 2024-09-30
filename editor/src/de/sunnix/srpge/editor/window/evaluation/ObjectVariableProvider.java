package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class ObjectVariableProvider extends de.sunnix.srpge.engine.evaluation.ObjectVariableProvider implements IValueProvider {

    @Override
    public String getText(Window window, MapData map, GameObject object) {
        return String.format("%s [%01d]", map.getObject(objectID), index);
    }

    @Override
    public Runnable getEditGUI(Window window, MapData map, GameObject object, JPanel content) {
        content.setLayout(new BorderLayout());

        var objectSelect = new JComboBox<>(map.getObjects().toArray(GameObject[]::new));
        content.add(objectSelect, BorderLayout.NORTH);

        var indexSpinner = new JSpinner(new SpinnerNumberModel(index, 0, de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1, 1));
        content.add(indexSpinner, BorderLayout.CENTER);

        // Set values
        if(objectID == -1)
            objectSelect.setSelectedItem(object);
        else
            objectSelect.setSelectedItem(map.getObject(objectID));

        return () -> {
            objectID = objectSelect.getSelectedIndex() == -1 ? -1 : ((GameObject)objectSelect.getSelectedItem()).ID;
            index = ((Number)indexSpinner.getValue()).intValue();
        };
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        IValueProvider.super.save(dso);
        dso.putInt("object", objectID);
        dso.putInt("index", index);
        return dso;
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
