package de.sunnix.engine;

import de.sunnix.engine.debug.BuildData;
import de.sunnix.engine.graphics.Window;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;

import static de.sunnix.engine.debug.GameLogger.*;

public class Core {

    private enum State{
        PRE_INIT, INITED, WINDOW_CREATED, STARTED
    }

    private static State current_state = State.PRE_INIT;

    @Getter
    private static boolean power_safe_mode = false;

    @Getter
    private static double unstable_fps;
    @Getter
    private static double fps;

    // *************************************************************** //
    //                        Window properties                        //
    // *************************************************************** //
    @Getter(AccessLevel.PACKAGE)
    private static long window;
    @Setter
    @Getter
    private static int window_targetFPS = 60;
    public static final int TILE_WIDTH = 24;
    public static final int TILE_HEIGHT = 16;
    @Getter
    private static boolean vsync = true;
    @Setter
    private static double pixel_scale = 2;

    // *************************************************************** //

    private static void validate(State expected){
        if(expected != current_state)
            throw new IllegalStateException(String.format("The current state is %s but state %s was expected", current_state, expected));
    }

    public static void init(){
        validate(State.PRE_INIT);
        current_state = State.INITED;

        BuildData.create();

        if (!glfwInit())
            throw new IllegalStateException("GLFW could not be initialized");

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logException("UncaughtExceptionHandler", throwable);
            System.exit(1);
        });
        logI("Core", "Inited Core " + BuildData.getData("name") + " Version: " + BuildData.getData("version"));
    }

    public static void createWindow(String title, int width, int height, Consumer<Window.WindowBuilder> windowBuilder){
        validate(State.INITED);

        var builder = new Window.WindowBuilder(title, width, height);
        if(windowBuilder != null)
            windowBuilder.accept(builder);

        setVsync(vsync);

        window = builder.build();

        InputManager.process(window); // Create InputManager Keys

        createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        current_state = State.WINDOW_CREATED;
    }

    public static void createWindow(String title, int width, int height){
        createWindow(title, width, height,null);
    }

    public static void createWindow(int width, int height){
        createWindow("Game", width,height);
    }

    public static void start(){
        validate(State.WINDOW_CREATED);

        subscribeLoop("fps_generator", 0, createGenFPSFunction());
        subscribeLoop("input_process", 0, () -> InputManager.process(window));

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        current_state = State.STARTED;

        var error = glGetError();
        if(error != GL_NO_ERROR){
            logE("Core", "GL_ERROR: " + error);
        }

        logI("Core", "Game started!");
        Looper.loop();
        logI("Core", "Game stopped!");
    }

    public static void setVsync(boolean on){
        if(current_state == State.WINDOW_CREATED || current_state == State.STARTED)
            glfwSwapInterval(on ? 1 : 0);
        vsync = on;
    }

    public static void subscribeLoop(@NonNull String id, int period, Runnable runnable){
        Looper.subscribe(id, period, runnable);
    }

    public static void unsubscribeLoop(String id){
        Looper.unsubscribe(id);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Runnable createGenFPSFunction(){
        var maximumListSize = 60 * 4;
        var fpsList = new ArrayList<Double>(maximumListSize);
        var wrapper = new Object(){ double latestTime = glfwGetTime(); };
        return () -> {
            var currentTime = glfwGetTime();
            unstable_fps = 1.0 / (currentTime - wrapper.latestTime);
            if(fpsList.size() >= maximumListSize)
                fpsList.remove(0);
            fpsList.add(unstable_fps);
            fps = fpsList.stream().mapToDouble(d -> d).average().getAsDouble();
            wrapper.latestTime = currentTime;
        };
    }

}
