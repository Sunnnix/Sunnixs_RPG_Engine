package de.sunnix.srpge.editor.window.resource.audio;

import lombok.Getter;

import static de.sunnix.srpge.engine.audio.OpenALContext.checkALError;
import static org.lwjgl.openal.AL11.*;

public class AudioSpeaker {

    public final int ID;
    @Getter
    private AudioResource audio;
    private float gain = 1;

    public AudioSpeaker() throws RuntimeException{
        ID = alGenSources();
        checkALError("creating speaker", true);
        alSourcef(ID, AL_MAX_GAIN, 4);
    }

    public AudioSpeaker(AudioResource audio) throws RuntimeException{
        this();
        setAudio(audio);
    }

    public void setAudio(AudioResource audio){
        this.audio = audio;
        alSourcei(ID, AL_BUFFER, audio == null ? 0 : audio.ID);
        checkALError("binding source", true);
        setGain(gain);
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

    public int getFrequency(){
        return audio.frequency;
    }

    public int getBitDepth(){
        return audio.bitDepth;
    }

    public int getChannelCount(){
        return audio.channels;
    }

    public int getSize(){
        return audio.getSize();
    }

    public int getPosition(){
        return alGetSourcei(ID, AL_BYTE_OFFSET);
    }

    public int getLengthInMS(){
        return audio.convertBytesToMillies(getSize());
    }

    public int getPositionInMS(){
        return audio.convertBytesToMillies(getPosition());
    }

    public void setPosition(int pos){
        alSourcei(ID, AL_BYTE_OFFSET, pos);
    }

    public float getGain(){
        return alGetSourcef(ID, AL_GAIN);
    }

    public void setGain(float gain){
        alSourcef(ID, AL_GAIN, gain * (audio == null ? 1 : audio.getDefaultGain()));
    }

    public void cleanup() {
        alDeleteSources(ID);
    }

}
