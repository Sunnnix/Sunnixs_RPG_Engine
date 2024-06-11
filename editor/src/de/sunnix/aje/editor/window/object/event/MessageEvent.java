package de.sunnix.aje.editor.window.object.event;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.lang.Language;
import de.sunnix.aje.engine.graphics.gui.text.Text;
import de.sunnix.sdso.DataSaveObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class MessageEvent extends Event{

    private String name = "";
    private String message = "";

    public MessageEvent() {
        super("message");
        super.blockingType = BLOCK_UPDATE;
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        name = new String(dso.getByteArray("name"), StandardCharsets.UTF_8);
        message = new String(dso.getByteArray("msg"), StandardCharsets.UTF_8);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        if(!name.isBlank())
            dso.putArray("name", name.getBytes(StandardCharsets.UTF_8));
        dso.putArray("msg", message.getBytes(StandardCharsets.UTF_8));
        return dso;
    }

    @Override
    protected String getGUIText(MapData map) {
        String text;
        if(name.isEmpty())
            text = message;
        else
            text = String.format("%s: %s", name, message);
        return Language.getString("event.msg.info", text.length() > 80 ? (text.substring(0, 80).trim() + "...") : text);
    }

    @Override
    protected String getMainColor() {
        return "/c8c3";
    }

    @Override
    protected String getEventDisplayName() {
        return Language.getString("event.msg.name");
    }

    @Override
    protected Runnable createEventEditDialog(GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        contentPanel.setLayout(new BorderLayout());
        var name = new JTextField(this.name, 30);

        contentPanel.add(name, BorderLayout.NORTH);

        var text = new JTextArea(message, 4, 30);
        contentPanel.add(new JScrollPane(text), BorderLayout.CENTER);

        var specialCharPanel = new JPanel();
        specialCharPanel.setLayout(new BoxLayout(specialCharPanel, BoxLayout.Y_AXIS));

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

        contentPanel.add(specialCharPanel, BorderLayout.SOUTH);

        return () -> {
            message = text.getText();
            this.name = name.getText();
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
}
