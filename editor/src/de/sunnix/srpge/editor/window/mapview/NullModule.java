package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import java.awt.*;
import java.awt.event.MouseEvent;

public class NullModule extends MapViewModule{

    public NullModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseReleased(MapView view, MapData map, int button, int mask, int mX, int mY, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapView view, MapData map, int mX, int mY, int sX, int sY, int tX, int tY) {
        return false;
    }

    @Override
    public boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        return false;
    }

    @Override
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, float offsetX, float offsetY, long animTime) {

    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData mapData, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

}
