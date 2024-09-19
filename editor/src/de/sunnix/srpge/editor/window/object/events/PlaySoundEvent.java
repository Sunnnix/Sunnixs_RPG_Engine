package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.audio.AudioResource;
import de.sunnix.srpge.editor.window.resource.audio.AudioSpeaker;

import javax.swing.*;
import java.awt.*;

public class PlaySoundEvent extends de.sunnix.srpge.engine.ecs.event.PlaySoundEvent implements IEvent {
    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("sound", sound);
        dso.putBool("useLocation", useLocation);
        dso.putFloat("x", posX);
        dso.putFloat("y", posY);
        dso.putFloat("z", posZ);
        dso.putFloat("g", gain);
        dso.putBool("wait", waitForEnd);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        String s;
        if(useLocation)
            s = Language.getString("event.play_sound.info_with_location", sound, (int)(gain * 100), posX, posY, posZ, waitForEnd);
        else
            s = Language.getString("event.play_sound.info_no_location", sound, (int)(gain * 100), waitForEnd);
        return s;
    }

    @Override
    public String getMainColor() {
        return "/c4c4";
    }

    @Override
    public String getEventDisplayName() {
        return Language.getString("event.play_sound.name");
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        var speaker = new AudioSpeaker();

        contentPanel.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets.set(3, 5, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        var boxes = window.getSingleton(Resources.class).audio.getSelectionBoxes(true);

        contentPanel.add(new JLabel(Language.getString("name.category")), gbc);
        gbc.gridx++;
        contentPanel.add(boxes[0], gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        contentPanel.add(new JLabel(Language.getString("event.play_sound.dialog.sound")), gbc);
        gbc.gridx++;
        contentPanel.add(boxes[1], gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        var cbUseLocation = new JCheckBox(Language.getString("event.play_sound.dialog.use_loc"));
        cbUseLocation.setHorizontalTextPosition(JCheckBox.LEFT);
        cbUseLocation.setEnabled(false);
        gbc.gridwidth = 2;
        contentPanel.add(cbUseLocation, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        contentPanel.add(new JLabel("X"), gbc);
        gbc.gridx++;
        var numPosX = new JSpinner(new SpinnerNumberModel(posX, Integer.MIN_VALUE, Integer.MAX_VALUE, .2));
        numPosX.setEnabled(false);
        contentPanel.add(numPosX, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        contentPanel.add(new JLabel("Y"), gbc);
        gbc.gridx++;
        var numPosY = new JSpinner(new SpinnerNumberModel(posY, Integer.MIN_VALUE, Integer.MAX_VALUE, .2));
        numPosY.setEnabled(false);
        contentPanel.add(numPosY, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        contentPanel.add(new JLabel("Z"), gbc);
        gbc.gridx++;
        var numPosZ = new JSpinner(new SpinnerNumberModel(posZ, Integer.MIN_VALUE, Integer.MAX_VALUE, .2));
        numPosZ.setEnabled(false);
        contentPanel.add(numPosZ, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 2;
        var btnUseObjPos = new JButton(Language.getString("event.play_sound.dialog.use_obj_pos"));
        btnUseObjPos.setEnabled(false);
        btnUseObjPos.addActionListener(l -> {
            numPosX.setValue(currentObject.getX());
            numPosY.setValue(currentObject.getY());
            numPosZ.setValue(currentObject.getZ());
        });
        contentPanel.add(btnUseObjPos, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        cbUseLocation.addActionListener(l -> {
            numPosX.setEnabled(cbUseLocation.isSelected());
            numPosY.setEnabled(cbUseLocation.isSelected());
            numPosZ.setEnabled(cbUseLocation.isSelected());
            btnUseObjPos.setEnabled(cbUseLocation.isSelected());
        });

        boxes[1].addActionListener(l -> {
            AudioResource audio = window.getSingleton(Resources.class).audio.getData((String) boxes[0].getSelectedItem(), (String) boxes[1].getSelectedItem());
            speaker.stop();
            speaker.setAudio(audio);
            if(boxes[0].getSelectedIndex() == 0 || boxes[1].getSelectedIndex() == -1 || audio == null || !audio.isMono()){
                cbUseLocation.setSelected(false);
                cbUseLocation.setEnabled(false);
            } else
                cbUseLocation.setEnabled(true);;

            cbUseLocation.getActionListeners()[0].actionPerformed(l);
        });

        if(sound != null && sound.contains("/")){
            var split = sound.split("/");
            boxes[0].setSelectedItem(split[0]);
            boxes[1].setSelectedItem(split[1]);
            cbUseLocation.setSelected(useLocation);
        }

        speaker.setGain(gain);

        gbc.gridwidth = 2;
        contentPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
        gbc.gridy++;

        var cbWaitForEnd = new JCheckBox(Language.getString("event.play_sound.dialog.wait_for_end"), waitForEnd);
        cbWaitForEnd.setHorizontalTextPosition(JCheckBox.LEFT);
        contentPanel.add(cbWaitForEnd, gbc);
        gbc.gridy++;

        var gainSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, (int)(gain * 100));
        gainSlider.setMajorTickSpacing(100);
        gainSlider.setMinorTickSpacing(20);
        gainSlider.setPaintTicks(true);
        gainSlider.setPaintLabels(true);
        gainSlider.addChangeListener(l -> speaker.setGain(gainSlider.getValue() / 100f));
        contentPanel.add(gainSlider, gbc);
        gbc.gridy++;

        var btnPlay = new JButton(Language.getString("name.play"));
        btnPlay.addActionListener(l -> {
            if(speaker.isPlaying())
                speaker.stop();
            else
                speaker.play();
        });
        contentPanel.add(btnPlay, gbc);

        return () -> {
            if(boxes[0].getSelectedIndex() != 0 && boxes[1].getSelectedIndex() != -1)
                sound = boxes[0].getSelectedItem() + "/" + boxes[1].getSelectedItem();
            else
                sound = null;
            useLocation = cbUseLocation.isSelected();
            posX = ((Number)numPosX.getValue()).floatValue();
            posY = ((Number)numPosY.getValue()).floatValue();
            posZ = ((Number)numPosZ.getValue()).floatValue();
            gain = gainSlider.getValue() / 100f;
            waitForEnd = cbWaitForEnd.isSelected();

            speaker.cleanup();
        };
    }
}
