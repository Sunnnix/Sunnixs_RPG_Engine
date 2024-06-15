package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public abstract class Component implements Cloneable{

    public final String ID;

    public Component(String id){
        this.ID = id;
    }

    public abstract String genName();

    public abstract DataSaveObject load(DataSaveObject dso);

    public abstract DataSaveObject save(DataSaveObject dso);

    public abstract void createView(Window window, GameObject object, JPanel parent);

    protected <T extends JComponent> T addView(JPanel parent, T component){
        return addView(parent, component, component.getPreferredSize().height);
    }

    protected <T extends JComponent> T addView(JPanel parent, T component, int height){
        component.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
        component.setAlignmentX(JButton.CENTER_ALIGNMENT);
        parent.add(component);
        return component;
    }

    @Override
    public Component clone() {
        try {
            return (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
