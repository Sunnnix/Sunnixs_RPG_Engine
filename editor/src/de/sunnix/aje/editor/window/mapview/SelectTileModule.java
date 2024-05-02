package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SelectTileModule extends MapViewModule {

    public SelectTileModule(Window window) {
        super(window);
    }

    private int preX, preY;

    @Override
    public boolean onMousePresses(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        if(tileX < 0)
            tileX = 0;
        if(tileY < 0)
            tileY = 0;
        if(tileX >= map.getWidth())
            tileX = map.getWidth() - 1;
        if(tileY >= map.getHeight())
            tileY = map.getHeight() - 1;
        var sTiles = map.getSelectedTiles();
        sTiles[0] = preX = tileX;
        sTiles[1] = preY = tileY;
        sTiles[2] = 1;
        sTiles[3] = 1;
        window.getPropertiesView().loadSelectedTileData();
        return true;
    }

    @Override
    public boolean onMouseReleased(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        var sTiles = map.getSelectedTiles();
        window.getInfo().setText(String.format("Tile(%s, %s) | Mouse: (%s, %s) | selected: (%s, %s, %s, %s)", tileX, tileY, mapX, mapY, sTiles[0], sTiles[1], sTiles[2], sTiles[3]));
        return false;
    }

    @Override
    public boolean onMouseDragged(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        var sTiles = map.getSelectedTiles();
        window.getInfo().setText(String.format("Tile(%s, %s) | Mouse: (%s, %s) | selected: (%s, %s, %s, %s)", tileX, tileY, mapX, mapY, sTiles[0], sTiles[1], sTiles[2], sTiles[3]));
        if(sameTile)
            return false;
        if(tileX < 0)
            tileX = 0;
        if(tileY < 0)
            tileY = 0;
        if(tileX >= map.getWidth())
            tileX = map.getWidth() - 1;
        if(tileY >= map.getHeight())
            tileY = map.getHeight() - 1;
        sTiles[0] = Math.min(preX, tileX);
        sTiles[1] = Math.min(preY, tileY);
        sTiles[2] = Math.max(Math.abs(preX - tileX) + 1, 1);
        sTiles[3] = Math.max(Math.abs(preY - tileY) + 1, 1);
        return true;
    }

    @Override
    public void onDraw(Graphics2D g, MapData map, int screenWidth, int screenHeight) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var x = screenWidth / 2 - mapWidth * 24 / 2;
        var y = screenHeight / 2 - mapHeight * 16 / 2;

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++){
                var tile = tiles[tX + tY * mapWidth];
                var texID = tile.getTexID();
                var tsID = texID[0];
                var index = texID[1];
                if(tsID < 0 || tsID > tilesets.length || index < 0)
                    continue;
                var tileset = tilesets[tsID];
                var tsWidth = tileset == null ? 1 : tileset.getWidth() / 24;
                var tsHeight = tileset == null ? 1 : tileset.getHeight() / 16;

                var floorY = tile.getgroundY();

                var dX = x + tX * 24;
                var dY = y + (tY - floorY) * 16;
                var iX = (index % tsWidth) * 24;
                var iY = (index / tsWidth) * 16;
                g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
                // draw walls
                for(var wall = 0; wall < tile.getWallHeight(); wall++){
                    var wallTS = tile.getWallTileset(wall);
                    var wallIndex = tile.getWallTexIndex(wall);
                    if(wallTS == -1 || wallIndex == -1)
                        continue;
                    tileset = tilesets[tsID];
                    dY = y + (tY - wall) * 16;
                    iX = (wallIndex % tsWidth) * 24;
                    iY = (wallIndex / tsWidth) * 16;
                    g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
                }
            }

        g.setColor(Color.BLACK);
        for (int i = 0; i < mapWidth; i++)
            for (int j = 0; j < mapHeight; j++)
                g.drawRect(x + 24 * i, y + 16 * j, 24, 16);


        var selected = map.getSelectedTiles();
        var sX = selected[0];
        var sY = selected[1];
        var sW = selected[2];
        var sH = selected[3];

        var groundY = tiles[sX + sY * map.getWidth()].getgroundY();

        g.setColor(Color.MAGENTA);
        g.drawLine(x + 24 * sX, y + 16 * sY, x + 24 * sX + 24 * sW, y + 16 * sY);
        g.drawRect(x + 24 * sX, y + 16 * (sY - groundY), 24 * sW, 16 * (sH + groundY));

        g.setColor(Color.YELLOW);
        g.drawRect(x + 24 * sX, y + 16 * (sY - groundY), 24 * sW, 16 * sH);
    }

    @Override
    public boolean omMouseWheelMoved(MapData mapData, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private BufferedImage[] loadTilesets(String[] tilesets){
        var images = new BufferedImage[tilesets.length];
        var res = window.getSingleton(Resources.class);
        for(var i = 0; i < tilesets.length; i++) {
            var ts = res.tileset_get(tilesets[i]);
            images[i] = ts == null ? null : ts.getImage(window);
        }
        return images;
    }
}
