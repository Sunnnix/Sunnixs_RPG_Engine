package de.sunnix.engine.graphics.gui;

import de.sunnix.engine.graphics.gui.text.Text;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    static List<Text> texts = new ArrayList<Text>();

    public static void render(){
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        texts.forEach(Text::render);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void add(Text text) {
        texts.add(text);
    }
}
