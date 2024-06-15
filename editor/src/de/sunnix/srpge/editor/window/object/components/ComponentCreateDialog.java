package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.object.ObjectEditDialog;

import javax.swing.*;
import java.awt.*;

public class ComponentCreateDialog extends JDialog {

    private Component createdComponent;

    public static Component show(ObjectEditDialog parent, GameObject object){
        var dialog = new ComponentCreateDialog(parent, object);
        return dialog.createdComponent;
    }

    public ComponentCreateDialog(ObjectEditDialog parent, GameObject object) {
        super(parent, "Add Component", true);
        var panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        ComponentRegistry.getAll().forEach(comp -> {
            var btn = new JButton(comp.t2());
            if(object.hasComponent(comp.t1()))
                btn.setEnabled(false);
            else
                btn.addActionListener(a -> {
                    createdComponent = ComponentRegistry.createComponent(comp.t1());
                    dispose();
                });
            panel.add(btn);
        });

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

}
