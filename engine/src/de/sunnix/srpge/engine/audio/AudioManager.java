package de.sunnix.srpge.engine.audio;

import org.lwjgl.openal.AL10;

import java.util.Objects;

public class AudioManager {

    private static AudioManager instance;

    private final AudioSpeaker bgm;
    private AudioResource bgm_resource;

    private final AudioSpeaker[] sounds;
    private final int soundBuffer = 64;
    private int soundPointer;

    public AudioManager(){
        bgm = new AudioSpeaker();
        bgm.setLooping(true);
        sounds = new AudioSpeaker[soundBuffer];
        for (int i = 0; i < soundBuffer; i++)
            sounds[i] = new AudioSpeaker();
    }

    public static AudioManager get(){
        if(instance == null)
            instance = new AudioManager();
        return instance;
    }

    public void setLocation(float x, float y, float z){
        AL10.alListener3f(AL10.AL_POSITION, x, y, z);
    }

    public void setBGM(AudioResource audio){
        if(audio != bgm_resource) {
            bgm_resource = audio;
            bgm.setAudio(audio);
        }
        if(audio != null) {
            if(!bgm.isPlaying())
                bgm.play();
        }
    }

    public void playBGM(){
        bgm.play();
    }

    public void pauseBGM(){
        bgm.pause();
    }

    public void stopBGM(){
        bgm.stop();
    }

    public void cleanup(){
        bgm.cleanup();
    }

    public AudioSpeaker playSound(AudioResource audio){
        return playSound(audio, 1);
    }

    public AudioSpeaker playSound(AudioResource audio, float gain){
        return playSound(audio, false, 0, 0, 0, gain);
    }

    public AudioSpeaker playSound(AudioResource audio, boolean use3DSound, float x, float y, float z, float gain){
        var speaker = sounds[soundPointer++];
        if(soundPointer >= soundBuffer)
            soundPointer = 0;
        speaker.stop();
        speaker.setAudio(audio);
        if(use3DSound)
            speaker.setLocation(x, y, z);
        speaker.setGain(audio.defaultGain * gain);
        speaker.play();
        return speaker;
    }

    public void stopAllSounds(){
        for (int i = 0; i < soundBuffer; i++) {
            var speaker = sounds[soundBuffer];
            speaker.stop();
            speaker.setAudio(null);
        }
        soundPointer = 0;
    }

}
