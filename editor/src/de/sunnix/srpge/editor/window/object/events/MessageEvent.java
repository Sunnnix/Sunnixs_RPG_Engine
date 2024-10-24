package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.srpge.editor.util.StringToHTMLConverter;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.graphics.gui.SpeechBox;
import de.sunnix.srpge.engine.graphics.gui.text.Text;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

import static de.sunnix.srpge.editor.util.StringToHTMLConverter.fat;

public class MessageEvent extends de.sunnix.srpge.engine.ecs.event.MessageEvent implements IEvent {

    private EventList onYes, onNo;

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        if(!name.isBlank())
            dso.putArray("name", name.getBytes(StandardCharsets.UTF_8));
        dso.putArray("msg", message.getBytes(StandardCharsets.UTF_8));
        dso.putBool("yn", yesNoOption);
        if(soundType != SpeechBox.SoundType.NONE)
            dso.putByte("st", (byte) soundType.ordinal());
        if(onYes != null)
            dso.putObject("y_events", onYes.save(new DataSaveObject()));
        if(onNo != null)
            dso.putObject("n_events", onNo.save(new DataSaveObject()));
        return dso;
    }

    @Override
    public void load(DataSaveObject dso) {
        name = new String(dso.getByteArray("name"), StandardCharsets.UTF_8);
        message = new String(dso.getByteArray("msg"), StandardCharsets.UTF_8);
        soundType = SpeechBox.SoundType.values()[dso.getByte("st", (byte)0)];
        yesNoOption = dso.getBool("yn", false);
        if(yesNoOption){
            onYes = new EventList(dso.getObject("y_events"));
            onNo = new EventList(dso.getObject("n_events"));
        }
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return varText(!name.isBlank() ? name + ":" : "");
    }

    @Override
    public String getMainColor() {
        return "#8c3";
    }

    @Override
    public String getEventDisplayName() {
        return yesNoOption ? "Yes No Message" : Language.getString("event.msg.name");
    }

    public EventListTreeView.EventNode getTreeNode(){
        var node = new EventListTreeView.EventNode(this);
        onChangeEvent(node);
        return node;
    }

    private String getText(int row){
        var lines = message.split("\n");
        if(row >= lines.length)
            return "- ";
        return "- " + varText(lines[row]);
    }

    @Override
    public boolean onChangeEvent(EventListTreeView.EventNode node) {
        if(yesNoOption && (node.getChildCount() == 0 || node.getChildCount() == 3)){
            node.removeAllChildren();
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(0))));
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(1))));
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(2))));
            node.add(onYes.genListNode(StringToHTMLConverter.formatSimpleToHTML("[" + getMainColor() + ":" + fat("Yes:") + "]")));
            node.add(onNo.genListNode(StringToHTMLConverter.formatSimpleToHTML("[" + getMainColor() + ":" + fat("No:") + "]")));
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML("[" + getMainColor() + ":" + fat("End") + "]")));
            return true;
        } else if(!yesNoOption && (node.getChildCount() == 0 || node.getChildCount() > 3)) {
            node.removeAllChildren();
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(0))));
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(1))));
            node.add(new EventListTreeView.EmptyNode(StringToHTMLConverter.formatSimpleToHTML(getText(2))));
            return true;
        } else {
            ((EventListTreeView.EmptyNode)node.getChildAt(0)).name = StringToHTMLConverter.formatSimpleToHTML(getText(0));
            ((EventListTreeView.EmptyNode)node.getChildAt(1)).name = StringToHTMLConverter.formatSimpleToHTML(getText(1));
            ((EventListTreeView.EmptyNode)node.getChildAt(2)).name = StringToHTMLConverter.formatSimpleToHTML(getText(2));
            return true;
        }
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        contentPanel.setLayout(new BorderLayout());

        var messageProperties = new JPanel(new BorderLayout());
        messageProperties.setBorder(BorderFactory.createTitledBorder("Message"));

        var name = new JTextField(this.name, 30);

        messageProperties.add(name, BorderLayout.NORTH);

        var text = new JTextArea(message, 4, 30);
        messageProperties.add(new JScrollPane(text), BorderLayout.CENTER);

        var specialCharPanel = new JPanel();
        specialCharPanel.setLayout(new BoxLayout(specialCharPanel, BoxLayout.Y_AXIS));

        var stPanel = new JPanel(new GridLayout(1, 0));

        stPanel.add(new JLabel(Language.getString("name.sound_type")));
        var soundType = new JComboBox<>(SpeechBox.SoundType.values());
        soundType.setSelectedItem(this.soundType);
        if(soundType.getSelectedIndex() == -1)
            soundType.setSelectedIndex(0);
        stPanel.add(soundType);

        specialCharPanel.add(stPanel);

        var arrowsPanel = new JPanel(new GridLayout(0, 2));
        arrowsPanel.setBorder(new TitledBorder(Language.getString("event.msg.dialog.arrows")));

        arrowsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.arr_r"), Text.ARROW_RIGHT, text));
        arrowsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.arr_l"), Text.ARROW_LEFT, text));
        arrowsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.arr_u"), Text.ARROW_UP, text));
        arrowsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.arr_d"), Text.ARROW_DOWN, text));

        specialCharPanel.add(arrowsPanel);

        var controllerButtonsPanel = new JPanel(new GridLayout(0, 2));
        controllerButtonsPanel.setBorder(new TitledBorder(Language.getString("event.msg.dialog.buttons")));

        controllerButtonsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.btn_x"), Text.XBOX_X, text));
        controllerButtonsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.btn_y"), Text.XBOX_Y, text));
        controllerButtonsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.btn_b"), Text.XBOX_B, text));
        controllerButtonsPanel.add(createSpecialButton(Language.getString("event.msg.dialog.btn_a"), Text.XBOX_A, text));

        specialCharPanel.add(controllerButtonsPanel);

        var techPanel = new JPanel(new GridLayout(0, 2));
        techPanel.setBorder(new TitledBorder(Language.getString("event.msg.dialog.tech")));

        techPanel.add(createSpecialButton(Language.getString("event.msg.dialog.token_wait"), Text.STOP_TOKEN, text));

        specialCharPanel.add(techPanel);

        messageProperties.add(specialCharPanel, BorderLayout.SOUTH);

        contentPanel.add(messageProperties, BorderLayout.NORTH);

        var yesNoProperties = new JPanel(new GridBagLayout());
        yesNoProperties.setBorder(new TitledBorder("Yes/No Options"));
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        var yesNoOptionCheck = new JCheckBox("As Yes/No Message");
        yesNoProperties.add(yesNoOptionCheck, gbc);
        gbc.gridy++;

        if(yesNoOption)
            yesNoOptionCheck.doClick();

        contentPanel.add(yesNoProperties, BorderLayout.SOUTH);

        return () -> {
            message = text.getText();
            this.name = name.getText();
            this.soundType = (SpeechBox.SoundType) soundType.getSelectedItem();
            if(this.yesNoOption != yesNoOptionCheck.isSelected()){
                this.yesNoOption = !this.yesNoOption;
                if(this.yesNoOption) {
                    onYes = new EventList();
                    onNo = new EventList();
                } else {
                    onYes = null;
                    onNo = null;
                }
            }
        };
    }

    private JButton createSpecialButton(String name, char c, JTextArea area){
        var b = new JButton(name);
        b.setFocusable(false);
        b.addActionListener(l -> {
            var sb = new StringBuilder(area.getText());
            var pos = area.getCaretPosition();
            sb.insert(pos, c);
            area.setText(sb.toString());
            area.setCaretPosition(pos + 1);
            area.grabFocus();
        });
        return b;
    }

    @Override
    public Object clone() {
        var clone = (MessageEvent) super.clone();
        if(yesNoOption) {
            clone.onYes = onYes.clone();
            clone.onNo = onNo.clone();
        }
        return clone;
    }
}
