package de.sunnix.engine.graphics;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class Window {

    private Window() {}

    @Accessors(chain = true)
    @Setter
    public static class WindowBuilder{

        @Setter(AccessLevel.NONE)
        private String title;

        @Setter(AccessLevel.NONE)
        private int width, height;

        private boolean resizable;

        private GLFWWindowSizeCallbackI sizeCallback = (win, w, h) -> glViewport(0, 0, w, h);
        private GLFWWindowFocusCallbackI focusCallback;

        public WindowBuilder(String title, int width, int height){
            this.title = title;
            this.width = width;
            this.height = height;
        }

        public long build(){
            // Set hints
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

            // Create and bind
            var window = glfwCreateWindow(width, height, title, 0, 0);
            if (window == 0)
                throw new RuntimeException("No window could be created!");
            glfwMakeContextCurrent(window);

            // Callbacks
            if(sizeCallback != null)
                glfwSetWindowSizeCallback(window, sizeCallback);
            if(focusCallback != null)
                glfwSetWindowFocusCallback(window, focusCallback);

            return window;
        }

    }

}
