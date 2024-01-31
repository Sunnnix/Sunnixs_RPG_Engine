package de.sunnix.engine.memory;

import de.sunnix.engine.debug.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to load openGL relevant objects in other threads but make the openGL calls from main
 */
public class ContextQueue {

    private final static List<Runnable> queue = new ArrayList<>();

    /**
     * put all openGL calls in here
     */
    public static void addQueue(Runnable function) {
        synchronized (queue) {
            queue.add(function);
        }
    }

    /**
     * Should only be called from the main thread
     */
    public static void runQueueOnMain() {
        synchronized (queue) {
            if(queue.size() > 0) {
                queue.forEach(Runnable::run);
                queue.clear();
            }
        }
    }

}
