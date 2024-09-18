package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.ecs.Tile;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.graphics.Camera;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.engine.Core.TILE_HEIGHT;
import static de.sunnix.srpge.engine.Core.TILE_WIDTH;
import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;

public class TileAnimationSystem {

    private static final List<Tile> tiles = new ArrayList<>();

    private static long animTime;

    public static void clear(){
        tiles.clear();
    }

    public static void addTile(Tile tile){
        tiles.add(tile);
    }

    public static void update(World world){
        var cSize = new Vector2f(Camera.getSize()).mul(1.5f).div(TILE_WIDTH, TILE_HEIGHT);
        var cPos = new Vector2f(Camera.getPos()).div(TILE_WIDTH, -TILE_HEIGHT).sub(cSize.div(2, new Vector2f()));
        var camera = new Rectangle((int) cPos.x, (int) cPos.y, (int) cSize.x, (int) cSize.y);

        var layer0Update = new ArrayList<Tile>();
        var layer1Update = new ArrayList<Tile>();

        for(var tile: tiles){
            if(!camera.contains(tile.getX(), tile.getY()))
                continue;
            var layers = tile.checkAndUpdateAnimation(animTime);
            if(layers == 0)
                continue;
            if(bitcheck(layers, 0x1))
                layer0Update.add(tile);
            if(bitcheck(layers, 0x10))
                layer1Update.add(tile);
        }

        world.getMap().bindTextures(0);
        for(var tile: layer0Update)
            tile.bufferTextures0();

        world.getMap().bindTextures(1);
        for(var tile: layer1Update)
            tile.bufferTextures1();

        animTime++;
    }

}
