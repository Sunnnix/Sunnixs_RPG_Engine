package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.ObjectPicker;

import javax.swing.*;
import java.awt.*;

public class ObjectPropertyEvent extends de.sunnix.srpge.engine.ecs.event.ObjectPropertyEvent implements IEvent {

    private ObjectValue objectID = new ObjectValue();

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        objectID = new ObjectValue(dso.getObject("obj"));
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putObject("obj", objectID.save());
        dso.putBool("enabled", enabled);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return " set " + getVarColoring(objectID.getText(window, map)) + " " + getVarColoring(enabled ? "enabled" : "disabled");
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

        var objectsSelect = new ObjectPicker(window, map, false, currentObject, objectID);
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
        enabledRadio.setSelected(enabled);
        disabledRadio.setSelected(!enabled);

        return () -> {
            objectID = objectsSelect.getNewValue();
            enabled = enabledRadio.isSelected();
        };
    }
}
