package de.sunnix.srpge.engine.util;

import de.sunnix.srpge.engine.debug.GameLogger;

import static org.lwjgl.opengl.GL30.*;

public class Utils {

    public static void checkForOpenGLErrors(String caller) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            GameLogger.logE(caller, "OpenGL Error: " + getGLErrorString(error));
        }
    }

    public static String getGLErrorString(int error) {
        return switch (error) {
            case GL_NO_ERROR -> "No Error";
            case GL_INVALID_ENUM -> "Invalid Enum";
            case GL_INVALID_VALUE -> "Invalid Value";
            case GL_INVALID_OPERATION -> "Invalid Operation";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> "Invalid Framebuffer Operation";
            case GL_OUT_OF_MEMORY -> "Out of Memory";
            case GL_STACK_UNDERFLOW -> "Stack Underflow";
            case GL_STACK_OVERFLOW -> "Stack Overflow";
            default -> "Unknown Error";
        };
    }

    public static boolean bitcheck(int flag, int check){
        return (flag & check) == check;
    }

}
