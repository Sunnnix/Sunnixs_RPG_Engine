package de.sunnix.engine;

import de.sunnix.engine.debug.BuildData;
import de.sunnix.engine.debug.FPSGenerator;
import de.sunnix.engine.debug.GLDebugPrintStream;
import de.sunnix.engine.debug.GameLogger;
import de.sunnix.engine.ecs.components.BaseComponent;
import de.sunnix.engine.graphics.Camera;
import de.sunnix.engine.graphics.Window;
import de.sunnix.engine.graphics.gui.GUIManager;
import de.sunnix.engine.memory.ContextQueue;
import de.sunnix.engine.memory.MemoryHandler;
import de.sunnix.engine.stage.GameplayState;
import de.sunnix.engine.stage.IState;
import de.sunnix.engine.stage.IntroState;
import de.sunnix.engine.stage.MainMenuState;
import de.sunnix.engine.util.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector3f;
import org.lwjgl.opengl.GLUtil;

import java.util.function.Consumer;

import static de.sunnix.engine.debug.GameLogger.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class Core {

    private enum CoreStage {
        PRE_INIT, INITED, WINDOW_CREATED, STARTED
    }

    public enum GameState {
        INTRO(new IntroState()), MAIN_MENU(new MainMenuState()), GAMEPLAY(new GameplayState());

        public final IState state;
        GameState(IState state){
            this.state = state;
        }
    }

    private static CoreStage current_stage = CoreStage.PRE_INIT;
    @Getter
    private static GameState current_game_state = GameState.INTRO;
    @Setter
    private static GameState next_game_state = GameState.INTRO;

    private static boolean gl_debug_enabled;

    @Getter
    @Setter
    private static boolean power_safe_mode = true;

    @Getter
    private static double unstable_fps;
    @Getter
    private static double fps;

    @Getter
    @Setter
    private static boolean useProfiler;

    @Getter
    @Setter
    private static boolean exit_on_close = true;

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
    @Getter
    private static float pixel_scale = 2;

    // *************************************************************** //

    @Getter
    private static final Vector3f backgroundColor = new Vector3f(0.7f, 0.7f, 0.7f);

    private static void validate(CoreStage expected){
        if(expected != current_stage)
            throw new IllegalStateException(String.format("The current stage is %s but stage %s was expected", current_stage, expected));
    }

    public static void init(){
        validate(CoreStage.PRE_INIT);
        current_stage = CoreStage.INITED;

        BuildData.create();
        BaseComponent.registerComponents(BaseComponent.class.getPackageName());

        if (!glfwInit())
            throw new IllegalStateException("GLFW could not be initialized");

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logException("UncaughtExceptionHandler", throwable);
            System.exit(1);
        });
        logI("Core", "Inited Core " + BuildData.getData("name") + " Version: " + BuildData.getData("version"));
    }

    public static void enableGL_debug(boolean enable){
        validate(CoreStage.INITED);
        gl_debug_enabled = enable;
    }

    public static void createWindow(String title, int width, int height, Consumer<Window.WindowBuilder> windowBuilder){
        validate(CoreStage.INITED);

        var builder = new Window.WindowBuilder(title, width, height, gl_debug_enabled);
        if(windowBuilder != null)
            windowBuilder.accept(builder);

        setVsync(vsync);

        window = builder.build();

        InputManager.process(window); // Create InputManager Keys

        createCapabilities();
        if(gl_debug_enabled)
            GLUtil.setupDebugMessageCallback(new GLDebugPrintStream());
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        glfwSetErrorCallback((err, msg) -> GameLogger.logE("Core", String.format("%s: %s", Utils.getGLErrorString(err), msg)));
        current_stage = CoreStage.WINDOW_CREATED;
    }

    public static void createWindow(String title, int width, int height){
        createWindow(title, width, height,null);
    }

    public static void createWindow(int width, int height){
        createWindow("Game", width,height);
    }

    public static void start(){
        validate(CoreStage.WINDOW_CREATED);
        current_stage = CoreStage.STARTED;

        subscribeLoop("fps_generator", 0, Core::calculateFPS);
        subscribeLoop("context_queue", 0, ContextQueue::runQueueOnMain);
        subscribeLoop("input_process", 0, () -> InputManager.process(window));
        subscribeLoop("update", 1, Core::update);
        subscribeLoop("render", 2, Core::render);
        subscribeLoop("postUpdate", 3, Core::postUpdate);

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        var error = glGetError();
        if(error != GL_NO_ERROR){
            logE("Core", "GL_ERROR: " + error);
        }

        logI("Core", "Game started!");
        try {
            Looper.loop();
        } catch (Exception e){
            logException("Core", e);
        }
        logI("Core", "Game stopping!");
        unsubscribeLoop("fps_generator");
        unsubscribeLoop("context_queue");
        unsubscribeLoop("input_process");
        unsubscribeLoop("update");
        unsubscribeLoop("render");
        unsubscribeLoop("postUpdate");
        MemoryHandler.freeAll();
        glfwTerminate();
        logI("Core", "Game stopped!");
        if(exit_on_close)
            System.exit(0);
    }

    private static void update(){
        Camera.process();
        current_game_state.state.update();
    }

    private static void render(){
        var bgc = Core.getBackgroundColor();
        glClearColor(bgc.x, bgc.y, bgc.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        current_game_state.state.render();
        GUIManager.render();
    }

    private static void postUpdate(){
        current_game_state.state.postUpdate();
        if(next_game_state != current_game_state){
            current_game_state.state.onStop();
            next_game_state.state.onStart();
            current_game_state = next_game_state;
        }
    }

    public static void setVsync(boolean on){
        if(current_stage == CoreStage.WINDOW_CREATED || current_stage == CoreStage.STARTED)
            glfwSwapInterval(on ? 1 : 0);
        vsync = on;
    }

    /**
     * the runnable is called every loop
     */
    public static void subscribeLoop(@NonNull String id, int period, Runnable runnable){
        Looper.subscribe(id, period, runnable);
    }

    public static void unsubscribeLoop(String id){
        Looper.unsubscribe(id);
    }

    /**
     * the listener is called if a key of the InputManager is pressed or released
     */
    public static void subscribeInputManager(@NonNull String id, int period, InputManager.InputListener listener){
        InputManager.subscribe(id, period, listener);
    }

    public static void unsubscribeInputManager(String id){
        InputManager.unsubscribe(id);
    }

    private static void calculateFPS() {
        var data = FPSGenerator.run();
        unstable_fps = data[0];
        fps = data[1];
    }

}
