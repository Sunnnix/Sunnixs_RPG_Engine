package de.sunnix.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {
	
	private static final LinkedList<Key> keys = new LinkedList<>();
	private static final LinkedList<Button> buttons = new LinkedList<>();
	private static final LinkedList<Axis> axes = new LinkedList<>();

	// *********************************************************** //
	//							Keyboard						   //
	// *********************************************************** //
	/**
	 * Default W
	 */
	public static final Key UP = new Key(GLFW_KEY_W);
	/**
	 * Default S
	 */
	public static final Key DOWN = new Key(GLFW_KEY_S);
	/**
	 * Default A
	 */
	public static final Key LEFT = new Key(GLFW_KEY_A);
	/**
	 * Default D
	 */
	public static final Key RIGHT = new Key(GLFW_KEY_D);

	/**
	 * Default L_CTRL
	 */
	public static final Key X = new Key(GLFW_KEY_LEFT_CONTROL);
	/**
	 * Default L_SHIFT
	 */
	public static final Key Y = new Key(GLFW_KEY_LEFT_SHIFT);
	/**
	 * Default SPACE
	 */
	public static final Key A = new Key(GLFW_KEY_SPACE);
	/**
	 * Default R_ALT
	 */
	public static final Key B = new Key(GLFW_KEY_RIGHT_ALT);

	/**
	 * Default ESC
	 */
	public static final Key START = new Key(GLFW_KEY_ESCAPE);
	/**
	 * Default BACKSPACE
	 */
	public static final Key SELECT = new Key(GLFW_KEY_BACKSPACE);

	/**
	 * Default Q
	 */
	public static final Key L_SHOULDER = new Key(GLFW_KEY_Q);
	/**
	 * Default E
	 */
	public static final Key R_SHOULDER = new Key(GLFW_KEY_E);
	/**
	 * Default 1
	 */
	public static final Key L2_SHOULDER = new Key(GLFW_KEY_1);
	/**
	 * Default 3
	 */
	public static final Key R2_SCHOULDER = new Key(GLFW_KEY_3);

	/**
	 * Default F
	 */
	public static final Key L_JOY_BUTTON = new Key(GLFW_KEY_F);
	/**
	 * Default ENTER
	 */
	public static final Key R_JOY_BUTTON = new Key(GLFW_KEY_ENTER);

	// *********************************************************** //
	//							Gamepad							   //
	// *********************************************************** //
	public static final Button PAD_UP = new Button(GLFW_GAMEPAD_BUTTON_DPAD_UP);
	public static final Button PAD_DOWN = new Button(GLFW_GAMEPAD_BUTTON_DPAD_DOWN);
	public static final Button PAD_LEFT = new Button(GLFW_GAMEPAD_BUTTON_DPAD_LEFT);
	public static final Button PAD_RIGHT = new Button(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);

	public static final Button PAD_X = new Button(GLFW_GAMEPAD_BUTTON_X);
	public static final Button PAD_Y = new Button(GLFW_GAMEPAD_BUTTON_Y);
	public static final Button PAD_A = new Button(GLFW_GAMEPAD_BUTTON_A);
	public static final Button PAD_B = new Button(GLFW_GAMEPAD_BUTTON_B);

	public static final Button PAD_START = new Button(GLFW_GAMEPAD_BUTTON_START);
	public static final Button PAD_BACK = new Button(GLFW_GAMEPAD_BUTTON_BACK);

	public static final Button PAD_L_SHOULDER = new Button(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER);
	public static final Button PAD_R_SHOULDER = new Button(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER);
	public static final Axis PAD_L2_SHOULDER = new Axis(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER);
	public static final Axis PAD_R2_SHOULDER = new Axis(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER);

	public static final Button PAD_L_JOY_BUTTON = new Button(GLFW_GAMEPAD_BUTTON_LEFT_THUMB);
	public static final Button PAD_R_JOY_BUTTON = new Button(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB);
	public static final Button PAD_GUIDE = new Button(GLFW_GAMEPAD_BUTTON_GUIDE);

	/**
	 * Left joystick horizontal movement
	 */
	public static final Axis PAD_JS_L_H = new Axis(GLFW_GAMEPAD_AXIS_LEFT_X);
	/**
	 * Left joystick vertical movement
	 */
	public static final Axis PAD_JS_L_V = new Axis(GLFW_GAMEPAD_AXIS_LEFT_Y);

	/**
	 * Right joystick horizontal movement
	 */
	public static final Axis PAD_JS_R_H = new Axis(GLFW_GAMEPAD_AXIS_RIGHT_X);
	/**
	 * Right joystick vertical movement
	 */
	public static final Axis PDA_JS_R_V = new Axis(GLFW_GAMEPAD_AXIS_RIGHT_Y);

    public static void process(long window) {
		keys.forEach(k -> k.process(window));
		// Überprüfe, ob ein Joystick angeschlossen ist
		if (GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1)) {
			// Überprüfe, ob der Joystick ein Gamepad ist
			if (GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) {
				// Erstelle einen GLFWGamepadState
				GLFWGamepadState gpState = GLFWGamepadState.create();
				// Lese den aktuellen Gamepad-Status
				if (GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, gpState)) {
					// Durchlaufe die Buttons und rufe die process-Methode auf
					for (Button button : buttons)
						button.process(gpState);
					for (Axis axis : axes)
						axis.process(gpState);
				}
			}
		}
    }

    public static class Key {
		
		public final int keyCode;
		private boolean pressed;
		private int time;
		
		public Key(int keyCode) {
			this.keyCode = keyCode;
			keys.add(this);
		}

		private void process(long window){
			time++;
			var status = glfwGetKey(window, keyCode);
			if(status == GLFW_PRESS && !pressed){
				pressed = true;
				time = 0;
			} else if(status == GLFW_RELEASE && pressed){
				pressed = false;
				time = 0;
			}
		}

		public boolean isPressed(){
			return pressed;
		}

		public boolean startPressed(){
			return pressed && time == 0;
		}

		public boolean isReleased(){
			return !pressed;
		}

		public boolean startRelease(){
			return !pressed && time == 0;
		}
		
		public int time() {
			return time;
		}
		
	}

	public static class Button {
		public final int buttonCode;
		private boolean pressed;
		private int time;

		public Button(int buttonCode){
			this.buttonCode = buttonCode;
			buttons.add(this);
		}

		private void process(GLFWGamepadState gpState){
			time++;
			var pressed = gpState.buttons(buttonCode) == GLFW_PRESS;
			if(pressed && !this.pressed){
				this.pressed = true;
				time = 0;
			} else if(!pressed && this.pressed){
				this.pressed = false;
				time = 0;
			}
		}

		public boolean isPressed(){
			return pressed;
		}

		public boolean startPressed(){
			return pressed && time == 0;
		}

		public boolean isReleased(){
			return !pressed;
		}

		public boolean startRelease(){
			return !pressed && time == 0;
		}

		public int time() {
			return time;
		}
	}

	public static class Axis {

		public final int axisCode;
		private float position;

		public Axis(int axisCode){
			this.axisCode = axisCode;
			axes.add(this);
		}

		private void process(GLFWGamepadState gpState){
			this.position = Math.round(gpState.axes(axisCode) * 4) / 4f;
		}

		public float getLeft(){
			return Math.max(-position, 0);
		}

		public float getRight(){
			return Math.max(position, 0);
		}

		public float getUp(){
			return getLeft();
		}

		public float getDown(){
			return getRight();
		}

	}
	
}
