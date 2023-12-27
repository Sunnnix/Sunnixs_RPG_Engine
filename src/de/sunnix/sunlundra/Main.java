package de.sunnix.sunlundra;

import de.sunnix.engine.debug.BuildData;
import de.sunnix.engine.debug.GameLogger;

public class Main {

    public static void main(String[] args) {
        GameLogger.logI("Main", BuildData.getData("name") + " Version: " + BuildData.getData("version"));
    }

}
