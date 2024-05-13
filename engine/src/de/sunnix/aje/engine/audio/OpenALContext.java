package de.sunnix.aje.engine.audio;

import org.lwjgl.openal.*;

import static org.lwjgl.openal.AL10.AL_NO_ERROR;
import static org.lwjgl.openal.AL10.alGetError;
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
        if(alGetError() != AL_NO_ERROR)
            System.err.println("AL error occurred!");
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

}
