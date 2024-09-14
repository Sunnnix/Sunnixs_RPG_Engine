package de.sunnix.srpge.engine.util;

import de.sunnix.srpge.engine.debug.GameLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.lwjgl.opengl.GL30.*;

public class FunctionUtils {

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

    public static <T> T firstOrNull(Collection<T> collection, Function<T, Boolean> expression){
        for(var item: collection){
            if(expression.apply(item))
                return item;
        }
        return null;
    }

    public static <T> T first(Collection<T> collection, Function<T, Boolean> expression){
        var value = firstOrNull(collection, expression);
        if(value == null)
            throw new NullPointerException("No matching value with this expression");
        return value;
    }

    public static <T> T firstOrElse(Collection<T> collection, Function<T, Boolean> expression, T elseValue){
        var value = firstOrNull(collection, expression);
        return value == null ? elseValue : value;
    }

    public static short[] shortListToArray(List<Short> list){
        var arr = new short[list.size()];
        for(var i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }

    public static ArrayList<Short> shortArrayToList(short[] arr){
        var list = new ArrayList<Short>();
        for(var s: arr)
            list.add(s);
        return list;
    }

    public static <T> T[] arrayReversed(T[] array){
        for(int i = 0; i < array.length / 2; i++) {
            var temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
        return array;
    }

}
