package de.sunnix.aje.engine.audio;

import de.sunnix.aje.engine.audio.AudioDecoder;

import java.io.InputStream;

public interface IAudioDecoder {

    String[] getSupportedExtensions();

    AudioDecoder.AudioData decode(InputStream stream) throws Exception;

}
