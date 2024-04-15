package de.sunnix.aje.editor.window;

import javax.swing.*;

public class PropertiesView extends JPanel {

    private final Window window;

    public PropertiesView(Window window) {
        this.window = window;
        setBorder(BorderFactory.createTitledBorder((String)null));
    }

}
