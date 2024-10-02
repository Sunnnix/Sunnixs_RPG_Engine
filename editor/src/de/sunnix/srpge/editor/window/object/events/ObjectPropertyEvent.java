package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class ObjectPropertyEvent extends de.sunnix.srpge.engine.ecs.event.ObjectPropertyEvent implements IEvent {
    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", objectID);
        dso.putBool("enabled", enabled);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return " set " + getVarColoring(map.getObject(objectID)) + " " + getVarColoring(enabled ? "enabled" : "disabled");
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Object Property";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();

        var objectsSelect = new JComboBox<>(map.getObjects().toArray(GameObject[]::new));
        content.add(objectsSelect, gbc);
        gbc.gridy++;

        var enabledBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
        enabledBox.setBorder(BorderFactory.createTitledBorder((String) null));
        var group = new ButtonGroup();
        var enabledRadio = new JRadioButton("Enabled");
        var disabledRadio = new JRadioButton("Disabled");
        group.add(enabledRadio);
        group.add(disabledRadio);
        enabledBox.add(enabledRadio);
        enabledBox.add(disabledRadio);
        content.add(enabledBox, gbc);

        // Set values
        if(objectID == -1)
            objectsSelect.setSelectedItem(currentObject);
        else
            objectsSelect.setSelectedItem(map.getObject(objectID));

        enabledRadio.setSelected(enabled);
        disabledRadio.setSelected(!enabled);

        return () -> {
            objectID = objectsSelect.getSelectedIndex() == -1 ? -1 : ((GameObject)objectsSelect.getSelectedItem()).ID;
            enabled = enabledRadio.isSelected();
        };
    }
}
