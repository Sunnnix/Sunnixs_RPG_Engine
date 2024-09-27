package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.World;
import org.joml.Vector4f;

import static org.joml.Math.lerp;

public class GlobalColorTintEvent extends Event {

    protected float[] color;
    protected int maxDelay;
    private int delay;
    private Vector4f initialColor;

    public GlobalColorTintEvent() {
        super("global_color_tint");
    }

    @Override
    public void load(DataSaveObject dso) {
        color = dso.getFloatArray("color", 4);
        maxDelay = dso.getInt("delay", 0);
        parallel = dso.getBool("parallel", false);
    }

    @Override
    public void prepare(World world) {
        delay = 0;
        initialColor = Core.getGlobalColoring().get(new Vector4f());
    }

    @Override
    public void run(World world) {
        delay++;

        float progress = Math.min((float) delay / maxDelay, 1.0f);

        float r = lerp(initialColor.x, color[0], progress);
        float g = lerp(initialColor.y, color[1], progress);
        float b = lerp(initialColor.z, color[2], progress);
        float a = lerp(initialColor.w, color[3], progress);

        Core.getGlobalColoring().set(r, g, b, a);
    }

    @Override
    public boolean isFinished(World world) {
        return delay >= maxDelay;
    }

    @Override
    public void finish(World world) {}
}
