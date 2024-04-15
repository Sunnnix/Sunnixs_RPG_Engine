package de.sunnix.aje.editor.window;

import javax.swing.*;

public class Toolbar extends JToolBar {

    private final Window window;

    public Toolbar(Window window){
        this.window = window;
        add(new JButton("Test"));
    }

}
