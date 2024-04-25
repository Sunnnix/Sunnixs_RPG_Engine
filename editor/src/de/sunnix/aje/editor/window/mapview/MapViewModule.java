package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;

import java.awt.*;

public abstract class MapViewModule {

    protected final Window window;

    public MapViewModule(Window window) {
        this.window = window;
    }

    /**
     * @param map the map of the MapView
     * @param button pressed button
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX map coordinate X (position relative to the map)
     * @param mapY map coordinate Y (position relative to the map)
     * @param tileX map tile coordinate X (tile position relative to the map)
     * @param tileY map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMousePresses(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param map the map of the MapView
     * @param button pressed button
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX map coordinate X (position relative to the map)
     * @param mapY map coordinate Y (position relative to the map)
     * @param tileX map tile coordinate X (tile position relative to the map)
     * @param tileY map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMouseReleased(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param map the map of the MapView
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX map coordinate X (position relative to the map)
     * @param mapY map coordinate Y (position relative to the map)
     * @param tileX map tile coordinate X (tile position relative to the map)
     * @param tileY map tile coordinate Y (tile position relative to the map)
     * @return should repaint component?
     */
    public abstract boolean onMouseMoved(MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);

    /**
     * @param map the map of the MapView
     * @param button pressed button
     * @param screenX screen coordinate X (position from 0 to MapView width)
     * @param screenY screen coordinate Y (position from 0 to MapView height)
     * @param mapX map coordinate X (position relative to the map)
     * @param mapY map coordinate Y (position relative to the map)
     * @param tileX map tile coordinate X (tile position relative to the map)
     * @param tileY map tile coordinate Y (tile position relative to the map)
     * @param sameTile does the map tile coordinates equals the previous coordinates
     * @return should repaint component?
     */
    public abstract boolean onMouseDragged(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile);

    /**
     * @param g            the graphics object of the MapView
     * @param map          the map of the MapView
     * @param screenWidth the screen width of the view
     * @param screenHeight the screen height of the view
     */
    public abstract void onDraw(Graphics2D g, MapData map, int screenWidth, int screenHeight);

    public abstract boolean omMouseWheelMoved(MapData mapData, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY);
}
