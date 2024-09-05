package de.sunnix.srpge.engine;

import de.sunnix.srpge.engine.debug.profiler.Profiler;
import de.sunnix.srpge.engine.util.Utils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class Looper {

    private Looper(){}

    private static final List<LoopSubscriber> listeners = new ArrayList<>();
    private static final List<LoopSubscriber> subscriber = new ArrayList<>();
    private static final List<String> unsubscriber = new LinkedList<>();

    private static int ticks;

    static void subscribe(@NonNull String id, int period, Consumer<Integer> onTick) {
        if(onTick == null)
            return;
        subscriber.add(new LoopSubscriber(id, period, onTick));
    }

    static void unsubscribe(String id) {
        unsubscriber.add(id);
    }

    /**
     * The GameLoop starts when this function is started.<br>
     * In this a while loop is executed until the function glfwWindowShouldClose returns true.<br>
     * In this function the fps are limited to the set fps of the core and offers an additional possibility
     * to relieve the CPU by the powersafemode in exchange of a small inaccuracy of the fps.<br>
     * For each frame, all listeners are executed by period.
     */
    static void loop() {
        double lastTime = glfwGetTime();

        double powerSafeDelay = .968; // designed for 60 fps

        long latestGCTime = System.currentTimeMillis();

        while (!glfwWindowShouldClose(Core.getWindow())) {
            double targetFrameTime = 1.0 / Core.getWindow_targetFPS();
            double currentTime = glfwGetTime();
            if (currentTime - lastTime < targetFrameTime * (Core.isPower_safe_mode() ? powerSafeDelay : 1)) {
                if (Core.isPower_safe_mode() && currentTime - lastTime < (targetFrameTime * powerSafeDelay) * .99)
                    try {
                        var delay = (targetFrameTime * powerSafeDelay - (currentTime - lastTime)) * .01;
                        var milli = (int) (delay * 1000);
                        var nano = (int) ((delay * 1000 - milli) * 1000000);
                        Thread.sleep(milli, nano);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                continue;
            }

            if(latestGCTime + 5000 < System.currentTimeMillis()){
                latestGCTime = System.currentTimeMillis();
                if(Core.isUse_manual_gc())
                    System.gc();
            }

            lastTime = currentTime;

            var profilerStart = System.nanoTime();

            ticks++;

            checkSubscribers();
            if(Core.isUseProfiler()) {
                listeners.forEach(s -> {
                    var start = System.nanoTime();
                    s.run();
                    Profiler.profile(s.ID, System.nanoTime() - start);
                });
            } else
                listeners.forEach(LoopSubscriber::run);

            if(Core.isUseProfiler()) {
                var start = System.nanoTime();
                glfwSwapBuffers(Core.getWindow());
                Profiler.profile("swap_buffers", System.nanoTime() - start);
            } else
                glfwSwapBuffers(Core.getWindow());
            if(Core.isUseProfiler()) {
                var start = System.nanoTime();
                glfwPollEvents();
                Profiler.profile("poll_events", System.nanoTime() - start);
            } else
                glfwPollEvents();

            Utils.checkForOpenGLErrors("Looper");

            if(Core.isUseProfiler())
                Profiler.profileTotal(System.nanoTime() - profilerStart);
        }
    }

    private static void checkSubscribers(){
        if(subscriber.size() > 0){
            listeners.removeIf(list -> subscriber.stream().anyMatch(sub -> sub.ID.equals(list.ID)));
            listeners.addAll(subscriber);
            subscriber.clear();
            listeners.sort(Comparator.comparing(LoopSubscriber::period));
        }
        if(unsubscriber.size() > 0) {
            unsubscriber.forEach(id -> listeners.removeIf(s -> s.ID.equals(id)));
            unsubscriber.clear();
        }
    }

    private record LoopSubscriber(@NonNull String ID, int period, Consumer<Integer> runnable){

        public void run(){
            runnable.accept(ticks);
        }

    }

}
