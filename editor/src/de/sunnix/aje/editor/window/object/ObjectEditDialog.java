package de.sunnix.aje.editor.window.object;

import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;

import javax.swing.*;

import java.awt.*;

import static de.sunnix.aje.editor.lang.Language.getString;
import static de.sunnix.aje.editor.util.Texts.WINDOW_NAME;

public class ObjectEditDialog extends JDialog {


    private final Window window;
    private final MapData map;
    private final GameObject object;

    public ObjectEditDialog(Window window, MapData map, GameObject obj) {
        super(window, WINDOW_NAME + " - " + getString("dialog_object.title"), true);
        this.window = window;
        this.map = map;
        this.object = obj;

        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5 ,5 ,5 , 5));

        add(setupProperties(), BorderLayout.EAST);
        add(setupEventList(), BorderLayout.CENTER);

        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
    }

    private JPanel setupProperties() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.set(0, 7, 5, 7);

        panel.add(new JLabel(getString("ID")), gbc);
        gbc.gridx++;

        var id = new JTextField(10);
        id.setText(Long.toString(object.ID));
        id.setEditable(false);
        panel.add(id, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        panel.add(new JLabel(getString("name.name")), gbc);
        gbc.gridx++;

        var name = new JTextField(object.getName(), 10);
        panel.add(name, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        var tPanel = new JPanel(new FlowLayout());
        tPanel.add(panel);
        return tPanel;
    }

    private JPanel setupEventList() {
        var panel = new JPanel(new BorderLayout());

        var scroll = new JScrollPane();
        scroll.setPreferredSize(new Dimension(600, 800));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

}
