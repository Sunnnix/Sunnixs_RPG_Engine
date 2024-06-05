package de.sunnix.aje.engine.ecs.event;

import de.sunnix.aje.engine.ecs.World;
import de.sunnix.aje.engine.graphics.gui.GUIManager;
import de.sunnix.aje.engine.graphics.gui.SpeechBox;
import de.sunnix.sdso.DataSaveObject;

public class MessageEvent extends Event{

    private String message;

    private SpeechBox sb;

    public MessageEvent() {
        super("message");
        super.blockingType = BLOCK_UPDATE;
    }

    @Override
    public void load(DataSaveObject dso) {
        message = dso.getString("msg", "");
    }

    @Override
    public void prepare(World world) {
        sb = GUIManager.showSpeechBox(null, message);
    }

    @Override
    public void run(World world) {}

    @Override
    public boolean isFinished(World world) {
        return sb != null && sb.isFinished() && !sb.isVisible();
    }

    @Override
    public void finish(World world) {
        sb = null;
    }
}
