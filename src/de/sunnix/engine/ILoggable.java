package de.sunnix.engine;

import de.sunnix.engine.debug.GameLogger;

public interface ILoggable {

    String getCallerName();

    default void logI(String message){
        GameLogger.logI(getCallerName(), message);
    }

    default void logW(String message){
        GameLogger.logW(getCallerName(), message);
    }

    default void logE(String message){
        GameLogger.logE(getCallerName(), message);
    }

    default void logError(Throwable throwable){
        GameLogger.logException(getCallerName(), throwable);
    }

}
