package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.graphics.gui.GUIManager;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.graphics.gui.SpeechBox;

import java.nio.charset.StandardCharsets;

public class MessageEvent extends Event{

    private String name;
    private String message;
    private SpeechBox.SoundType soundType;

    private int messageID;

    public MessageEvent() {
        super("message");
        super.blockingType = BLOCK_UPDATE;
    }

    @Override
    public void load(DataSaveObject dso) {
        name = new String(dso.getByteArray("name"), StandardCharsets.UTF_8);
        message = new String(dso.getByteArray("msg"), StandardCharsets.UTF_8);
        soundType = SpeechBox.SoundType.values()[dso.getByte("st", (byte)0)];
    }

    @Override
    public void prepare(World world) {
        messageID = GUIManager.showSpeechBox(name, message, soundType);
    }

    @Override
    public void run(World world) {}

    @Override
    public boolean isFinished(World world) {
        return GUIManager.isSpeechBoxFinished(messageID);
    }

    @Override
    public void finish(World world) {
        messageID = -1;
    }
}
