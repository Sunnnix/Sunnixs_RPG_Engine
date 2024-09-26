package de.sunnix.srpge.engine.util;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.debug.GameLogger;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.joml.Math.lerp;
import static org.lwjgl.opengl.GL30.*;

public class FunctionUtils {

    public static final float EPSILON = 1e-4f;

    /**
     * Checks the latest GL errors and prints them into the logger
     * @param caller The name to be displayed in the logger
     */
    public static void checkForOpenGLErrors(String caller) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            if(Core.isGl_error_stacktrace())
                GameLogger.logException(caller, new Exception("OpenGL Error: " + getGLErrorString(error)));
            else
                GameLogger.logE(caller, "OpenGL Error: " + getGLErrorString(error));
        }
    }

    /**
     * Gets a readable string for the GL enum
     * @param error The GL enum
     * @return Readable string
     */
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

    /**
     * Checks if a flag contains a specific bit value
     * @param flag The flag to be checked
     * @param check The specific bit value
     * @return Does the flag contains the bit value
     */
    public static boolean bitcheck(int flag, int check){
        return (flag & check) == check;
    }

    /**
     * Gets the first matching value of a collection or null
     * @param collection the collection to check
     * @param expression the expression that checks for the value
     * @return The first found value or null
     */
    public static <T> T firstOrNull(Collection<T> collection, Function<T, Boolean> expression){
        for(var item: collection){
            if(expression.apply(item))
                return item;
        }
        return null;
    }

    /**
     * Gets the first matching value of a collection
     * @param collection the collection to check
     * @param expression the expression that checks for the value
     * @return The first found value
     * @throws NullPointerException if there's no element matching or the collection is null
     */
    public static <T> T first(Collection<T> collection, Function<T, Boolean> expression) throws NullPointerException{
        var value = firstOrNull(collection, expression);
        if(value == null)
            throw new NullPointerException("No matching value with this expression");
        return value;
    }

    /**
     * Gets the first matching value of a collection or the elseValue if not found
     * @param collection the collection to check
     * @param expression the expression that checks for the value
     * @param elseValue the value that will be returned if nothing is found
     * @return The first found value or elseValue
     */
    public static <T> T firstOrElse(Collection<T> collection, Function<T, Boolean> expression, T elseValue){
        var value = firstOrNull(collection, expression);
        return value == null ? elseValue : value;
    }

    /**
     * Converts a List of shorts to a short array
     * @param list List of shorts
     * @return converted short array
     */
    public static short[] shortListToArray(List<Short> list){
        var arr = new short[list.size()];
        for(var i = 0; i < list.size(); i++)
            arr[i] = list.get(i);
        return arr;
    }


    /**
     * Converts a short array to a mutable list of shorts
     * @param arr short array
     * @return converted mutable list of shorts
     */
    public static ArrayList<Short> shortArrayToList(short[] arr){
        var list = new ArrayList<Short>();
        for(var s: arr)
            list.add(s);
        return list;
    }

    /**
     * Reverses an array
     * @param array Array to reverse
     * @return Reversed array
     */
    public static <T> T[] arrayReversed(T[] array){
        for(int i = 0; i < array.length / 2; i++) {
            var temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
        return array;
    }

    /**
     * equivalent to GLSL mix
     * @param c1 first color
     * @param c2 second color
     * @param factor factor of second color
     * @return mixed color
     */
    public static Vector3f mix(Vector3f c1, Vector3f c2, float factor){
        return new Vector3f(
                lerp(c1.x, c2.x, factor),
                lerp(c1.y, c2.y, factor),
                lerp(c1.z, c2.z, factor)
        );
    }

    /**
     * Mixes two colors to a new one
     * @param c1 first color
     * @param c2 second color
     * @param factor factor of the second color
     * @return mixed color
     */
    public static Vector4f mix(Vector4f c1, Vector4f c2, float factor){
        float r = lerp(c1.x, c2.x, factor);
        float g = lerp(c1.y, c2.y, factor);
        float b = lerp(c1.z, c2.z, factor);
        float a = lerp(c1.w, c2.w, factor);
        return new Vector4f(r, g, b, a);
    }

}
