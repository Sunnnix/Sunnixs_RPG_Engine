package de.sunnix.srpge.engine.audio;

import de.sunnix.sdso.DataSaveObject;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static de.sunnix.srpge.engine.audio.OpenALContext.checkALError;
import static org.lwjgl.openal.AL10.*;

public class AudioResource {

    public final int ID;
    public final String name;
    public final float defaultGain;

    public AudioResource(DataSaveObject dso) throws Exception {
        this.name = dso.getString("name", null);
        this.defaultGain = dso.getFloat("def_gain", 1);
        var data = AudioDecoder.decode(dso.getByteArray("data"), dso.getString("extension", null));
        this.ID = alGenBuffers();
        checkALError("creating buffer", true);
        setData(data);
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

    public void cleanup() {
        // Clean up the source
        alDeleteBuffers(ID);
    }

}
