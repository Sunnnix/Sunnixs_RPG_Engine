package de.sunnix.aje.editor;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.aje.editor.docu.UserGuide;
import de.sunnix.aje.editor.window.Window;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        if(args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("docu")))
            new UserGuide(null);
        else
            new Window();
    }

}
