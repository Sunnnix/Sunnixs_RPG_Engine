package de.sunnix.aje.editor;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.aje.editor.window.Window;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        new Window();
    }

}
