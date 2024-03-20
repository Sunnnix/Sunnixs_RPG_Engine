package de.sunnix.engine.graphics;

import de.sunnix.engine.Core;
import de.sunnix.engine.ILoggable;
import de.sunnix.engine.debug.BuildData;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private Window() {}

    @Accessors(chain = true)
    @Setter
    public static class WindowBuilder implements ILoggable {

        @Setter(AccessLevel.NONE)
        private String title;

        @Setter(AccessLevel.NONE)
        private int width, height;

        @Setter(AccessLevel.NONE)
        private boolean gl_debug_enabled;

        private boolean resizable;

        private GLFWWindowSizeCallbackI sizeCallback = (win, w, h) -> {
            glViewport(0, 0, w, h);
            Camera.getSize().set(w / Core.getPixel_scale(), h / Core.getPixel_scale());
        };
        private GLFWWindowFocusCallbackI focusCallback;

        public WindowBuilder(String title, int width, int height, boolean gl_debug_enabled){
            this.title = title;
            this.width = width;
            this.height = height;
            this.gl_debug_enabled = gl_debug_enabled;
        }

        public long build(){
            // Set hints
            glfwDefaultWindowHints();

            if(gl_debug_enabled)
                glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

            // Create and bind
            var window = glfwCreateWindow(width, height, title, NULL, NULL);
            if (window == NULL)
                throw new RuntimeException("No window could be created!");
            glfwMakeContextCurrent(window);

            createCapabilities();

            // Icon
            loadWindowIcon(window);

            var dim = Toolkit.getDefaultToolkit().getScreenSize();
            glfwSetWindowPos(window, dim.width / 2 - width / 2, dim.height / 2 - height / 2);

            Camera.getSize().set(width / Core.getPixel_scale(), height / Core.getPixel_scale());

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
                var images = GLFWImage.create(1);
                images.get(0).set(image.getWidth() , image.getHeight(), buffer);
                glfwSetWindowIcon(window, images);
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
