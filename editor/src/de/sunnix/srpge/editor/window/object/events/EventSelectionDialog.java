package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;

public class EventSelectionDialog extends JDialog {

    private final Window window;
    private final java.awt.Window parent;

    @Getter
    private IEvent event;

    public EventSelectionDialog(Window window, java.awt.Window parent, MapData map, GameObject object) {
        super(parent, "", ModalityType.APPLICATION_MODAL);
        this.window = window;
        this.parent = parent;
        create(map, object);

        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void create(MapData map, GameObject object) {
        setLayout(new GridBagLayout());
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        var gbc = FunctionUtils.genDefaultGBC();

        var panels = new ArrayList<JPanel>();

        EventRegistry.getGroups().forEach(g -> {
            var panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.setBorder(new TitledBorder(g));
            var events = EventRegistry.getGroupEvents(g);
            for(var e: events){
                var btn = new JButton(e.t2());
                btn.addActionListener(l -> {
                    var event = EventRegistry.createEvent(e.t1());
                    if(event.openDialog(window, this, window.getSingleton(GameData.class), map, object)) {
                        this.event = event;
                        dispose();
                    }
                });
                panel.add(btn);
            }
            panels.add(panel);
        });

        var lY = 0;
        var rY = 0;

        for(var i = 0; i < panels.size(); i++){
            var panel = panels.get(i);
            gbc.gridheight = panel.getComponentCount() + 2;
            if(i % 2 == 0){
                gbc.gridx = 0;
                gbc.gridy = lY;
                add(panel, gbc);
                lY += panel.getComponentCount() + 2;
            } else {
                gbc.gridx = 1;
                gbc.gridy = rY;
                add(panel, gbc);
                rY += panel.getComponentCount() + 2;
            }
        }

    }

    public static IEvent show(Window window, java.awt.Window parent, MapData map, GameObject object){
        var dialog = new EventSelectionDialog(window, parent, map, object);
        return dialog.event;
    }

}
