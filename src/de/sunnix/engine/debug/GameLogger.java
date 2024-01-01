package de.sunnix.engine.debug;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.*;

public final class GameLogger{

    private static final Logger logger = createLogger();

    private static Logger createLogger(){
        var log = Logger.getLogger(GameLogger.class.getPackageName());
        log.setUseParentHandlers(false);

        var cHandler = new ConsoleHandler();
        cHandler.setFormatter(new Formatter(true));
        log.addHandler(cHandler);

        try{
            var logDir = BuildData.getData("log_directory", "");
            if(!logDir.endsWith("/"))
                logDir += '/';
            var directory = new File(logDir);
            if(!directory.exists())
                directory.mkdir();
            var fHandler = new FileHandler( logDir + "game.log", (int)(50 * Math.pow(1024, 2)), 10, true);
            fHandler.setFormatter(new Formatter(false));
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

    private static class Formatter extends SimpleFormatter {

        private static final String COLOR_GREEN = "\u001B[32m";
        private static final String COLOR_YELLOW = "\u001B[33m";
        private static final String COLOR_RED = "\u001B[31m";
        private static final String COLOR_RESET = "\u001B[0m";

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");

        private final boolean useColors;

        public Formatter(boolean useColors){
            this.useColors = useColors;
        }

        @Override
        public String format(LogRecord record) {
            String formatted;
            var prefix = "[" + dateFormat.format(record.getMillis()) + "][" + record.getLoggerName() + "][" + record.getLevel().getName() + "]: ";
            if(record.getThrown() != null)
                formatted = createThrownString(prefix, record.getThrown(), false);
            else
                formatted = prefix + record.getMessage();
            if(useColors){
                String color = "";
                if(record.getLevel().equals(Level.INFO))
                    color = COLOR_GREEN;
                else if(record.getLevel().equals(Level.WARNING))
                    color = COLOR_YELLOW;
                else if(record.getLevel().equals(Level.SEVERE))
                    color = COLOR_RED;
                formatted = color + formatted + COLOR_RESET;
            }
            return formatted + "\n";
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
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

}
