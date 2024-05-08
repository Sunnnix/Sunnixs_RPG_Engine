package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.window.Window;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MapView extends JPanel {

    private final de.sunnix.aje.editor.window.Window window;
    @Getter
    private final int mapID;
    @Getter
    private float zoom = 1;
    private float offsetX, offsetY;

    public MapView(Window window, int mapID) {
        super(new BorderLayout());
        this.window = window;
        this.mapID = mapID;
        var ml = genMouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var mapData = window.getSingleton(GameData.class).getMap(mapID);
        if(mapData == null) {
            g.setColor(Color.RED);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 24f));
            var text = "MapData not found!";
            g.drawString(text, getWidth() / 2 - g.getFontMetrics().stringWidth(text) / 2, getHeight() / 2);
            return;
        }

        window.getCurrentMapModule().onDraw((Graphics2D) g, MapView.this, mapData, getWidth(), getHeight(), (int)offsetX, (int)offsetY);
    }

    public void setSelectedTilesetTile(int tileset, int index) {
        window.getSingleton(GameData.class).getMap(mapID).setSelectedTilesetTile(tileset, index);
    }

    public void setZoom(float zoom){
        var tmp = offsetX / this.zoom;
        tmp *= zoom;
        this.offsetX = tmp;
        tmp = offsetY / this.zoom;
        tmp *= zoom;
        this.offsetY = tmp;

        this.zoom = zoom;
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {
            int preX, preY;
            int preTileX, preTileY;
            int button;

            @Override
            public void mousePressed(MouseEvent e) {
                preX = e.getX();
                preY = e.getY();
                var mod = e.getModifiersEx();
                if((mod & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
                    return;
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                preTileX = tilePos[0];
                preTileY = tilePos[1];

                button = e.getButton();

                if(window.getCurrentMapModule().onMousePresses(MapView.this, mapData, button, e.getModifiersEx(), screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                preTileX = tilePos[0];
                preTileY = tilePos[1];

                button = e.getButton();

                if(window.getCurrentMapModule().onMouseReleased(MapView.this, mapData, button, e.getModifiersEx(), screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                window.getCurrentMapModule().onMouseMoved(MapView.this, mapData, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                var x = e.getX();
                var y = e.getY();
                var pX = preX;
                var pY = preY;
                preX = e.getX();
                preY = e.getY();
                var mod = e.getModifiersEx();
                if((mod & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                    offsetX += x - pX;
                    offsetY += y - pY;
                    repaint();
                    return;
                }
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                var sameTile = preTileX == tilePos[0] && preTileY == tilePos[1];
                preTileX = tilePos[0];
                preTileY = tilePos[1];

                if(window.getCurrentMapModule().onMouseDragged(MapView.this, mapData, button, e.getModifiersEx(), screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1], sameTile))
                    repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;

                if((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK){
                    var scrollAmount = 1.15f;
                    var zoom = getZoom();
                    if(e.getWheelRotation() > 0)
                        zoom = Math.round(zoom / scrollAmount * 100) / 100f;
                    else
                        zoom = Math.round(zoom * scrollAmount * 100) / 100f;
                    if(zoom < .25)
                        zoom = .25f;
                    else if(zoom > 15)
                        zoom = 15;
                    else if(zoom - .05 > 1 / scrollAmount && zoom + .05 < 1 * scrollAmount)
                        zoom = 1;
                    setZoom(zoom);
                    repaint();
                    return;
                }

                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                if(window.getCurrentMapModule().omMouseWheelMoved(MapView.this, mapData, e.getModifiersEx(), e.getWheelRotation() > 0, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint();
            }

            private int[] transScreenCoordToMapCoord(int x, int y){
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return new int[2];
                var xStart = getWidth() / 2 - (int)(mapData.getWidth() * Window.TILE_WIDTH / 2 * zoom);
                var yStart = getHeight() / 2 - (int)(mapData.getHeight() * Window.TILE_HEIGHT / 2 * zoom);
                return new int[] {x - xStart - (int)offsetX, y - yStart - (int)offsetY};
            }

            private int[] transMapCoordToTileCoord(int x, int y){
                return new int[]{ (int)Math.floor(x / (float) Window.TILE_WIDTH / zoom), (int)Math.floor(y / (float) Window.TILE_HEIGHT / zoom)};
            }
        };
    }
}
