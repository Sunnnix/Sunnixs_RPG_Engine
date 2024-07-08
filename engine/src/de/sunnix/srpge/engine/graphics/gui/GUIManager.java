package de.sunnix.srpge.engine.graphics.gui;

import de.sunnix.srpge.engine.util.Tuple;
import lombok.Getter;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    static final List<IGUIComponent> components = new ArrayList<>();

    @Getter
    private final static SpeechBox speechBox = new SpeechBox();

    private static List<Tuple.Tuple4<Integer, String, String, SpeechBox.SoundType>> messageQueue = new ArrayList<>();

    public static void render(){
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        if(speechBox.isVisible() || !messageQueue.isEmpty()) {
            if(!speechBox.isVisible()){
                var message = messageQueue.remove(0);
                speechBox.showText(message.t1(), message.t2(), message.t3(), message.t4());
            }
            speechBox.render();
        }
        synchronized (components) {
            components.forEach(IGUIComponent::render);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    /**
     * Only works if the speech box is not showing
     *
     * @return id of the message
     */
    public static int showSpeechBox(String name, String text, SpeechBox.SoundType soundType){
        var message = new Tuple.Tuple4<>((int)(Math.random() * Integer.MAX_VALUE), name, text, soundType);
        messageQueue.add(message);
        return message.t1();
    }

    public static boolean isSpeechBoxFinished(int id){
        return speechBox.getId() == id && speechBox.isFinished() && !speechBox.isVisible() || speechBox.getId() != id && messageQueue.stream().noneMatch(message -> message.t1() == id);
    }

    public static void add(IGUIComponent component) {
        synchronized (components) {
            components.add(component);
        }
    }
}
