package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import de.sunnix.aje.editor.window.resource.Tileset;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.aje.editor.window.Window.TILE_WIDTH;
import static de.sunnix.aje.editor.window.Window.TILE_HEIGHT;

public class TopDrawModule extends MapViewModule {

    public TopDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        var layer = (mask & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ? 1 : 0;
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            setTile(map, tileX, tileY, layer, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3){
            setTile(map, tileX, tileY, layer, -1, 0);
        } else if (button == MouseEvent.BUTTON2) {
            if(tileX < 0 || tileX >= map.getWidth() || tileY < 0 || tileY >= map.getHeight())
                return false;
            var tile = map.getTiles()[tileX + tileY * map.getWidth()];
            var tex = tile.getGroundTex();
            if(layer == 0)
                window.setSelectedTile(tex[0], tex[1]);
            else
                window.setSelectedTile(tex[2], tex[3]);
        }
        return true;
    }

    @Override
    public boolean onMouseReleased(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapView view, MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        window.getInfo().setText(String.format("Tile(%s, %s) | Mouse: (%s, %s) | Zoom: %s%%", tileX, tileY, mapX, mapY, (int)(view.getZoom() * 100)));
        return false;
    }

    @Override
    public boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        onMouseMoved(view, map, screenX, screenY, mapX, mapY, tileX, tileY);
        if(sameTile)
            return false;
        var layer = (mask & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ? 1 : 0;
        if(button == MouseEvent.BUTTON1) {
            var texID = map.getSelectedTilesetTile();
            setTile(map, tileX, tileY, layer, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3)
            setTile(map, tileX, tileY, layer, -1, -1);
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
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];
                for (var layer = 0; layer < 2; layer++) {
                    var texID = tile.getGroundTex();
                    var tsID = texID[layer * 2];
                    var index = texID[layer * 2 + 1];
                    if (tsID < 0 || tsID > tilesets.length || index < 0)
                        continue;
                    var tileset = tilesets[tsID];
                    var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                    var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                    var dX = x + tX * TW;
                    var dY = y + tY * TH;
                    var iX = (index % tsWidth) * 24;
                    var iY = (index / tsWidth) * 16;
                    g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
            for (int tX = 0; tX < mapWidth; tX++)
                for (int tY = 0; tY < mapHeight; tY++) {
                    var tile = tiles[tX + tY * mapWidth];
                    var groundY = tile.getgroundY();
                    g.drawRect(x + TW * tX, y + TH * tY, TW, TH);
                    if (groundY > 0) {
                        var text = Integer.toString(groundY);
                        g.drawString(text, x + TW * tX + (TW / 2) - g.getFontMetrics().stringWidth(text) / 2, y + TH * tY + TH - 2);
                    }
                }
        }
    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData mapData, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private void setTile(MapData map, int x, int y, int layer, int tilesetIndex, int index){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        Tileset tileset;
        var mapTilesets = map.getTilesets();
        if(tilesetIndex >= mapTilesets.length || tilesetIndex < 0)
            tileset = null;
        else
            tileset = window.getSingleton(Resources.class).tileset_get(mapTilesets[tilesetIndex]);
        tile.setDataTo(layer, tilesetIndex, index, tileset == null ? null : tileset.getPropertie(index));
        window.setProjectChanged();
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
