package de.sunnix.aje.engine.audio;

import lombok.Getter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

@Getter
public class DefaultAudioDecoder implements IAudioDecoder{

    private final String[] supportedExtensions = { "wav", "au", "aif", "aifc", "snd" };

    @Override
    public AudioDecoder.AudioData decode(InputStream stream) throws Exception{
        stream.mark(Integer.MAX_VALUE);
        var bytes = stream.readAllBytes();
        stream.reset();
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(stream)) {
            int format;
            int sampleRate;
            ByteBuffer buffer;
            AudioFormat aFormat = ais.getFormat();
            if (aFormat.getChannels() == 1) {
                if (aFormat.getSampleSizeInBits() == 8)
                    format = AL_FORMAT_MONO8;
                else if (aFormat.getSampleSizeInBits() == 16)
                    format = AL_FORMAT_MONO16;
                else
                    throw new RuntimeException("Illegal sample size");
            } else if (aFormat.getChannels() == 2) {
                if (aFormat.getSampleSizeInBits() == 8)
                    format = AL_FORMAT_STEREO8;
                else if (aFormat.getSampleSizeInBits() == 16)
                    format = AL_FORMAT_STEREO16;
                else
                    throw new RuntimeException("Illegal sample size");
            } else {
                throw new RuntimeException("Only mono or stereo is supported");
            }
            sampleRate = (int) aFormat.getSampleRate();
            int available = ais.available();
            if (available <= 0) {
                available = aFormat.getChannels() * (int) ais.getFrameLength() * aFormat.getSampleSizeInBits() / 8;
            }
            byte[] data = new byte[ais.available()];
            int read = 0, total = 0;
            while ((read = ais.read(data, total, data.length - total)) != -1 && total < data.length) {
                total += read;
            }

            buffer = ByteBuffer.allocateDirect(data.length);
            buffer.order(ByteOrder.nativeOrder());
            ByteBuffer src = ByteBuffer.wrap(data);
            src.order(aFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            if (aFormat.getSampleSizeInBits() == 16) {
                ShortBuffer dest_short = buffer.asShortBuffer();
                ShortBuffer src_short = src.asShortBuffer();
                while (src_short.hasRemaining())
                    dest_short.put(src_short.get());
            } else {
                while (src.hasRemaining())
                    buffer.put(src.get());
            }
            buffer.rewind();
            return new AudioDecoder.AudioData(bytes, buffer, format, sampleRate);
        }
    }

}
