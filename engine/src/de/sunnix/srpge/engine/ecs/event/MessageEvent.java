package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.graphics.gui.GUIManager;
import de.sunnix.srpge.engine.graphics.gui.SpeechBox;

import java.nio.charset.StandardCharsets;

public class MessageEvent extends Event{

    protected String name;
    protected String message;
    protected SpeechBox.SoundType soundType;
    protected boolean yesNoOption;
    private EventList onYes, onNo;

    private int messageID; // automatically generated
    private int selectedOption = 0; // 0 = yes, 1 = no

    public MessageEvent() {
        super("message");
        super.blockingType = EventList.BlockType.USER_INPUT;
    }

    @Override
    public void load(DataSaveObject dso) {
        name = new String(dso.getByteArray("name"), StandardCharsets.UTF_8);
        message = new String(dso.getByteArray("msg"), StandardCharsets.UTF_8);
        soundType = SpeechBox.SoundType.values()[dso.getByte("st", (byte)0)];
        yesNoOption = dso.getBool("yn", false);
        if(yesNoOption){
            onYes = new EventList(dso.getObject("y_events"));
            onNo = new EventList(dso.getObject("n_events"));
        }
    }

    @Override
    public void prepare(World world, GameObject parent) {
        super.prepare(world, parent);
        selectedOption = -1;
        messageID = GUIManager.showSpeechBox(name, message, soundType, yesNoOption ? b -> {
            selectedOption = b ? 0 : 1;
            if (b)
                onYes.start(owner);
            else
                onNo.start(owner);
        } : null);
    }

    @Override
    public void run(World world) {
        if(GUIManager.isSpeechBoxFinished(messageID) && yesNoOption && selectedOption > -1)
            if(selectedOption == 0)
                onYes.run(world);
            else
                onNo.run(world);
    }

    @Override
    public boolean isFinished(World world) {
        var finished = GUIManager.isSpeechBoxFinished(messageID);
        finished = finished && (!yesNoOption || (selectedOption == 0 ? !onYes.isActive() : !onNo.isActive()));
        return finished;
    }

    @Override
    public void finish(World world) {
        messageID = -1;
    }
}
