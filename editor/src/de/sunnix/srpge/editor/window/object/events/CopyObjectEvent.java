package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.ObjectPicker;

import javax.swing.*;
import java.awt.*;

public class CopyObjectEvent extends de.sunnix.srpge.engine.ecs.event.CopyObjectEvent implements IEvent {

    private ObjectValue object = new ObjectValue(), varObject = new ObjectValue();

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        object.load(dso.getObject("obj"));
        varObject.load(dso.getObject("var_obj"));
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putObject("obj", object.save());
        dso.putByte("put_on_var", saveIDType.ordinal());
        if(saveIDType != SaveIDType.NONE){
            dso.putInt("var", variable);
            if(saveIDType == SaveIDType.LOCAL)
                dso.putObject("var_obj", varObject.save());
        }
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        var sb = new StringBuilder(getVarColoring(object.getText(window, map)));
        switch (saveIDType){
            case GLOBAL -> sb.append(" to global variable ")
                    .append(getVarColoring(String.format("[%s]: %s", variable, Variables.getIntName(variable))));
            case LOCAL -> sb.append(" to local variable ")
                    .append(getVarColoring(varObject.getText(window, map)))
                    .append(getVarColoring(String.format("[%s]", variable)));
        }
        return sb.toString();
    }

    @Override
    public String getMainColor() {
        return "";
    }

    @Override
    public String getEventDisplayName() {
        return "Copy Object";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();

        var warningLbl = new JLabel("<html><p style=\"color:#f66\"><b>After copying an object, you must wait one frame<br>for the object to register before you can access it!</b></p></html>", JLabel.CENTER);
        content.add(warningLbl, gbc);
        gbc.gridy++;

        var objectSelect = new ObjectPicker(window, map, false, currentObject, object);
        content.add(objectSelect, gbc);
        gbc.gridy++;

        var saveTypeSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveTypeSelectPanel.setBorder(BorderFactory.createTitledBorder("Save object id"));
        var group = new ButtonGroup();
        var save_type_none = new JRadioButton("None");
        group.add(save_type_none);
        saveTypeSelectPanel.add(save_type_none);
        var save_type_global = new JRadioButton("Global variable");
        group.add(save_type_global);
        saveTypeSelectPanel.add(save_type_global);
        var save_type_local = new JRadioButton("Local variable");
        group.add(save_type_local);
        saveTypeSelectPanel.add(save_type_local);

        content.add(saveTypeSelectPanel, gbc);
        gbc.gridy++;

        var varObjectSelect = new ObjectPicker(window, map, false, currentObject, varObject);
        varObjectSelect.setEnabled(false);
        content.add(varObjectSelect, gbc);
        gbc.gridy++;
        var varPanel = new JPanel(new BorderLayout());
        var varName = new JTextField();
        varName.setEditable(false);
        varPanel.add(varName, BorderLayout.CENTER);
        var varSpinner = new JSpinner();
        varSpinner.setPreferredSize(new Dimension(100 ,25));
        varSpinner.setEnabled(false);
        varPanel.add(varSpinner, BorderLayout.EAST);
        content.add(varPanel, gbc);

        var reset = (Runnable) () -> {
            varObjectSelect.setEnabled(false);
            varSpinner.setEnabled(false);
        };

        var reloadVartext = (Runnable) () -> {
            if(save_type_global.isSelected())
                varName.setText(Variables.getIntName(((Number)varSpinner.getValue()).intValue()));
            else
                varName.setText("");
        };

        // listeners
        save_type_none.addActionListener(l -> {
            reset.run();
            reloadVartext.run();
        });
        save_type_global.addActionListener(l -> {
            reset.run();
            varSpinner.setEnabled(true);
            var max = Variables.getIntsSize() - 1;
            varSpinner.setModel(new SpinnerNumberModel(Math.min(max, variable), 0, max, 1));
            varSpinner.setEditor(new JSpinner.NumberEditor(varSpinner, "#"));
            reloadVartext.run();
        });
        save_type_local.addActionListener(l -> {
            reset.run();
            varObjectSelect.setEnabled(true);
            varSpinner.setEnabled(true);
            var max = de.sunnix.srpge.engine.ecs.GameObject.localVarCount - 1;
            varSpinner.setModel(new SpinnerNumberModel(Math.min(max, variable), 0, max, 1));
            varSpinner.setEditor(new JSpinner.NumberEditor(varSpinner, "#"));
            reloadVartext.run();
        });

        varSpinner.addChangeListener(l -> reloadVartext.run());

        // values
        switch (saveIDType){
            case NONE -> save_type_none.doClick();
            case GLOBAL -> save_type_global.doClick();
            case LOCAL -> save_type_local.doClick();
        }

        reloadVartext.run();

        return () -> {
            object = objectSelect.getNewValue();
            saveIDType = save_type_none.isSelected() ? SaveIDType.NONE : save_type_global.isSelected() ? SaveIDType.GLOBAL : SaveIDType.LOCAL;
            variable = ((Number)varSpinner.getValue()).intValue();
            varObject = varObjectSelect.getNewValue();
        };
    }
}
