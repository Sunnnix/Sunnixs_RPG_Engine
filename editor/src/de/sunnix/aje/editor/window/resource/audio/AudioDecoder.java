package de.sunnix.aje.editor.window.resource.audio;

import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;

public class AudioDecoder {

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
        return switch (extension) {
            case "mp1", "mp2", "mp3": // with JavaMP3 lib
//                    yield decodeMPEG(stream);
                throw new RuntimeException("MPEG not supported!");
            case "ogg":
                yield decodeOGG(stream);
            default:
                yield decodeJavaDecoder(stream);
        };
    }

    private static AudioData decodeJavaDecoder(InputStream stream) throws Exception {
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
            return new AudioData(bytes, buffer, format, sampleRate);
        }
    }

//    private static AudioData decodeMPEG(InputStream stream) throws Exception {
//        try(Sound sound = new Sound(stream)){
//            var os = new ByteArrayOutputStream();
//            int read = sound.decodeFullyInto(os);
//            var buffer = BufferUtils.createByteBuffer(read);
//            var bytes = os.toByteArray();
//            buffer.put(bytes).flip();
//            return new AudioData(buffer, sound.isStereo() ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, sound.getSamplingFrequency());
//        }
//    }

    private static AudioData decodeOGG(InputStream stream) throws Exception {
        var bytes = stream.readAllBytes();
        var buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        var channels = BufferUtils.createIntBuffer(1);
        var sampleRate = BufferUtils.createIntBuffer(1);

        var bufferOut = stb_vorbis_decode_memory(buffer, channels, sampleRate);

        return new AudioData(bytes, bufferOut, channels.get() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, sampleRate.get());
    }

    public record AudioData(byte[] data, Buffer buffer, int format, int sampleRate) {}

}
