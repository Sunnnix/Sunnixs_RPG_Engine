package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;

public class NULLCondition extends de.sunnix.srpge.engine.evaluation.NULLCondition implements ICondition {
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(Window window, MapData map, GameObject object) {
        return "NULL";
    }

    @Override
    public Runnable getEditGUI(Window window, MapData map, GameObject object, JPanel content) {
        return null;
    }
}
