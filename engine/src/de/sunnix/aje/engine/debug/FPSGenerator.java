package de.sunnix.aje.engine.debug;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class FPSGenerator{

    private static final int maximumListSize = 60;
    private static final List<Double> fpsList = new ArrayList<>(maximumListSize);
    private static double latestTime = 0;

    public static double[] run() {
        var currentTime = glfwGetTime();
        var unstable_fps = 1.0 / (currentTime - latestTime);
        if(fpsList.size() >= maximumListSize)
            fpsList.remove(0);
        fpsList.add(unstable_fps);
        var fps = fpsList.stream().mapToDouble(d -> d).average().orElse(0d);
        latestTime = currentTime;
        return new double[]{ unstable_fps, fps };
    }

}
