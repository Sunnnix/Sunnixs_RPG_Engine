package de.sunnix.engine;

import de.sunnix.engine.debug.BuildData;
import de.sunnix.engine.debug.GameLogger;

public class Core {

    private static boolean inited = false;

    public static void init(){
        if(inited)
            return;
        inited = true;
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            GameLogger.logException("UncaughtExceptionHandler", throwable);
            System.exit(-1);
        });
        GameLogger.logI("Core", "Inited Core " + BuildData.getData("name") + " Version: " + BuildData.getData("version"));
    }

}
