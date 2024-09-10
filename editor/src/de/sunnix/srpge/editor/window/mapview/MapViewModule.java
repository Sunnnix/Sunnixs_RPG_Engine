package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import java.awt.*;

import java.awt.event.*;

public abstract class MapViewModule {

    protected final Window window;

    public MapViewModule(Window window) {
        this.window = window;
    }

    /**
     * @param view  content of tab
     * @param map   the map of the MapView
     * @param me    the mouse event
     * @param mapX  map coordinate X (position relative to the map)
     * @param mapY  map coordinate Y (position relative to the map)
     * @param tileX map tile coordinate X (tile position relative to the map)
     * @param tileY map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param view    content of tab
     * @param map     the map of the MapView
     * @param button  pressed button
     * @param mask    mask-key like {@link MouseEvent#CTRL_DOWN_MASK CTRL_DOWN_MASK}, {@link MouseEvent#SHIFT_DOWN_MASK SHIFT_DOWN_MASK}, {@link MouseEvent#ALT_DOWN_MASK ALT_DOWN_MASK}
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX    map coordinate X (position relative to the map)
     * @param mapY    map coordinate Y (position relative to the map)
     * @param tileX   map tile coordinate X (tile position relative to the map)
     * @param tileY   map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMouseReleased(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param view    content of tab
     * @param map     the map of the MapView
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX    map coordinate X (position relative to the map)
     * @param mapY    map coordinate Y (position relative to the map)
     * @param tileX   map tile coordinate X (tile position relative to the map)
     * @param tileY   map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMouseMoved(MapView view, MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param view    content of tab
     * @param map      the map of the MapView
     * @param button   pressed button
     * @param mask     mask-key like {@link MouseEvent#CTRL_DOWN_MASK CTRL_DOWN_MASK}, {@link MouseEvent#SHIFT_DOWN_MASK SHIFT_DOWN_MASK}, {@link MouseEvent#ALT_DOWN_MASK ALT_DOWN_MASK}
     * @param screenX  screen coordinate X (position from 0 to MapView width)
     * @param screenY  screen coordinate Y (position from 0 to MapView height)
     * @param mapX     map coordinate X (position relative to the map)
     * @param mapY     map coordinate Y (position relative to the map)
     * @param tileX    map tile coordinate X (tile position relative to the map)
     * @param tileY    map tile coordinate Y (tile position relative to the map)
     * @param sameTile does the map tile coordinates equals the previous coordinates
     * @return should repaint component?
     */
    public abstract boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile);

    /**
     * @param g            the graphics object of the MapView
     * @param view         content of tab
     * @param map          the map of the MapView
     * @param screenWidth  the screen width of the view
     * @param screenHeight the screen height of the view
     * @param offsetX
     * @param offsetY
     * @param animTime timer to calculate animated tiles
     */
    public abstract void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, int offsetX, int offsetY, long animTime);

    /**
     * @param view    content of tab
     * @param map      the map of the MapView
     * @param mask     mask-key like {@link MouseEvent#CTRL_DOWN_MASK CTRL_DOWN_MASK}, {@link MouseEvent#SHIFT_DOWN_MASK SHIFT_DOWN_MASK}, {@link MouseEvent#ALT_DOWN_MASK ALT_DOWN_MASK}
     * @param scrollIn scroll direction
     * @param screenX  screen coordinate X (position from 0 to MapView width)
     * @param screenY  screen coordinate Y (position from 0 to MapView height)
     * @param mapX     map coordinate X (position relative to the map)
     * @param mapY     map coordinate Y (position relative to the map)
     * @param tileX    map tile coordinate X (tile position relative to the map)
     * @param tileY    map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean omMouseWheelMoved(MapView view, MapData map, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

}
