package de.sunnix.engine.graphics;

import de.sunnix.engine.ILoggable;
import de.sunnix.engine.debug.BuildData;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window {

    private Window() {}

    @Accessors(chain = true)
    @Setter
    public static class WindowBuilder implements ILoggable {

        @Setter(AccessLevel.NONE)
        private String title;

        @Setter(AccessLevel.NONE)
        private int width, height;

        private boolean resizable;

        private GLFWWindowSizeCallbackI sizeCallback = (win, w, h) -> {
            glViewport(0, 0, w, h);
            Camera.getSize().set(w, h);
        };
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

            // Icon
            loadWindowIcon(window);

            Camera.getSize().set(width, height);

            // Callbacks
            if(sizeCallback != null)
                glfwSetWindowSizeCallback(window, sizeCallback);
            if(focusCallback != null)
                glfwSetWindowFocusCallback(window, focusCallback);

            return window;
        }

        private void loadWindowIcon(long window){
            try {
                var image = Texture.loadImage("/assets/textures/icon/" + BuildData.getData("icon"));
                var buffer = Texture.getImagePixelsAsBuffer(image);
                try(var images = GLFWImage.create(1)){
                    images.get(0).set(image.getWidth() , image.getHeight(), buffer);
                    glfwSetWindowIcon(window, images);
                }
            } catch (Exception e){
                logError(new RuntimeException("Error loading window icon", e));
            }
        }

        @Override
        public String getCallerName() {
            return "WindowBuilder";
        }
    }

}
