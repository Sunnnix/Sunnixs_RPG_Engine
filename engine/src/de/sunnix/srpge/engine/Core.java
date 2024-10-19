package de.sunnix.srpge.engine;

import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.audio.OpenALContext;
import de.sunnix.srpge.engine.ecs.components.CombatComponent;
import de.sunnix.srpge.engine.ecs.components.ComponentRegistry;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;
import de.sunnix.srpge.engine.ecs.components.RenderComponent;
import de.sunnix.srpge.engine.ecs.event.*;
import de.sunnix.srpge.engine.evaluation.*;
import de.sunnix.srpge.engine.memory.ContextQueue;
import de.sunnix.srpge.engine.memory.MemoryHandler;
import de.sunnix.srpge.engine.debug.FPSGenerator;
import de.sunnix.srpge.engine.debug.GLDebugPrintStream;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.graphics.Camera;
import de.sunnix.srpge.engine.graphics.Window;
import de.sunnix.srpge.engine.graphics.gui.GUIManager;
import de.sunnix.srpge.engine.registry.Registry;
import de.sunnix.srpge.engine.stage.GameplayState;
import de.sunnix.srpge.engine.stage.IState;
import de.sunnix.srpge.engine.stage.IntroState;
import de.sunnix.srpge.engine.stage.MainMenuState;
import de.sunnix.srpge.engine.util.FunctionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.opengl.GLUtil;

import java.util.Arrays;
import java.util.function.Consumer;

import static de.sunnix.srpge.engine.debug.GameLogger.*;
import static de.sunnix.srpge.engine.util.FunctionUtils.mix;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The core of the game engine.<br>
 * This class is responsible for initializing and managing key libraries such as OpenGL and OpenAL, and running the main game loop via the {@link Looper}.<br><br>
 *
 * <b>Lifecycle and Setup:</b><br>
 * The lifecycle of the game engine follows these steps:<br>
 * <ul type="1">
 * <li><b>Initialization:</b> Call {@link Core#init() init} to initialize necessary components.</li>
 * <li><b>Window Creation:</b> Create the main window by calling {@link Core#createWindow(String, int, int, Consumer) createWindow}.</li>
 * <li><b>Start Game Loop:</b> Start the game loop with {@link Core#start() start}, which blocks the calling thread until the game loop ends.</li>
 *</ul><br>
 *
 * <b>Core Stages:</b><br>
 * The engine operates in different stages, represented by {@link CoreStage CoreStage}. These stages help ensure that functions are called in the correct order:
 * <ul>
 * <li>{@link CoreStage#PRE_INIT PRE_INIT}: Initial stage before any setup.</li>
 * <li>{@link CoreStage#INITED INITED}: After initialization but before window creation.</li>
 * <li>{@link CoreStage#WINDOW_CREATED WINDOW_CREATED}: After window creation but before starting the game loop.</li>
 * <li>{@link CoreStage#STARTING STARTING}: While setting up the game loop.</li>
 * <li>{@link CoreStage#STARTED STARTED}: The game is running.</li>
 * </ul><br>
 *
 * <b>Window and Graphics Settings:</b><br>
 * The class provides various settings for the game window and graphics:
 * <ul>
 * <li>{@link Core#window_targetFPS}: Target frames per second for the window.</li>
 * <li>{@link Core#vsync}: Vertical synchronization setting.</li>
 * <li>{@link Core#pixel_scale}: Scaling factor for rendering.</li>
 * </ul><br>
 *
 * <b>Game State Management:</b><br>
 * The class handles game states with {@link GameState GameState}. It allows transitioning between different game states and ensures proper initialization and cleanup.<br><br>
 *
 * <b>Debugging and Profiling:</b><br>
 * You can enable OpenGL debug mode with {@link Core#enableGL_debug(boolean) enableGL_debug}, and toggle profiling with {@link Core#useProfiler}.<br><br>
 *
 * <b>Power Management:</b><br>
 * The {@link Core#power_safe_mode} setting controls the power consumption of the game loop, balancing between performance and power efficiency.<br><br>
 *
 * <b>Exit Behavior:</b><br>
 * If {@link Core#exit_on_close} is set to true, the JVM will terminate with exit code 0 when the game ends.<br><br>
 *
 * <b>Subscriptions:</b><br>
 * You can subscribe to game loop events and input events using {@link Core#subscribeLoop(String, int, Consumer) subscribeLoop} and {@link Core#subscribeInputManager(String, int, InputManager.InputListener) subscribeInputManager}.<br><br>
 *
 * <b>Utility Methods:</b><br>
 * The class includes utility methods for managing frame rates, window focus, and other runtime details.<br>
 */
public class Core {

    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 8;
    public static final String VERSION = String.format("%s.%s", MAJOR_VERSION, MINOR_VERSION);

    public static final int PLAYER_ID = 999;

    public enum CoreStage {
        PRE_INIT, INITED, WINDOW_CREATED, STARTING, STARTED
    }

    public enum GameState {
        INTRO(new IntroState()), MAIN_MENU(new MainMenuState()), GAMEPLAY(new GameplayState());

        public final IState state;
        GameState(IState state){
            this.state = state;
        }
    }

    /**
     * Determines what can be done next with the kernel.<br>
     * This variable prevents functions from being called at the wrong time.
     */
    @Getter
    private static CoreStage current_core_stage = CoreStage.PRE_INIT;
    @Getter
    private static GameState current_game_state = GameState.INTRO;
    @Setter
    private static GameState next_game_state = GameState.INTRO;

    private static GLFWErrorCallbackI errorCallback;

    private static boolean gl_debug_enabled;

    /**
     * When power safe mode is active, the {@link Looper} tries to use as little process time as possible while still keeping the frame rate stable.<br>
     * If this option is false, the {@link Looper} will use 100% of a core and use much more power but have a stable frame rate.
     */
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

    @Getter
    private static boolean debug;

    @Getter
    @Setter
    private static boolean use_manual_gc;

    @Getter
    @Setter
    private static boolean gl_error_stacktrace;

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
    @Getter
    private static float screenWidth, screenHeight;
    @Setter
    @Getter
    private static float pixel_scale = 2;

    // *************************************************************** //
    //                     Game File properties                        //
    // *************************************************************** //
    @Getter
    @Setter
    private static String gameFile = "GameFile.sgf";

    /** Specifies whether Playstation or X-Box buttons should be displayed in a text box when buttons are displayed */
    @Getter
    @Setter
    private static boolean psMode = false;

    // *************************************************************** //

    @Getter
    private static final Vector3f backgroundColor = new Vector3f(0f, 0f, 0f);

    @Getter
    private static Vector4f globalColoring = new Vector4f(0);

    /**
     * Validates that the core is in one of the expected stages.<br>
     * If the current stage does not match any of the expected stages, an {@link IllegalStateException} is thrown.<br><br>
     *
     * This is useful for ensuring that certain operations are only performed when the core is in a valid state.<br>
     * For example, you might use this method to verify that the core is in the {@link CoreStage#INITED INITED} stage
     * before attempting to configure OpenGL settings or create a window.<br><br>
     *
     * @param expected one or more {@link CoreStage CoreStage} values that the core is expected to be in
     * @throws IllegalStateException if the current core stage does not match any of the expected stages
     */
    public static void validateCoreStage(CoreStage... expected){
        if(Arrays.stream(expected).noneMatch(s -> s == current_core_stage))
            throw new IllegalStateException(String.format("The current stage is %s but stages %s was expected", current_core_stage, Arrays.toString(expected)));
    }

    /**
     * Initializes the core of the game engine.<br>
     * This method performs the following tasks:<br>
     * <ul>
     * <li>Initializes GLFW for managing the window and OpenGL context.</li>
     * <li>Logs the OpenGL version and checks if it meets the minimum requirements.</li>
     * <li>Sets up OpenAL for audio management.</li>
     * <li>Registers default events.</li>
     * <li>Sets a default uncaught exception handler to handle any unexpected errors.</li>
     * </ul><br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#PRE_INIT PRE_INIT} stage.<br>
     * If it is called, it changes the state to {@link CoreStage#INITED INITED}
     *
     * @throws IllegalStateException if GLFW cannot be initialized or if the OpenGL version is insufficient.
     */
    public static void init(){
        validateCoreStage(CoreStage.PRE_INIT);
        current_core_stage = CoreStage.INITED;

        errorCallback = glfwSetErrorCallback((err, msg) -> GameLogger.logE("Core", String.format("%s: %s", FunctionUtils.getGLErrorString(err), msg)));

        if (!glfwInit())
            throw new IllegalStateException("GLFW could not be initialized");

        var glVersion = new int[3][1];
        glfwGetVersion(glVersion[0], glVersion[1], glVersion[2]);
        GameLogger.logI("Core", "OpenGL version %s.%s.%s", glVersion[0][0], glVersion[1][0], glVersion[2][0]);
        int minMajor = Version.VERSION_MAJOR, minMinor = Version.VERSION_MINOR, minRev = Version.VERSION_REVISION;
        if(glVersion[0][0] < minMajor || (glVersion[0][0] == minMajor && glVersion[1][0] < minMinor) || (glVersion[0][0] == minMajor && glVersion[1][0] == minMinor && glVersion[2][0] < minRev))
            throw new RuntimeException(String.format("minimum OpenGL version %s.%s.%s required but %s.%s.%s was provided!", minMajor, minMinor, minRev, glVersion[0][0], glVersion[1][0], glVersion[2][0]));

        OpenALContext.setUp();

        registerEvents();
        registerComponents();
        registerEvaluation();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logException("UncaughtExceptionHandler", throwable);
            System.exit(1);
        });
    }

    /**
     * Creates the main game window.<br>
     * This method sets up the window with the specified title, width, height, and optional window builder customizations.<br>
     * It also configures various OpenGL settings such as V-Sync and depth testing.<br><br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#INITED INITED} stage.<br>
     * If it is called, it changes the state to {@link CoreStage#WINDOW_CREATED WINDOW_CREATED}<br>
     *
     * @param title the title of the window
     * @param width the width of the window
     * @param height the height of the window
     * @param windowBuilder an optional {@link Consumer} to customize the window settings
     */
    public static void createWindow(String title, int width, int height, Consumer<Window.WindowBuilder> windowBuilder){
        validateCoreStage(CoreStage.INITED);

        screenWidth = width;
        screenHeight = height;
        var builder = new Window.WindowBuilder(title, width, height, gl_debug_enabled);
        if(windowBuilder != null)
            windowBuilder.accept(builder);

        setVsync(vsync);

        window = builder.build();

        InputManager.process(window); // Create InputManager Keys

        if(gl_debug_enabled)
            GLUtil.setupDebugMessageCallback(new GLDebugPrintStream());
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);

        current_core_stage = CoreStage.WINDOW_CREATED;
    }

    /**
     * Starts the game loop.<br>
     * This method performs the following tasks:<br>
     * <ul>
     * <li>Initializes various game components.</li>
     * <li>Subscribes to game loop events for updating, rendering, and processing inputs.</li>
     * <li>Sets the OpenGL context and shows the game window.</li>
     * <li>Starts the {@link Looper} loop which runs the game.</li>
     * <li>Handles cleanup and termination of resources when the game ends.</li>
     * </ul><br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#WINDOW_CREATED WINDOW_CREATED} stage.<br>
     * If it is called, it changes the state to {@link CoreStage#STARTING STARTING}<br>
     *
     * @throws RuntimeException if an error occurs during OpenGL setup
     */
    public static void start(){
        validateCoreStage(CoreStage.WINDOW_CREATED);
        current_core_stage = CoreStage.STARTING;

        Setup.init();
        Registry.registerAll();

        AudioManager.get();

        subscribeLoop("fps_generator", 0, ticks -> calculateFPS());
        subscribeLoop("input_process", 1, ticks -> InputManager.process(window));
        subscribeLoop("update", 10, ticks -> update());
        subscribeLoop("context_queue", 20, ticks ->  ContextQueue.runQueueOnMain());
        subscribeLoop("render", 30, ticks -> render());
        subscribeLoop("postUpdate", 40, ticks -> postUpdate());

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        var error = glGetError();
        if(error != GL_NO_ERROR){
            logE("Core", "GL_ERROR: " + error);
        }

        current_core_stage = CoreStage.STARTED;
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
        Arrays.stream(GameState.values()).forEach(state -> state.state.onDestroy());
        MemoryHandler.freeAll();
        glfwTerminate();
        AudioManager.get().cleanup();
        OpenALContext.close();
        logI("Core", "Game stopped!");
        if(exit_on_close)
            System.exit(0);
    }

    /**
     * Registers event types for the game engine.<br>
     * This method registers various types of events that the engine can handle, such as move events, wait events, and message events.
     */
    private static void registerEvents(){
        EventRegistry.registerEvent("move", MoveEvent::new);
        EventRegistry.registerEvent("wait", WaitEvent::new);
        EventRegistry.registerEvent("message", MessageEvent::new);
        EventRegistry.registerEvent("playsound", PlaySoundEvent::new);
        EventRegistry.registerEvent("script-lua", LuaScriptEvent::new);
        EventRegistry.registerEvent("global_color_tint", GlobalColorTintEvent::new);
        EventRegistry.registerEvent("teleport", TeleportEvent::new);
        EventRegistry.registerEvent("look", LookEvent::new);
        EventRegistry.registerEvent("camera", CameraEvent::new);
        EventRegistry.registerEvent("change_state", ChangeStateEvent::new);
        EventRegistry.registerEvent("change_var", ChangeVariableEvent::new);
        EventRegistry.registerEvent("change_tile", ChangeTileEvent::new);
        EventRegistry.registerEvent("change_local_var", ChangeObjectVariableEvent::new);
        EventRegistry.registerEvent("obj_prop", ObjectPropertyEvent::new);
        EventRegistry.registerEvent("copy_object", CopyObjectEvent::new);
    }

    private static void registerComponents(){
        ComponentRegistry.add("render", RenderComponent::new);
        ComponentRegistry.add("physic", PhysicComponent::new);
        ComponentRegistry.add("combat", CombatComponent::new);
    }

    private static void registerEvaluation(){
        EvaluationRegistry.registerCondition("number", NumberCondition::new);
        EvaluationRegistry.registerProvider("num_var", NumberVariableProvider::new);
        EvaluationRegistry.registerProvider("loc_var", ObjectVariableProvider::new);
        EvaluationRegistry.registerProvider("object_num", ObjectNumberProvider::new);
    }

    /**
     * Updates the {@link Camera Camera} and calls the update function of the {@link Core#current_game_state}.<br>
     */
    private static void update(){
        Camera.process();
        current_game_state.state.update();
    }

    /**
     * Clears the screen of the game window with the {@link Core#backgroundColor} and {@link Core#globalColoring},
     * calls the render function of the {@link Core#current_game_state}
     * and renders the {@link GUIManager} elements
     */
    private static void render(){
        var bgc = getBackgroundColor();
        var mixed = mix(bgc, new Vector3f(globalColoring.x, globalColoring.y, globalColoring.z), globalColoring.w);
        glClearColor(mixed.x, mixed.y, mixed.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        current_game_state.state.render();
        GUIManager.render();
    }

    /**
     * Performs post-update operations.<br>
     * This method post updates the {@link Core#current_game_state} calling {@link IState#postUpdate() postUpdate} and transitions to a new {@link GameState GameState}
     * if the {@link Core#next_game_state} is different to the {@link Core#current_game_state}.
     */
    private static void postUpdate(){
        current_game_state.state.postUpdate();
        if(next_game_state != current_game_state){
            current_game_state.state.onStop();
            next_game_state.state.onStart();
            current_game_state = next_game_state;
        }
    }

    /**
     * Calculates the frames per second (FPS).<br>
     * This method uses the {@link FPSGenerator} to obtain the {@link Core#fps current FPS} and {@link Core#unstable_fps unstable FPS} values.
     */
    private static void calculateFPS() {
        var data = FPSGenerator.run();
        unstable_fps = data[0];
        fps = data[1];
    }

    /**
     * Enables or disables OpenGL debug mode.<br>
     * It allows for additional OpenGL error checking and debugging information.<br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#INITED INITED} stage.<br>
     *
     * @param enable whether to enable (true) or disable (false) the debug mode
     */
    public static void enableGL_debug(boolean enable){
        validateCoreStage(CoreStage.INITED);
        gl_debug_enabled = enable;
    }

    /**
     * Sets the vertical synchronization (V-Sync) for the game window.<br>
     * This method configures whether the frame rate should be synchronized with the monitor's refresh rate.
     *
     * @param on true to enable V-Sync, false to disable it
     */
    public static void setVsync(boolean on){
        if(current_core_stage == CoreStage.WINDOW_CREATED || current_core_stage == CoreStage.STARTED)
            glfwSwapInterval(on ? 1 : 0);
        vsync = on;
    }

    /**
     * Checks if the game window currently has focus.<br>
     * This method determines if the window is currently in the foreground and has user input focus.<br><br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#STARTING STARTING} or {@link CoreStage#STARTED STARTED} stage.
     *
     * @return true if the window has focus, false otherwise
     */
    public static boolean hasFocus(){
        validateCoreStage(CoreStage.STARTING, CoreStage.STARTED);
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
    }

    /**
     * Subscribes to a game loop event.<br>
     * This method allows you to register a consumer that will be called periodically during the game loop.
     *
     * @param id a unique identifier for the event
     * @param priority the call priority (higher values are called later)
     * @param onTick a {@link Consumer} that is called at every tick. The passed integer represents the current game tick.
     */
    public static void subscribeLoop(@NonNull String id, int priority, Consumer<Integer> onTick){
        Looper.subscribe(id, priority, onTick);
    }

    /**
     * Unsubscribes from a game loop event.<br>
     * This method removes a previously registered event consumer.
     *
     * @param id the unique identifier for the event to unsubscribe from
     */
    public static void unsubscribeLoop(String id){
        Looper.unsubscribe(id);
    }

    /**
     * Subscribes to input events.<br>
     * This method allows you to register a listener that will be notified when input events occur.
     *
     * @param id a unique identifier for the listener
     * @param priority the call priority (higher values are called later)
     * @param listener an {@link InputManager.InputListener} to handle input events
     */
    public static void subscribeInputManager(@NonNull String id, int priority, InputManager.InputListener listener){
        InputManager.subscribe(id, priority, listener);
    }

    /**
     * Unsubscribes from input events.<br>
     * This method removes a previously registered input event listener.
     *
     * @param id the unique identifier for the listener to unsubscribe from
     */
    public static void unsubscribeInputManager(String id){
        InputManager.unsubscribe(id);
    }

    /**
     * Sets the debug mode for the core.<br>
     * This method allows enabling or disabling debug mode before the core is initialized or after it has been initialized.<br><br>
     *
     * This function can only be used while the Core is in the {@link CoreStage#PRE_INIT PRE_INIT} or {@link CoreStage#INITED INITED} stage.
     *
     * @param debug true to enable debug mode, false to disable it
     */
    public static void setDebug(boolean debug) {
        validateCoreStage(CoreStage.PRE_INIT, CoreStage.INITED);
        Core.debug = debug;
    }
}
