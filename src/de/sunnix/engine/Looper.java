package de.sunnix.engine;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Looper {

    private Looper(){}

    private static List<Runnable> loopListener = new LinkedList<>();

    static void subscribeLoop(Runnable runnable){
        loopListener.add(runnable);
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

            loopListener.forEach(Runnable::run);

            lastTime = currentTime;

            glfwSwapBuffers(Core.getWindow());
            glfwPollEvents();
        }
    }

}
