package de.sunnix.srpge.editor.window.undoredo;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Tile;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.mapview.MapView;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableTilePasteEdit extends AbstractUndoableEdit {

    protected final Window window;
    protected final MapView view;
    protected final MapData map;
    protected final Tile[] preTiles, postTiles;
    protected final int x, y, width;

    public UndoableTilePasteEdit(Window window, MapView view, MapData map, Tile[] preTiles, Tile[] postTiles, int x, int y, int width) {
        this.window = window;
        this.view = view;
        this.map = map;
        this.preTiles = preTiles;
        this.postTiles = postTiles;
        this.x = x;
        this.y = y;
        this.width = width;
        window.getUndoManager().addEdit(this);
    }

    @Override
    public String getUndoPresentationName() {
        return "Pasted " + postTiles.length + (postTiles.length > 1 ? " tiles " : " tile");
    }

    @Override
    public String getRedoPresentationName() {
        return "Pasted " + preTiles.length + (preTiles.length > 1 ? " tiles " : " tile");
    }

    @Override
    public boolean canRedo() {
        return true;
    }

    @Override
    public void undo() throws CannotUndoException {
        map.replaceTiles(x, y, width, preTiles);
        view.repaint();
        window.setProjectChanged();
    }

    @Override
    public void redo() throws CannotRedoException {
        map.replaceTiles(x, y, width, postTiles);
        view.repaint();
        window.setProjectChanged();
    }

}
