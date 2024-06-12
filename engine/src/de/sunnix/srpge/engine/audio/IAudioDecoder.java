package de.sunnix.srpge.engine.audio;

import java.io.InputStream;

public interface IAudioDecoder {

    String[] getSupportedExtensions();

    AudioDecoder.AudioData decode(InputStream stream) throws Exception;

}
