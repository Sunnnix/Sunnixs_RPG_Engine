package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.Tile;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.util.Tuple.Tuple3;

import java.util.ArrayList;
import java.util.List;

/**
 * This event allows modifying the texture index of walls and the ground of a {@link de.sunnix.srpge.engine.ecs.Tile Tile}.<br>
 * Note that changes applied using this event are <b>not</b> persistent.<br>
 * This means that if the map is reloaded, all changes will be reverted to their original state.<br>
 * <br>
 * Use this event when you want to temporarily modify the appearance of a tile during runtime,
 * but do not need to keep these modifications once the map is reloaded or reset.
 */
public class ChangeTileEvent extends Event {

    protected int x = -1, y = -1;
    protected List<Tuple3<Byte, Boolean, Integer>> changedTextures = new ArrayList<>();

    private Tile tile;

    /**
     * Constructs a new event with the specified ID.
     */
    public ChangeTileEvent() {
        super("change_tile");
    }

    @Override
    public void load(DataSaveObject dso) {
        x = dso.getInt("x", 0);
        y = dso.getInt("y", 0);
        changedTextures.addAll(
                dso.<DataSaveObject>getList("changes").stream()
                        .map(cDSO -> new Tuple3<>(cDSO.getByte("w", (byte) -1), cDSO.getBool("fl", true), cDSO.getInt("i", 0)))
                        .toList()
        );
    }

    @Override
    public void prepare(World world, GameObject parent) {
        tile = world.getTile(x, y);
    }

    @Override
    public void run(World world) {
        if(tile == null)
            return;
        tile.changeTextures(world, tc -> {
            for (var cT : changedTextures) {
                if(cT.t1() == -1)
                    tc.changeGround(cT.t3(), cT.t2());
                else
                    tc.changeWall(cT.t1(), cT.t3(), cT.t2());
            }
        });
    }

    @Override
    public boolean isFinished(World world) {
        return true;
    }

    @Override
    public boolean isInstant(World world) {
        return true;
    }
}
