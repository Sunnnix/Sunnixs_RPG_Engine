package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

import static de.sunnix.srpge.engine.ecs.event.MoveEvent.MoveEventHandle.*;

/**
 * Represents an event that handles the movement of a game object within a world.
 * This event allows setting a target position and speed for an object to move towards.
 * The movement can be controlled by different handling mechanisms if the path is blocked.<br>
 *<br>
 * <p>The movement is defined in terms of x, y, and z coordinates, along with a speed parameter.
 * Depending on the state of the movement, it can either cancel the movement, wait for the completion,
 * or try continue moving without interruption. The movement behavior is controlled using the
 * {@link MoveEventHandle} enumeration.</p>
 *<br>
 * <p>This event is commonly used for pathfinding and NPC movement within a game world, where the
 * object's position is updated based on the specified movement attributes. The event includes
 * functionality for saving and loading its state, as well as displaying GUI elements to edit the
 * movement properties.</p>
 *
 * @see EventList
 * @see IEvent
 * @see GameObject
 * @see MoveEventHandle MoveEventHandle
 * @see de.sunnix.srpge.engine.ecs.event.MoveEvent Engine -> MoveEvent
 *
 */
public class MoveEvent extends de.sunnix.srpge.engine.ecs.event.MoveEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("object", object);
        if(movX != 0)
            dso.putFloat("x", movX);
        if(movY != 0)
            dso.putFloat("y", movY);
        if(movZ != 0)
            dso.putFloat("z", movZ);
        dso.putFloat("s", speed);
        if(onBlockHandle != NONE)
            dso.putByte("handle", (byte) onBlockHandle.ordinal());
        if(parallel)
            dso.putBool("parallel", true);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var txt = Language.getString("event.move.info", map.getObject(object), movX, movY, movZ, speed, Math.max(Math.abs(movX), Math.abs(movZ)) / speed / 60);
        txt = switch (onBlockHandle){
            case CANCEL_MOVEMENT -> txt + " " + getVarColoring("cancel");
            case WAIT_FOR_COMPLETION -> txt + " " + getVarColoring("wait");
            default -> txt;
        };
        return txt;
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return Language.getString("event.move.name");
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        JComboBox<String> objects;
        JSpinner tf_x, tf_y, tf_z, tf_speed;

        var current = -1;
        var oList = map.getObjects();
        for(var i = 0; i < oList.size(); i++)
            if(oList.get(i).ID == object) {
                current = i;
                break;
            }
        if(current == -1)
            current = oList.indexOf(currentObject);

        objects = new JComboBox<>(oList.stream().map(o -> String.format("%03d: %s", o.ID, o.getName() == null ? "" : o.getName())).toArray(String[]::new));
        objects.setSelectedIndex(current);

        tf_x = new JSpinner(new SpinnerNumberModel(movX, -10000, 10000, .1f));
        tf_y = new JSpinner(new SpinnerNumberModel(movY, -1000, 10000, .1f));
        tf_z = new JSpinner(new SpinnerNumberModel(movZ, -10000, 10000, .1f));

        tf_speed = new JSpinner(new SpinnerNumberModel(speed < .005 ? .005 : speed, .005, 1, .005));

        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets.set(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        content.add(new JLabel(Language.getString("event.move.dialog.object")), gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        content.add(objects, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("X:"), gbc);
        gbc.gridx++;
        content.add(tf_x, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Y:"), gbc);
        gbc.gridx++;
        content.add(tf_y, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("Z:"), gbc);
        gbc.gridx++;
        content.add(tf_z, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel(Language.getString("event.move.dialog.speed")), gbc);
        gbc.gridx++;
        content.add(tf_speed, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        content.add(new JLabel("On block handle:"), gbc);
        gbc.gridy++;

        var group = new ButtonGroup();
        var handleNoneBtn = new JRadioButton("None", onBlockHandle == NONE);
        group.add(handleNoneBtn);
        content.add(handleNoneBtn, gbc);
        gbc.gridy++;
        var handleCancelMovementBtn = new JRadioButton("Cancel movement", onBlockHandle == CANCEL_MOVEMENT);
        group.add(handleCancelMovementBtn);
        content.add(handleCancelMovementBtn, gbc);
        gbc.gridy++;
        var handleWaitForCompletionBtn = new JRadioButton("Wait for completion", onBlockHandle == WAIT_FOR_COMPLETION);
        group.add(handleWaitForCompletionBtn);
        content.add(handleWaitForCompletionBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 3;
        var runParallelCheck = new JCheckBox("Run parallel", parallel);
        content.add(runParallelCheck, gbc);

        return () -> {
            var index = objects.getSelectedIndex();
            object = index == -1 ? index : oList.get(index).ID;
            movX = ((Number) tf_x.getValue()).floatValue();
            movY = ((Number) tf_y.getValue()).floatValue();
            movZ = ((Number) tf_z.getValue()).floatValue();
            speed = ((Number) tf_speed.getValue()).floatValue();
            onBlockHandle = handleNoneBtn.isSelected() ? NONE : handleCancelMovementBtn.isSelected() ? CANCEL_MOVEMENT : WAIT_FOR_COMPLETION;
            parallel = runParallelCheck.isSelected();
        };
    }
}
