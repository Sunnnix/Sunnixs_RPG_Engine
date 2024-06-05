package de.sunnix.aje.editor.window.object;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.GameObject;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.object.event.Event;
import de.sunnix.aje.editor.window.object.event.MoveEvent;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

import static de.sunnix.aje.editor.lang.Language.getString;

public class EventSelectionDialog extends JDialog {

    private final Window window;
    private final ObjectEditDialog parent;

    @Getter
    private Event event;

    public EventSelectionDialog(Window window, ObjectEditDialog parent, MapData map, GameObject object) {
        super(parent, "", true);
        this.window = window;
        this.parent = parent;
        create(map, object);

        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void create(MapData map, GameObject object) {
        setLayout(new GridLayout(0, 1, 5, 5));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        EventRegistry.getAll().forEach(t -> {
            var btn = new JButton(getString(t.t2()));
            btn.addActionListener(l -> {
                var event = EventRegistry.createEvent(t.t1());
                if(event.openDialog(this, window.getSingleton(GameData.class), map, object))
                    this.event = event;
                dispose();
            });
            add(btn);
        });
    }

}
