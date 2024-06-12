package de.sunnix.srpge.engine.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioDecoder {

    private static final List<IAudioDecoder> decoders = new ArrayList<>();

    static {
        decoders.add(new OGGAudioDecoder());
        decoders.add(new DefaultAudioDecoder());
    }

    public static AudioData decode(String path, String extension) throws Exception{
        try (var stream = new BufferedInputStream(new FileInputStream(path))){
            return decode(stream, extension);
        }
    }

    public static AudioData decode(byte[] data, String extension) throws Exception {
        try (var stream = new BufferedInputStream(new ByteArrayInputStream(data))){
            return decode(stream, extension);
        }
    }

    public static AudioData decode(InputStream stream, String extension) throws Exception {
        for(var decoder: decoders){
            var supportedExtensions = decoder.getSupportedExtensions();
            if(supportedExtensions == null || Arrays.asList(supportedExtensions).contains(extension))
                return decoder.decode(stream);
        }
        throw new Exception(String.format("Extension \"%s\" is not supported!", extension));
    }

    public record AudioData(byte[] data, Buffer buffer, int format, int sampleRate) {}

}
