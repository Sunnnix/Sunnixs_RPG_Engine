package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;

import java.awt.*;
import java.awt.image.BufferedImage;

import static de.sunnix.aje.editor.lang.Language.getString;
import static de.sunnix.aje.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.aje.editor.window.Window.TILE_WIDTH;

public class SelectTileModule extends MapViewModule {

    public SelectTileModule(Window window) {
        super(window);
    }

    private int preX, preY;

    @Override
    public boolean onMousePresses(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
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
    public boolean onMouseReleased(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapView view, MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        var sTiles = map.getSelectedTiles();
        window.getInfo().setText(getString("view.map.module.select_tile.info", tileX, tileY, mapX, mapY, sTiles[0], sTiles[1], sTiles[2], sTiles[3], (int)(view.getZoom() * 100)));
        return false;
    }

    @Override
    public boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        var sTiles = map.getSelectedTiles();
        onMouseMoved(view, map, screenX, screenY, mapX, mapY, tileX, tileY);
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
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, int offsetX, int offsetY) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var TW = (int)(TILE_WIDTH * view.getZoom());
        var TH = (int)(TILE_HEIGHT * view.getZoom());
        var x = screenWidth / 2 - (mapWidth * TW / 2) + offsetX;
        var y = screenHeight / 2 - (mapHeight * TH / 2) + offsetY;

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        // ground tex
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++){
                var tile = tiles[tX + tY * mapWidth];

                var floorY = tile.getgroundY();

                var dX = x + tX * TW;
                var dY = y + (tY - floorY) * TH;

                var tex = tile.getGroundTex();
                for(var layer = 0; layer < 2; layer++){
                    var tsID = tex[layer * 2];
                    var texID = tex[layer * 2 + 1];
                    if(tsID < 0 || tsID > tilesets.length || texID < 0)
                        continue;
                    var tileset = tilesets[tsID];
                    var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                    var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                    var iX = (texID % tsWidth) * TILE_WIDTH;
                    var iY = (texID / tsWidth) * TILE_HEIGHT;
                    g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                // wall tex
                for(var wall = 0; wall < tile.getWallHeight(); wall++){
                    for(var layer = 0; layer < 2; layer++){
                        tex = tile.getWallTex(wall);
                        var wallTS = tex[layer * 2];
                        var wallIndex = tex[layer * 2 + 1];
                        if(wallTS == -1 || wallIndex == -1)
                            continue;
                        var tileset = tilesets[wallTS];
                        var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                        var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;
                        dY = y + (tY - wall) * TH;
                        var iX = (wallIndex % tsWidth) * TILE_WIDTH;
                        var iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                        g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                    }
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            for (int i = 0; i < mapWidth; i++)
                for (int j = 0; j < mapHeight; j++)
                    g.drawRect(x + TW * i, y + TH * j, TW, TH);
        }

        var selected = map.getSelectedTiles();
        var sX = selected[0];
        var sY = selected[1];
        var sW = selected[2];
        var sH = selected[3];

        var groundY = tiles[sX + sY * map.getWidth()].getgroundY();

        g.setColor(Color.MAGENTA);
        g.drawLine(x + TW * sX, y + TH * sY, x + TW * sX + TW * sW, y + TH * sY);
        g.drawRect(x + TW * sX, y + TH * (sY - groundY), TW * sW, TH * (sH + groundY));

        g.setColor(Color.YELLOW);
        g.drawRect(x + TW * sX, y + TH * (sY - groundY), TW * sW, TH * sH);
    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData mapData, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
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
