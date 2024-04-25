package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MapView extends JPanel {

    private final de.sunnix.aje.editor.window.Window window;
    @Getter
    private final int mapID;

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

        window.getCurrentMapModule().onDraw((Graphics2D) g, mapData, getWidth(), getHeight());
    }

    public void setSelectedTilesetTile(int tileset, int index) {
        window.getSingleton(GameData.class).getMap(mapID).setSelectedTilesetTile(tileset, index);
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {
            int preTileX, preTileY;
            int button;

            @Override
            public void mousePressed(MouseEvent e) {
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

                if(window.getCurrentMapModule().onMousePresses(mapData, button, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
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

                if(window.getCurrentMapModule().onMouseReleased(mapData, button, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
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

                window.getCurrentMapModule().onMouseMoved(mapData, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
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

                if(window.getCurrentMapModule().onMouseDragged(mapData, button, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1], sameTile))
                    repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                if(window.getCurrentMapModule().omMouseWheelMoved(mapData, e.getWheelRotation() > 0, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint();
            }

            private int[] transScreenCoordToMapCoord(int x, int y){
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return new int[2];
                var xStart = getWidth() / 2 - mapData.getWidth() * 24 / 2;
                var yStart = getHeight() / 2 - mapData.getHeight() * 16 / 2;
                return new int[] {x - xStart, y - yStart};
            }

            private int[] transMapCoordToTileCoord(int x, int y){
                return new int[]{ (int)Math.floor(x / 24f), (int)Math.floor(y / 16f)};
            }
        };
    }
}
