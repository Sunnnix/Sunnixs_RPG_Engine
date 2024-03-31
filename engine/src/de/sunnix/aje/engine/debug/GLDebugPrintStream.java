package de.sunnix.aje.engine.debug;

import java.io.PrintStream;
import java.util.logging.Level;

public class GLDebugPrintStream extends PrintStream {
    public GLDebugPrintStream() {
        super(System.out);
    }

    private void write(String s){
        GameLogger.logI("LWJGL DEBUG", s);
    }

    @Override
    public void print(Object obj) {
        if(obj instanceof StringBuilder sb){
            var split = sb.toString().split("\n");
            if(split.length < 6)
                write(String.valueOf(obj));
            else {
                var id = split[1].substring(5);
                var source = split[2].substring(9);
                var type = split[3].substring(7);
                var severity = split[4].substring(11);
                var message = split[5].substring(10);

                var level = switch (type) {
                    case "OTHER" -> Level.INFO;
                    case "MARKER", "PERFORMANCE", "PORTABILITY", "UNDEFINED BEHAVIOR", "DEPRECATED BEHAVIOR" -> Level.WARNING;
                    default -> Level.SEVERE;
                };
                GameLogger.log(new GameLogger.CustomLogRecord("LWJGL DEBUG", level, String.format("(%s) %s: [%s] %s", id, source, type, message)));
            }
        } else
            write(String.valueOf(obj));
    }

}
