package de.sunnix.aje.editor;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.aje.editor.docu.UserGuide;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.engine.Core;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Main {

    @Getter
    private static boolean FROM_IDE;

    public static void main(String[] args) throws Exception {
        FlatDarkLaf.setup();
        UIManager.put("TabbedPane.showTabSeparators", true);
        FROM_IDE = args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("fromIDE"));
        if(args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("docu")))
            new UserGuide(null);
        else if(args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("startGame"))) {
            var opt = Arrays.stream(args).filter(s -> s.startsWith("gameFile=")).findFirst();
            opt.ifPresent(s -> Core.setGameFile(s.substring(s.indexOf("=") + 1)));
            de.sunnix.aje.game.Main.main(args);
        } else
            new Window();
    }

}
