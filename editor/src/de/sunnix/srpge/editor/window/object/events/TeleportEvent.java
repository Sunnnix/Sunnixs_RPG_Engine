package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.DefaultValueComboboxModel;
import de.sunnix.srpge.editor.window.customswing.ObjectPicker;
import de.sunnix.srpge.engine.ecs.Direction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The TeleportEvent class represents an event that teleports a game object to a specific location
 * in a map with a specified transition effect. It extends the base TeleportEvent class from the
 * game's event system and implements the IEvent interface for integration with the event editor.<br>
 * <br>
 * This class includes functionality for saving and loading event data, generating GUI text
 * descriptions, and creating a dialog for event editing. It supports various transition types
 * and allows customization of the teleportation effect.
 */
public class TeleportEvent extends de.sunnix.srpge.engine.ecs.event.TeleportEvent implements IEvent {

    private ObjectValue object = new ObjectValue(), other = new ObjectValue();

    /**
     * Constructs a new TeleportEvent with default values.
     */
    public TeleportEvent(){
        transitionType = TransitionType.NONE;
        transitionTime = 0;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("map", map);
        dso.putObject("obj", object.save());
        dso.putArray("pos", new float[]{ x, y, z });
        dso.putByte("transition_type", (byte) transitionType.ordinal());
        dso.putInt("transition_time", transitionTime);
        if(customTransitionEvent != null)
            dso.putObject("custom_transition_event", ((GlobalColorTintEvent)customTransitionEvent).save(new DataSaveObject()));
        if(!fadeIn)
            dso.putBool("fade_in", false);
        if(facing != null)
            dso.putByte("facing", (byte) facing.ordinal());
        if(toObject){
            dso.putObject("other", other.save());
            dso.putBool("to_other", true);
        }
        return dso;
    }

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        object = new ObjectValue(dso.getObject("obj"));
        if(toObject)
            other = new ObjectValue(dso.getObject("other"));
        var obj = dso.getObject("custom_transition_event");
        if(obj != null) {
            customTransitionEvent = new GlobalColorTintEvent();
            customTransitionEvent.load(obj);
        }
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder();
        sb.append("object ").append(varText(object.getText(window, map)));
        if(this.map != -1 && this.map != map.getID())
            sb.append(" to map ").append(varText(window.getSingleton(GameData.class).getMap(this.map)));
        sb.append(" to ");
        if(toObject)
            sb.append(varText(other.getText(window, map))).append(" + ");
        sb.append(varText(String.format("(%.2f, %.2f, %.2f)", x, y, z)));
        sb.append(". Transition: ").append(varText(transitionType));
        if(transitionTime > 0)
            sb.append(", in ").append(varText(transitionTime)).append(" ms");
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "#ff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Teleport";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject go, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.insets.set(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        var selectMap = createNamedComponent(
                content, gbc, "Map",
                new JComboBox<>(new DefaultValueComboboxModel<>(" - ", window.getSingleton(GameData.class).getMapListNames())),
                false
        );
        selectMap.setPreferredSize(new Dimension(0, 25));

        var selectObj = createNamedComponent(content, gbc, "Object", new ObjectPicker(window, map, true, go, object), false);
        selectObj.setPreferredSize(new Dimension(0, 25));

        gbc.gridwidth = 2;
        var toOtherCheck = new JCheckBox("To other object");
        content.add(toOtherCheck, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        var selectOObj = createNamedComponent(content, gbc, "Other object", new ObjectPicker(window, map, true, go, other), false);
        selectOObj.setPreferredSize(new Dimension(0, 25));
        selectOObj.setEnabled(false);

        var directions = new ArrayList<>(Arrays.stream(Direction.values()).map(Direction::toString).toList());
        directions.add(0, "KEEP");
        var facing = createNamedComponent(content, gbc, "Facing", new JComboBox<>(directions.toArray(String[]::new)), true);

        var fX = createPositionSpinner(content, gbc, "X", x);
        var fY = createPositionSpinner(content, gbc, "Y", y);
        var fZ = createPositionSpinner(content, gbc, "Z", z);

        var selectType = createNamedComponent(content, gbc, "Transition",
                new JComboBox<>(Arrays.copyOf(TransitionType.values(), TransitionType.values().length - 1)),
                true
        );
        var setTime = createNamedComponent(content, gbc, "Time", new JSpinner(new SpinnerNumberModel(transitionTime, 0, 1000, 1)), true);

        gbc.gridwidth = 2;
        var fadeInCheck = new JCheckBox("Fade in", fadeIn);
        content.add(fadeInCheck, gbc);

        // Listeners & co
        selectMap.addActionListener(l -> {
            if(selectMap.getSelectedIndex() > 0) {
                if (toOtherCheck.isSelected())
                    toOtherCheck.doClick();
                toOtherCheck.setEnabled(false);
            } else
                toOtherCheck.setEnabled(true);
        });
        toOtherCheck.addActionListener(l -> {
            selectOObj.setEnabled(toOtherCheck.isSelected());
            if(toOtherCheck.isSelected())
                fY.setModel(new SpinnerNumberModel(((Number) fY.getValue()).doubleValue(), Short.MIN_VALUE, Short.MAX_VALUE, .25));
            else {
                if(((Number)fY.getValue()).floatValue() < 0)
                    fY.setValue(0);
                fY.setModel(new SpinnerNumberModel(((Number) fY.getValue()).doubleValue(), 0, Short.MAX_VALUE, .25));
            }
        });
        selectType.addActionListener(l -> updateTimerState(selectType, setTime));

        // set values
        if(toObject)
            toOtherCheck.doClick();
        facing.setSelectedIndex(this.facing == null ? 0 : this.facing.ordinal() + 1);
        selectType.setSelectedItem(this.transitionType);

        if(this.map != -1){
            var tmp = window.getSingleton(GameData.class).getMap(this.map);
            if(tmp != null)
                selectMap.setSelectedItem(tmp.toString());
        } else
            selectMap.setSelectedIndex(0);

        return () -> {
            this.map = selectMap.getSelectedIndex() == 0 ? -1 : Integer.parseInt(((String)selectMap.getSelectedItem()).substring(0, 4));
            this.object = selectObj.getNewValue();
            this.toObject = toOtherCheck.isSelected();
            this.other = selectOObj.getNewValue();
            this.facing = facing.getSelectedIndex() == 0 ? null : Direction.values()[facing.getSelectedIndex() - 1];
            this.x = ((Number)fX.getValue()).floatValue();
            this.y = ((Number)fY.getValue()).floatValue();
            this.z = ((Number)fZ.getValue()).floatValue();
            this.transitionType = (TransitionType) selectType.getSelectedItem();
            this.transitionTime = ((Number) setTime.getValue()).intValue();
            this.fadeIn = fadeInCheck.isSelected();
        };
    }

    /**
     * Creates a JSpinner for editing position values with the given initial value.
     *
     * @param panel The JPanel to add the spinner to.
     * @param gbc The GridBagConstraints to use for positioning.
     * @param name The label for the spinner.
     * @param initValue The initial value for the spinner.
     * @return The created JSpinner.
     */
    private JSpinner createPositionSpinner(JPanel panel, GridBagConstraints gbc, String name, float initValue){
        return createNamedComponent(panel, gbc, name, new JSpinner(new SpinnerNumberModel(initValue, Short.MIN_VALUE, Short.MAX_VALUE, .25)), true);
    }

    /**
     * Creates a labeled component for the event editor dialog.
     *
     * @param panel The JPanel to add the component to.
     * @param gbc The GridBagConstraints to use for positioning.
     * @param name The label for the component.
     * @param comp The JComponent to add.
     * @param horizontal Whether the label and component should be laid out horizontally.
     * @param <T> The type of the JComponent.
     * @return The created component.
     */
    private <T extends JComponent> T createNamedComponent(JPanel panel, GridBagConstraints gbc, String name, T comp, boolean horizontal){
        if(!horizontal)
            gbc.gridwidth = 2;
        panel.add(new JLabel(name), gbc);
        if(horizontal)
            gbc.gridx++;
        else
            gbc.gridy++;
        panel.add(comp, gbc);
        if(horizontal)
            gbc.gridx = 0;
        else
            gbc.gridwidth = 1;
        gbc.gridy++;
        return comp;
    }

    /**
     * Updates the style of the object selection ComboBox based on the selected object.
     *
     * @param selectObj The JComboBox for selecting objects.
     * @param selectMap The JComboBox for selecting maps.
     */
    private void updateObjectSelectionStyle(JComboBox<GameObject> selectObj, JComboBox<String> selectMap) {
        GameObject selected = (GameObject) selectObj.getSelectedItem();
        if(selected != null && selected.getID() == 999) {
            selectObj.setFont(selectObj.getFont().deriveFont(Font.BOLD));
            selectObj.setForeground(Color.CYAN);
            selectMap.setEnabled(true);
        } else {
            selectObj.setFont(selectObj.getFont().deriveFont(Font.PLAIN));
            selectObj.setForeground(UIManager.getColor("ComboBox.foreground"));
            selectMap.setSelectedIndex(0);
            selectMap.setEnabled(false);
        }
    }

    /**
     * Updates the state of the transition time spinner based on the selected transition type.<br>
     * <br>
     * If the selected transition type is {@link TransitionType#NONE NONE}, the transition time spinner is
     * disabled and its value is set to 0. Otherwise, the spinner is enabled, allowing the user to
     * adjust the transition time.
     *
     * @param selectType The JComboBox for selecting the transition type.
     * @param setTime The JSpinner for setting the transition time.
     */
    private void updateTimerState(JComboBox<TransitionType> selectType, JSpinner setTime) {
        if (selectType.getSelectedItem().equals(TransitionType.NONE)) {
            setTime.setValue(0);
            setTime.setEnabled(false);
        } else {
            setTime.setEnabled(true);
        }
    }

}
