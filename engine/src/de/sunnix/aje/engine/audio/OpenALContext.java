package de.sunnix.aje.engine.audio;

import lombok.Getter;
import org.lwjgl.openal.*;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC.createCapabilities;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenALContext {

    private static boolean inited;
    private static long device = NULL;
    private static long context = NULL;

    public static void setUp(){
        if(inited)
            return;
        device = alcOpenDevice((CharSequence) null);
        context = alcCreateContext(device, (int[]) null);
        alcMakeContextCurrent(context);
        var alcCapabilities = createCapabilities(device);
        AL.createCapabilities(alcCapabilities);
        checkALError("OpenAL context", true);
        inited = true;
    }

    public static void close(){
        if(!inited)
            return;
        alcMakeContextCurrent(NULL);
        alcDestroyContext(context);
        context = NULL;
        alcCloseDevice(device);
        device = NULL;
    }

    public static void checkALError(String caller, boolean throwException){
        var error = alGetError();
        if(error != AL_NO_ERROR)
            if(throwException)
                throw new RuntimeException(String.format("OpenAL error in %s: %s", caller, getALError(error)));
            else
                System.err.println(String.format("OpenAL error in %s: %s", caller, getALError(error)));
    }

    public static String getALError(int code){
        return switch (code) {
            case AL_NO_ERROR -> "No Error";
            case AL_INVALID_NAME -> "Invalid Name";
            case AL_INVALID_ENUM -> "Invalid Enum";
            case AL_INVALID_VALUE -> "Invalid Value";
            case AL_INVALID_OPERATION -> "Invalid Operation";
            case AL_OUT_OF_MEMORY -> "Out of Memory";
            default -> "Unknown Error";
        };
    }

}
