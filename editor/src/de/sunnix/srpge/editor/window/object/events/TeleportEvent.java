package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.DefaultValueComboboxModel;
import de.sunnix.srpge.engine.util.FunctionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class TeleportEvent extends de.sunnix.srpge.engine.ecs.event.TeleportEvent implements IEvent {

    public TeleportEvent(){
        transitionType = TransitionType.BLACK;
        transitionTime = 60;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("map", map);
        dso.putInt("object", objectID);
        dso.putArray("pos", new float[]{ x, y, z });
        dso.putByte("transition_type", (byte) transitionType.ordinal());
        dso.putInt("transition_time", transitionTime);
        if(customTransitionEvent != null)
            dso.putObject("custom_transition_event", ((GlobalColorTintEvent)customTransitionEvent).save(new DataSaveObject()));
        return dso;
    }

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        var obj = dso.getObject("custom_transition_event");
        if(obj != null) {
            customTransitionEvent = new GlobalColorTintEvent();
            customTransitionEvent.load(obj);
        }
    }

    @Override
    public String getGUIText(MapData map) {
        return String.format("to map /cv00 /b %s /n /cx to /cv00 /b (%.2f, %.2f, %.2f) /n /cx", this.map, x, y, z);
    }

    @Override
    public String getMainColor() {
        return "/cff8";
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

        var selectMap = new JComboBox<>(new DefaultValueComboboxModel<>(" - ", window.getSingleton(GameData.class).getMapListNames()));
        if(this.map != -1){
            var tmp = window.getSingleton(GameData.class).getMap(this.map);
            if(tmp != null)
                selectMap.setSelectedItem(tmp.toString());
        }
        content.add(selectMap, gbc);
        gbc.gridy++;

        var objects = map.getObjects();
        objects.add(window.getPlayer());
        var selectObj = new JComboBox<>(objects.toArray(GameObject[]::new));
        var defRenderer = selectObj.getRenderer();
        selectObj.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            var c = defRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(index == selectObj.getItemCount() - 1) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                c.setForeground(Color.CYAN);
            } else
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            return c;
        });
        selectObj.addActionListener(l -> {
            if(((GameObject) selectObj.getSelectedItem()).getID() == 999){
                selectObj.setFont(selectObj.getFont().deriveFont(Font.BOLD));
                selectObj.setForeground(Color.CYAN);
            } else {
                selectObj.setFont(selectObj.getFont().deriveFont(Font.PLAIN));
                selectObj.setForeground(UIManager.getColor("ComboBox.foreground"));
            }
        });
        var sObj = FunctionUtils.firstOrElse(objects, o -> o.ID == objectID, window.getPlayer());
        selectObj.setSelectedItem(sObj);
        content.add(selectObj, gbc);
        gbc.gridy++;

        var fX = new JSpinner(new SpinnerNumberModel(x, 0, Short.MAX_VALUE, 1));
        content.add(fX, gbc);
        gbc.gridy++;
        var fY = new JSpinner(new SpinnerNumberModel(y, 0, Short.MAX_VALUE, 1));
        content.add(fY, gbc);
        gbc.gridy++;
        var fZ = new JSpinner(new SpinnerNumberModel(z, 0, Short.MAX_VALUE, 1));
        content.add(fZ, gbc);
        gbc.gridy++;

        var selectType = new JComboBox<>(Arrays.copyOf(TransitionType.values(), TransitionType.values().length - 1));
        selectType.setSelectedItem(this.transitionType);
        content.add(selectType, gbc);
        gbc.gridy++;

        var setDelay = new JSpinner(new SpinnerNumberModel(transitionTime, 0, Integer.MAX_VALUE, 1));
        content.add(setDelay, gbc);
        gbc.gridy++;

        return () -> {
            this.map = selectMap.getSelectedIndex() == 0 ? -1 : Integer.parseInt(((String)selectMap.getSelectedItem()).substring(0, 4));
            this.objectID = ((GameObject)selectObj.getSelectedItem()).ID;
            this.x = ((Number)fX.getValue()).floatValue();
            this.y = ((Number)fY.getValue()).floatValue();
            this.z = ((Number)fZ.getValue()).floatValue();
            this.transitionType = (TransitionType) selectType.getSelectedItem();
            this.transitionTime = ((Number)setDelay.getValue()).intValue();
        };
    }
}
