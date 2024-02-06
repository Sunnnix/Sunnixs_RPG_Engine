package de.sunnix.engine.graphics.gui;

import de.sunnix.engine.graphics.gui.text.Text;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    static final List<IGUIComponent> components = new ArrayList<>();

    public static void render(){
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        synchronized (components) {
            components.forEach(IGUIComponent::render);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void add(IGUIComponent component) {
        synchronized (components) {
            components.add(component);
        }
    }
}
