package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;

import java.awt.*;

public class NullModule extends MapViewModule{

    public NullModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapData map, int button, int mX, int mY, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseReleased(MapData map, int button, int mX, int mY, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapData map, int mX, int mY, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseDragged(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        return false;
    }

    @Override
    public void onDraw(Graphics2D g, MapData map, int screenWidth, int screenHeight) {

    }

}
