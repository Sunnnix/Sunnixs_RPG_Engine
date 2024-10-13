package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.audio.AudioSpeaker;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.resources.Resources;

public class PlaySoundEvent extends Event{

    protected String sound;
    protected boolean useLocation;
    protected float posX, posY, posZ;
    protected float gain = 1;
    protected boolean waitForEnd;

    private AudioSpeaker speaker;

    public PlaySoundEvent() {
        super("playsound");
    }

    @Override
    public void load(DataSaveObject dso) {
        sound = dso.getString("sound", null);
        useLocation = dso.getBool("useLocation", false);
        posX = dso.getFloat("x", 0);
        posY = dso.getFloat("y", 0);
        posZ = dso.getFloat("z", 0);
        gain = dso.getFloat("g", 1);
        waitForEnd = dso.getBool("wait", false);
    }

    @Override
    public void prepare(World world) {
        var res = Resources.get().getAudio(sound);
        if(res == null)
            return;
        if(useLocation)
            speaker = AudioManager.get().playSound(res, true, posX, posY, posZ, gain);
        else
            speaker = AudioManager.get().playSound(res, gain);
    }

    @Override
    public void run(World world) {}

    @Override
    public boolean isFinished(World world) {
        return !waitForEnd || !speaker.isPlaying();
    }

    @Override
    public boolean isInstant(World world) {
        return !waitForEnd;
    }
}
