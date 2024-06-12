package de.sunnix.srpge.engine.debug;

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
            var logDir = "log/";
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

    public static void logI(String caller, String msg, Object... args){
        logger.log(new CustomLogRecord(caller, Level.INFO, String.format(msg, args)));
    }

    public static void logW(String caller, String msg, Object... args){
        logger.log(new CustomLogRecord(caller, Level.WARNING, String.format(msg, args)));
    }

    public static void logE(String caller, String msg, Object... args){
        logger.log(new CustomLogRecord(caller, Level.SEVERE, String.format(msg, args)));
    }

    public static void logException(String caller, Throwable throwable){
        var record = new CustomLogRecord(caller, Level.SEVERE, "");
        record.setThrown(throwable);
        logger.log(record);
    }

    public static void log(CustomLogRecord record){
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
            var prefix = "[" + dateFormat.format(record.getMillis()) + "][" + ((CustomLogRecord)record).caller + "][" + record.getLevel().getName() + "]: ";
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

    public static class CustomLogRecord extends LogRecord{

        public final String caller;

        public CustomLogRecord(String caller, Level level, String msg) {
            super(level, msg);
            this.caller = caller;
        }
    }

}
