package de.sunnix.srpge.engine.audio;

public class AudioManager {

    private static AudioManager instance;

    private final AudioSpeaker bgm;

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

    public void setBGM(AudioResource audio){
        bgm.setAudio(audio);
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

    public void playSound(AudioResource audio){
        var speaker = sounds[soundPointer++];
        if(soundPointer >= soundBuffer)
            soundPointer = 0;
        speaker.stop();
        speaker.setAudio(audio);
        speaker.play();
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
