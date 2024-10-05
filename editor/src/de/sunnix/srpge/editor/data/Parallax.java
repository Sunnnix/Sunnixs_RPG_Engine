package de.sunnix.srpge.editor.data;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;

import javax.swing.*;
import java.awt.*;

public class Parallax extends de.sunnix.srpge.engine.Parallax {

    public DataSaveObject save(DataSaveObject dso){
        dso.putString("tex", textureID);
        dso.putArray("tempo", new float[]{ vSpeed, hSpeed });
        return dso;
    }

    public Runnable showView(Window window, JPanel content){
        content.setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();

        var boxes = window.getSingleton(Resources.class).images.getSelectionBoxes(true);

        gbc.gridwidth = 2;
        var catSelect = boxes[0];
        content.add(catSelect, gbc);
        gbc.gridy++;
        var texSelect = boxes[1];
        content.add(texSelect, gbc);
        gbc.gridy++;

        gbc.gridwidth = 1;
        content.add(new JLabel("H-Speed:"), gbc);
        gbc.gridx++;
        var hSpeedSpinner = new JSpinner(new SpinnerNumberModel(hSpeed, -10f, 10f, .1f));
        content.add(hSpeedSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        content.add(new JLabel("V-Speed:"), gbc);
        gbc.gridx++;
        var vSpeedSpinner = new JSpinner(new SpinnerNumberModel(vSpeed, -10f, 10f, .1f));
        content.add(vSpeedSpinner, gbc);

        // set values
        if(textureID == null)
            catSelect.setSelectedIndex(0);
        else {
            var split = textureID.split("/");
            if(split.length < 2)
                catSelect.setSelectedIndex(0);
            else {
                catSelect.setSelectedItem(split[0]);
                texSelect.setSelectedItem(split[1]);
            }
        }

        return () -> {
            textureID = catSelect.getSelectedIndex() < 1 ? null : catSelect.getSelectedItem() + "/" + texSelect.getSelectedItem();
            hSpeed = ((Number)hSpeedSpinner.getValue()).floatValue();
            vSpeed = ((Number)vSpeedSpinner.getValue()).floatValue();
        };
    }

}
