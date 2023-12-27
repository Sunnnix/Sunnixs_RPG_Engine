package de.sunnix.engine.debug;

import java.text.SimpleDateFormat;
import java.util.logging.*;

public final class GameLogger{

    private static final Logger logger = createLogger();

    private static Logger createLogger(){
        var log = Logger.getLogger(GameLogger.class.getPackageName());
        log.setUseParentHandlers(false);

        var formatter = new SimpleFormatter(){

            final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss:SSS");

            @Override
            public String format(LogRecord record) {
                var prefix = "[" + dateFormat.format(record.getMillis()) + "][" + record.getLoggerName() + "][" + record.getLevel().getName() + "]: ";
                if(record.getThrown() != null)
                    return createThrownString(prefix, record.getThrown(), false);
                return prefix + record.getMessage() + "\n";
            }

            private String createThrownString(String prefix, Throwable thrown, boolean caused){
                var stacks = thrown.getStackTrace();
                var sb = new StringBuilder();
                sb.append(prefix);
                if(caused)
                    sb.append("Caused by: ");
                sb.append(thrown);
                sb.append('\n');
                for(var stack : stacks){
                    sb.append(prefix);
                    sb.append('\t');
                    sb.append(stack.toString());
                    sb.append('\n');
                }
                if(thrown.getCause() != null)
                    sb.append(createThrownString(prefix, thrown.getCause(), true));
                return sb.toString();
            }

        };

        var cHandler = new ConsoleHandler();
        cHandler.setFormatter(formatter);
        log.addHandler(cHandler);

        try{
            var fHandler = new FileHandler("game.log", (int)(50 * Math.pow(10, 6)), 2, true);
            fHandler.setFormatter(formatter);
            log.addHandler(fHandler);
        } catch (Exception e){
            e.printStackTrace();
        }

        return log;
    }

    public static void logI(String caller, String msg){
        var record = new LogRecord(Level.INFO, msg);
        record.setLoggerName(caller);
        logger.log(record);
    }

    public static void logW(String caller, String msg){
        var record = new LogRecord(Level.WARNING, msg);
        record.setLoggerName(caller);
        logger.log(record);
    }

    public static void logE(String caller, String msg){
        var record = new LogRecord(Level.SEVERE, msg);
        record.setLoggerName(caller);
        logger.log(record);
    }

    public static void logException(String caller, Throwable throwable){
        var record = new LogRecord(Level.SEVERE, "");
        record.setLoggerName(caller);
        record.setThrown(throwable);
        logger.log(record);
    }

    private GameLogger() {}

}
