package de.sunnix.aje.editor.window.resource.audio;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

public class AudioResource {

    private static final String[] SUFFIXES = { "Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };

    public final int ID;

    /**
     * byte or short array
     */
    @Getter
    private final byte[] data;
    @Getter
    private final String extension;

    public int frequency;
    public final int bitDepth;
    public final int channels;

    @Getter
    @Setter
    private String name;

    public AudioResource(String name, String path) throws Exception {
        if(path.endsWith(".") || !path.contains("."))
            throw new RuntimeException("Unknown file format!");
        this.name = name;
        this.extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
        var data = AudioDecoder.decode(path, extension);
        ID = alGenBuffers();
        checkALError("creating buffer", true);
        setData(data);

        frequency = alGetBufferi(ID, AL_FREQUENCY);
        bitDepth = alGetBufferi(ID, AL_BITS);
        channels = alGetBufferi(ID, AL_CHANNELS);

        this.data = data.data();
    }

    public AudioResource(DataSaveObject dso) throws Exception {
        this.name = dso.getString("name", null);
        this.data = dso.getByteArray("data");
        this.extension = dso.getString("extension", null);
        var data = AudioDecoder.decode(this.data, extension);
        ID = alGenBuffers();
        checkALError("creating buffer", true);
        setData(data);

        frequency = alGetBufferi(ID, AL_FREQUENCY);
        bitDepth = alGetBufferi(ID, AL_BITS);
        channels = alGetBufferi(ID, AL_CHANNELS);
    }

    private void setData(AudioDecoder.AudioData data) {
        if(data.buffer() instanceof ByteBuffer buffer)
            alBufferData(ID, data.format(), buffer, data.sampleRate());
        else if(data.buffer() instanceof ShortBuffer buffer)
            alBufferData(ID, data.format(), buffer, data.sampleRate());
        else
            throw new RuntimeException("Unknown audio type " + data.data().getClass());
        checkALError("setting audio data", true);
    }

    public int getSize(){
        return alGetBufferi(ID, AL_SIZE);
    }

    public String getRawSizeInText(){
        int suffixIndex = 0;
        double size = data.length;
        while (size >= 1024 && suffixIndex < SUFFIXES.length - 1){
            suffixIndex++;
            size /= 1024;
        }
        return String.format("%.1f %s", size, SUFFIXES[suffixIndex]);
    }

    public void cleanup() {
        // Clean up the source
        alDeleteBuffers(ID);
    }

    /**
     * converts a size (bytes) of this track to milliseconds
     */
    public int convertBytesToMillies(int bytes){
        if(bytes == 0)
            return 0;
        return (int)(bytes / (float)(frequency * bitDepth / 8 * channels) * 1000);
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

    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("name", name);
        dso.putString("extension", extension);
        dso.putArray("data", data);
        return dso;
    }
}
