package de.sunnix.aje.engine.graphics.gui;

import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    static final List<IGUIComponent> components = new ArrayList<>();

    @Getter
    private final static SpeechBox speechBox = new SpeechBox();

    public static void render(){
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        speechBox.render();
        synchronized (components) {
            components.forEach(IGUIComponent::render);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Only works if the speech box is not showing
     *
     * @return
     */
    public static SpeechBox showSpeechBox(String name, String text){
        speechBox.showText(name, text);
        return speechBox;
    }

    public static void add(IGUIComponent component) {
        synchronized (components) {
            components.add(component);
        }
    }
}
