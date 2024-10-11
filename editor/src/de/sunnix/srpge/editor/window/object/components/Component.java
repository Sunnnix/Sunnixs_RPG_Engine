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

    /**
     *
     * @param window main Window
     * @param object current object
     * @param parent parent component panel
     * @return a Runnable which will be called every 16.666 ms<br>can be <b>null</b>
     */
    public abstract Runnable createView(Window window, GameObject object, JPanel parent);

    protected <T extends JComponent> T addView(JPanel parent, T component){
        return addView(parent, component, component.getPreferredSize().height);
    }

    protected <T extends JComponent> T addView(JPanel parent, T component, int height){
        component.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
        component.setPreferredSize(new Dimension(0, height));
        component.setMinimumSize(new Dimension(Short.MAX_VALUE, height));
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

    public String[] getDependencies(){
        return new String[0];
    }

    /**
     * @return should draw default hb
     */
    public boolean onDraw(Window window, GameObject parent, Graphics2D g, float zoom, float offsetX, float offsetY, boolean selected) {
        return true;
    }

    /**
     * Determines the drawing priority<br>
     * Higher values will be drawn later
     */
    public int getRenderPriority(){
        return 0;
    }

    /**
     * Does the position matches the parent object of this component
     * @param x position X of map
     * @param y position Y of map
     * @param parent parent component
     * @return if the coords match the parent
     */
    public boolean intersects(float x, float y, GameObject parent) {
        return false;
    }

}
