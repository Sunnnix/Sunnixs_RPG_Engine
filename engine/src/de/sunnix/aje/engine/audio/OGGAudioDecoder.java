package de.sunnix.aje.engine.audio;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.io.InputStream;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;

@Getter
public class OGGAudioDecoder implements IAudioDecoder{

    private final String[] supportedExtensions = { "ogg" };

    @Override
    public AudioDecoder.AudioData decode(InputStream stream) throws Exception {
        var bytes = stream.readAllBytes();
        var buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        var channels = BufferUtils.createIntBuffer(1);
        var sampleRate = BufferUtils.createIntBuffer(1);

        var bufferOut = stb_vorbis_decode_memory(buffer, channels, sampleRate);

        return new AudioDecoder.AudioData(bytes, bufferOut, channels.get() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, sampleRate.get());
    }
}
