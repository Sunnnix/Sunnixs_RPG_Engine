package de.sunnix.aje.editor.window.resource.audio;

import lombok.Getter;

import static org.lwjgl.openal.AL11.*;

public class AudioSpeaker {

    public final int ID;
    @Getter
    private final AudioResource audio;

    public AudioSpeaker(AudioResource audio) throws RuntimeException{
        this.audio = audio;
        ID = alGenSources();
        AudioResource.checkALError("creating speaker", true);
        alSourcei(ID, AL_BUFFER, audio.ID);
        AudioResource.checkALError("binding source", true);
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

    public void cleanup() {
        alDeleteSources(ID);
    }

}
