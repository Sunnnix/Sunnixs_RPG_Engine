package de.sunnix.engine;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Looper {

    private Looper(){}

    private static final List<LoopSubscriber> listeners = new ArrayList<>();
    private static final List<LoopSubscriber> subscriber = new ArrayList<>();
    private static final List<String> unsubscriber = new LinkedList<>();

    static void subscribe(@NonNull String id, int period, Runnable runnable){
        if(runnable == null)
            return;
        subscriber.add(new LoopSubscriber(id, period, runnable));
    }

    static void unsubscribe(String id){
        unsubscriber.add(id);
    }

    static void loop(){
        double lastTime = glfwGetTime();

        while (!glfwWindowShouldClose(Core.getWindow())) {
            double targetFrameTime = 1.0 / Core.getWindow_targetFPS();
            double currentTime = glfwGetTime();
            if(currentTime - lastTime < targetFrameTime * (Core.isPower_safe_mode() ? .95 : 1)) {
                if(Core.isPower_safe_mode() && currentTime - lastTime < (targetFrameTime * .95) * .99)
                    try {
                        var delay = (targetFrameTime * .95 - (currentTime - lastTime)) * .01;
                        var milli = (int)(delay * 1000);
                        var nano = (int)((delay * 1000 - milli) * 1000000);
                        Thread.sleep(milli, nano);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                continue;
            }
            lastTime = currentTime;

            checkSubscribers();
            listeners.forEach(LoopSubscriber::run);

            glfwSwapBuffers(Core.getWindow());
            glfwPollEvents();
        }
    }

    private static void checkSubscribers(){
        if(subscriber.size() > 0){
            listeners.addAll(subscriber);
            subscriber.clear();
            listeners.sort(Comparator.comparing(LoopSubscriber::period));
        }
        if(unsubscriber.size() > 0) {
            unsubscriber.forEach(id -> listeners.removeIf(s -> s.ID.equals(id)));
            unsubscriber.clear();
        }
    }

    private record LoopSubscriber(@NonNull String ID, int period, Runnable runnable){

        public void run(){
            runnable.run();
        }

    }

}
