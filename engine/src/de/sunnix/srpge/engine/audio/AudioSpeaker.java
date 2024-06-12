package de.sunnix.srpge.engine.audio;

import lombok.Getter;

import static de.sunnix.srpge.engine.audio.OpenALContext.checkALError;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL11.AL_BYTE_OFFSET;

public class AudioSpeaker {

    public final int ID;
    @Getter
    private AudioResource audio;

    public AudioSpeaker(AudioResource audio){
        ID = alGenSources();
        setAudio(audio);
        checkALError("creating speaker", true);
        alSourcef(ID, AL_MAX_GAIN, 2);
    }

    public AudioSpeaker(){
        this(null);
    }

    public void setAudio(AudioResource audio){
        this.audio = audio;
        if(audio == null)
            alSourcei(ID, AL_BUFFER, 0);
        else {
            setGain(audio.defaultGain);
            alSourcei(ID, AL_BUFFER, audio.ID);
        }
        checkALError("creating speaker", true);
    }

    public void play() {
        alSourcePlay(ID);
    }

    public void pause() {
        alSourcePause(ID);
    }

    public void stop(){
        alSourceStop(ID);
    }

    public boolean isPlaying(){
        return alGetSourcei(ID, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public int getPosition(){
        return alGetSourcei(ID, AL_BYTE_OFFSET);
    }

    public void setPosition(int pos){
        alSourcei(ID, AL_BYTE_OFFSET, pos);
    }

    public float getGain(){
        return alGetSourcef(ID, AL_GAIN);
    }

    public void setGain(float gain){
        alSourcef(ID, AL_GAIN, gain);
    }

    public void setLooping(boolean looping){
        alSourcei(ID, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }

    public void cleanup() {
        alDeleteSources(ID);
    }

}
