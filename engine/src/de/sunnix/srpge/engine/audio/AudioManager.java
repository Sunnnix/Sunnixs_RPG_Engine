package de.sunnix.srpge.engine.audio;

public class AudioManager {

    private static AudioManager instance;

    private final AudioSpeaker bgm;

    public AudioManager(){
        bgm = new AudioSpeaker();
        bgm.setLooping(true);
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

}
