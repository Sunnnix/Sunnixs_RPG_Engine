package de.sunnix.srpge.editor;

import com.formdev.flatlaf.FlatDarkLaf;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.Core;
import lombok.Getter;

import javax.swing.*;
import java.util.Arrays;

public class Main {

    @Getter
    private static boolean FROM_IDE;

    public static void main(String[] args) throws Exception {
        FlatDarkLaf.setup();
        UIManager.put("TabbedPane.showTabSeparators", true);
        FROM_IDE = args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("fromIDE"));
        if(args.length > 0 && Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("startGame"))) {
            var opt = Arrays.stream(args).filter(s -> s.startsWith("gameFile=")).findFirst();
            opt.ifPresent(s -> Core.setGameFile(s.substring(s.indexOf("=") + 1)));
            de.sunnix.srpge.game.Main.main(args);
        } else
            new Window();
    }

}
