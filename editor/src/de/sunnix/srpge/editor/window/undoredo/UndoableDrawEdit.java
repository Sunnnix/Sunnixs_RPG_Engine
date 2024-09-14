package de.sunnix.srpge.editor.window.undoredo;

import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.mapview.MapView;

import javax.swing.undo.AbstractUndoableEdit;
import java.util.List;

import static de.sunnix.srpge.engine.util.FunctionUtils.arrayReversed;

public class UndoableDrawEdit extends AbstractUndoableEdit {

    protected final Window window;
    protected final MapView view;
    protected final TileRecord[] records;
    protected final String presentationName;

    public UndoableDrawEdit(List<TileRecord> records, String presentationName, Window window, MapView view){
        this.window = window;
        this.view = view;
        this.records = arrayReversed(records.toArray(TileRecord[]::new));
        records.clear();
        this.presentationName = presentationName;
    }

    @Override
    public String getUndoPresentationName() {
        return presentationName;
    }

    @Override
    public String getRedoPresentationName() {
        return presentationName;
    }

    @Override
    public boolean canRedo() {
        return true;
    }

    public record TileRecord(int x, int y, de.sunnix.srpge.editor.data.Tile tile, int layer, int[] preTexture, int[] postTexture, int meta) {}

}
