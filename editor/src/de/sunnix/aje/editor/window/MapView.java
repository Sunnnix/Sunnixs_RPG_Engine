package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.window.resource.Resources;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class MapView extends JPanel {

    private final Window window;
    @Getter
    private final int mapID;

    public MapView(Window window, int mapID) {
        super(new BorderLayout());
        this.window = window;
        this.mapID = mapID;
        var ml = genMouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);
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
        var x = getWidth() / 2 - mapData.getWidth() * 24 / 2;
        var y = getHeight() / 2 - mapData.getHeight() * 16 / 2;

        var tilesets = loadTilesets(mapData.getTilesets());
        var tiles = mapData.getTiles();
        for (var tX = 0; tX < mapData.getWidth(); tX++)
            for (var tY = 0; tY < mapData.getHeight(); tY++){
                var tile = tiles[tX + tY * mapData.getWidth()];
                var texID = tile.getTexID();
                var tsID = texID[0];
                var index = texID[1];
                if(tsID < 0 || tsID > tilesets.length || index < 0)
                    continue;
                var tileset = tilesets[tsID];
                var tsWidth = tileset.getWidth() / 24;
                var tsHeight = tileset.getHeight() / 16;

                var dX = x + tX * 24;
                var dY = y + tY * 16;
                var iX = (index % tsWidth) * 24;
                var iY = (index / tsWidth) * 16;
                g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
            }

        g.setColor(Color.BLACK);
        for (int i = 0; i < mapData.getWidth(); i++)
            for (int j = 0; j < mapData.getHeight(); j++)
                g.drawRect(x + 24 * i, y + 16 * j, 24, 16);
    }

    private BufferedImage[] loadTilesets(String[] tilesets){
        var images = new BufferedImage[tilesets.length];
        for(var i = 0; i < tilesets.length; i++) {
            var tileset = tilesets[i];
            var cat = window.getSingleton(Resources.class).imageResources.get(tileset.substring(0, tileset.indexOf('/')));
            if(cat == null)
                continue;
            var rawImage = FunctionUtils.firstOrNull(cat, x -> x.getName().equals(tileset.substring(tileset.indexOf('/') + 1)));
            if(rawImage == null)
                continue;
            images[i] = rawImage.getImage();
        }
        return images;
    }

    public void setSelectedTilesetTile(int tileset, int index) {
        window.getSingleton(GameData.class).getMap(mapID).setSelectedTilesetTile(tileset, index);
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {
            int preX, preY;
            int preTX, preTY;
            boolean primaryPressed;
            boolean secondaryPressed;

            @Override
            public void mousePressed(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var pos = transScreenCoordToMapCoord(e.getX(), e.getY());
                var tPos = transMapCoordToTileCoord(pos[0], pos[1]);
                preX = pos[0];
                preY = pos[1];
                preTX = tPos[0];
                preTY = tPos[1];
                if(e.getButton() == MouseEvent.BUTTON1){
                    primaryPressed = true;
                    secondaryPressed = false;
                    var texID = mapData.getSelectedTilesetTile();
                    setTile(tPos[0], tPos[1], texID[0], texID[1]);
                } else if(e.getButton() == MouseEvent.BUTTON3){
                    secondaryPressed = true;
                    primaryPressed = false;
                    setTile(tPos[0], tPos[1], -1, -1);
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var pos = transScreenCoordToMapCoord(e.getX(), e.getY());
                var tPos = transMapCoordToTileCoord(pos[0], pos[1]);
                preX = pos[0];
                preY = pos[1];
                if(preTX == tPos[0] && preTY == tPos[1])
                    return;
                preTX = tPos[0];
                preTY = tPos[1];
                window.getInfo().setText(String.format("Tile: (%s, %s)", tPos[0], tPos[1]));
                if(primaryPressed) {
                    var texID = mapData.getSelectedTilesetTile();
                    setTile(tPos[0], tPos[1], texID[0], texID[1]);
                } else if(secondaryPressed)
                    setTile(tPos[0], tPos[1], -1, -1);
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                var pos = transScreenCoordToTileCoord(e.getX(), e.getY());
                window.getInfo().setText(String.format("Tile: (%s, %s)", pos[0], pos[1]));
            }

            private void setTile(int x, int y, int tileset, int index){
                var map = window.getSingleton(GameData.class).getMap(mapID);
                if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
                    return;
                var tile = map.getTiles()[x + y * map.getWidth()];
                tile.setTexID(tileset, index);
                window.setProjectChanged();
            }

            private int[] transScreenCoordToMapCoord(int x, int y){
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return new int[2];
                var xStart = getWidth() / 2 - mapData.getWidth() * 24 / 2;
                var yStart = getHeight() / 2 - mapData.getHeight() * 16 / 2;
                return new int[] {x - xStart, y - yStart};
            }

            private int[] transScreenCoordToTileCoord(int x, int y){
                var pos = transScreenCoordToMapCoord(x, y);
                return transMapCoordToTileCoord(pos[0], pos[1]);
            }

            private int[] transMapCoordToTileCoord(int x, int y){
                return new int[]{ (int)Math.floor(x / 24f), (int)Math.floor(y / 16f)};
            }
        };
    }
}
