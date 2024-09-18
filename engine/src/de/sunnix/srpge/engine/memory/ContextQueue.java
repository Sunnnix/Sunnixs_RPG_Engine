package de.sunnix.srpge.engine.memory;

import de.sunnix.srpge.engine.util.FunctionUtils;
import de.sunnix.srpge.engine.util.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to load openGL relevant objects in other threads but make the openGL calls from main
 */
public class ContextQueue {

    private final static List<Tuple.Tuple2<String, Runnable>> queue = new ArrayList<>();

    /**
     * put all openGL calls in here
     */
    public static void addQueue(Runnable function) {
        synchronized (queue) {
            var stack = Thread.currentThread().getStackTrace()[2];
            queue.add(new Tuple.Tuple2<>(stack.getClassName() + "." +stack.getMethodName(), function));
        }
    }

    /**
     * Should only be called from the main thread
     */
    public static void runQueueOnMain() {
        synchronized (queue) {
            FunctionUtils.checkForOpenGLErrors("ContextQueue - Pre run");
            if(queue.size() > 0) {
                queue.forEach(t -> {
                    t.t2().run();
                    FunctionUtils.checkForOpenGLErrors("ContextQueue - " + t.t1());
                });
                queue.clear();
            }
            FunctionUtils.checkForOpenGLErrors("ContextQueue - Post run");
        }
    }

}
