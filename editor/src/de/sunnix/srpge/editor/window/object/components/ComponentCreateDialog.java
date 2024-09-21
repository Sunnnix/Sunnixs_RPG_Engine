package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.srpge.editor.window.object.ObjectEditDialog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentCreateDialog extends JDialog {

    private List<Component> createdComponents = Collections.emptyList();

    public static List<Component> show(ObjectEditDialog parent, List<Component> compList){
        var dialog = new ComponentCreateDialog(parent, compList);
        return dialog.createdComponents;
    }

    public ComponentCreateDialog(ObjectEditDialog parent, List<Component> compList) {
        super(parent, "Add Component", true);
        var panel = new JPanel(new GridLayout(0, 1, 3, 3));
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        ComponentRegistry.getAll().forEach(comp -> {
            var btn = new JButton(comp.t2());
            if(compList.stream().anyMatch(c -> c.ID.equals(comp.t1())))
                btn.setEnabled(false);
            else
                btn.addActionListener(a -> {
                    createdComponents = genCompStruct(compList.stream().map(c -> c.ID).collect(Collectors.toList()), comp.t1());
                    dispose();
                });
            panel.add(btn);
        });

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public List<Component> genCompStruct(List<String> presentEvents, String id){
        if(presentEvents.stream().anyMatch(c -> c.equals(id)))
            return Collections.emptyList();
        var comp = ComponentRegistry.createComponent(id);
        if(comp == null)
            return Collections.emptyList();
        presentEvents.add(comp.ID);
        var dependencies = comp.getDependencies();
        if(dependencies.length > 0){
            var list = new ArrayList<Component>();
            for(var dependency: dependencies)
                list.addAll(genCompStruct(presentEvents, dependency));
            list.add(comp);
            return list;
        } else
            return List.of(comp);
    }

}
