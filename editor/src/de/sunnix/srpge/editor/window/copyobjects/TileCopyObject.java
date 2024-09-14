package de.sunnix.srpge.editor.window.copyobjects;

import de.sunnix.srpge.editor.data.Tile;

public abstract class TileCopyObject implements ICopyObject{

    protected final Tile[] tiles;

    public TileCopyObject(Tile[] tiles){
        this.tiles = new Tile[tiles.length];
        for(var i = 0; i < tiles.length; i++)
            this.tiles[i] = tiles[i].clone();
    }

}
