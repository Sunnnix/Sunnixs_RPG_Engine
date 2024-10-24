package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.ObjectPicker;

import javax.swing.*;
import java.awt.*;

public class CameraEvent extends de.sunnix.srpge.engine.ecs.event.CameraEvent implements IEvent {

    private ObjectValue objectID = new ObjectValue();

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        objectID = new ObjectValue(dso.getObject("obj"));
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putBool("attack_obj", attachObject);
        dso.putObject("obj", objectID.save());
        dso.putBool("move_cam", moveCamera);
        dso.putArray("pos", new float[]{ x, y, z });
        dso.putBool("instant", instant);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder();
        if(attachObject)
            sb.append("attach to ").append(varText(objectID.getText(window, map)));
        else
            sb.append("don't attach to object");
        if(moveCamera)
            sb.append(" move to ").append(varText(String.format("(%.2f, %.2f, %.2f)", x, y, z)));
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "#ff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Camera";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.set(3,3,0,0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 2;
        var attachObjectCheck = new JCheckBox("Attach to object");
        content.add(attachObjectCheck, gbc);
        gbc.gridy++;
        var objectSelect = new ObjectPicker(window, map, true, currentObject, objectID);
        content.add(objectSelect, gbc);
        gbc.gridy++;

        content.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
        gbc.gridy++;

        var moveCameraCheck = new JCheckBox("Move camera to");
        content.add(moveCameraCheck, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        content.add(new JLabel("X:"), gbc);
        gbc.gridx++;
        var xSpinner = new JSpinner(new SpinnerNumberModel(x, -1000, 1000, 1));
        content.add(xSpinner, gbc);
        gbc.gridx--;
        gbc.gridy++;
        content.add(new JLabel("Y:"), gbc);
        gbc.gridx++;
        var ySpinner = new JSpinner(new SpinnerNumberModel(y, -1000, 1000, 1));
        content.add(ySpinner, gbc);
        gbc.gridx--;
        gbc.gridy++;
        content.add(new JLabel("Z:"), gbc);
        gbc.gridx++;
        var zSpinner = new JSpinner(new SpinnerNumberModel(z, -1000, 1000, 1));
        content.add(zSpinner, gbc);
        gbc.gridx--;
        gbc.gridy++;
        gbc.gridwidth = 2;
        var setToObjectBtn = new JButton("Set to object");
        setToObjectBtn.setEnabled(map != null);
        content.add(setToObjectBtn, gbc);
        gbc.gridy++;
        var setPosObjectsCombo = new ObjectPicker(window, map, true, currentObject, objectID, false);
        setPosObjectsCombo.setEnabled(map != null);
        content.add(setPosObjectsCombo, gbc);
        gbc.gridy++;
        var moveInstantCheck = new JCheckBox("Move instant", instant);
        content.add(moveInstantCheck, gbc);
        gbc.gridy++;

        // Listeners
        attachObjectCheck.addChangeListener(l -> objectSelect.setEnabled(attachObjectCheck.isSelected()));
        moveCameraCheck.addChangeListener(l -> {
            xSpinner.setEnabled(moveCameraCheck.isSelected());
            ySpinner.setEnabled(moveCameraCheck.isSelected());
            zSpinner.setEnabled(moveCameraCheck.isSelected());
            setToObjectBtn.setEnabled(moveCameraCheck.isSelected());
            setPosObjectsCombo.setEnabled(moveCameraCheck.isSelected());
        });
        setToObjectBtn.addActionListener(l -> {
            var index = setPosObjectsCombo.getNewValue().object;
            var obj = index == -1 ? currentObject : index == 999 ? window.getPlayer() : map.getObject(index);
            if(obj == null)
                return;
            xSpinner.setValue(obj.getX());
            ySpinner.setValue(obj.getY());
            zSpinner.setValue(obj.getZ());
        });

        // Values
        attachObjectCheck.setSelected(true);
        moveCameraCheck.setSelected(true);
        attachObjectCheck.setSelected(attachObject);
        moveCameraCheck.setSelected(moveCamera);

        return () -> {
            attachObject = attachObjectCheck.isSelected();
            objectID = objectSelect.getNewValue();
            moveCamera = moveCameraCheck.isSelected();
            x = ((Number)xSpinner.getValue()).floatValue();
            y = ((Number)ySpinner.getValue()).floatValue();
            z = ((Number)zSpinner.getValue()).floatValue();
            instant = moveInstantCheck.isSelected();
        };
    }
}
