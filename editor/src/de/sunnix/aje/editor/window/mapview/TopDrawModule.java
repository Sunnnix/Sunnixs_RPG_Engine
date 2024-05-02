package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import de.sunnix.aje.editor.window.resource.Tileset;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class TopDrawModule extends MapViewModule {

    public TopDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            setTile(map, tileX, tileY, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3){
            setTile(map, tileX, tileY, -1, -1);
        } else if (button == MouseEvent.BUTTON2) {
            if(tileX < 0 || tileX >= map.getWidth() || tileY < 0 || tileY >= map.getHeight())
                return false;
            var tile = map.getTiles()[tileX + tileY * map.getWidth()];
            window.setSelectedTile(tile.getTileset(), tile.getTexIndex());
        }
        return true;
    }

    @Override
    public boolean onMouseReleased(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        window.getInfo().setText(String.format("Tile(%s, %s) | Mouse: (%s, %s)", tileX, tileY, mapX, mapY));
        return false;
    }

    @Override
    public boolean onMouseDragged(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        onMouseMoved(map, screenX, screenY, mapX, mapY, tileX, tileY);
        if(sameTile)
            return false;
        if(button == MouseEvent.BUTTON1) {
            var texID = map.getSelectedTilesetTile();
            setTile(map, tileX, tileY, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3)
            setTile(map, tileX, tileY, -1, -1);
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

                var dX = x + tX * 24;
                var dY = y + tY * 16;
                var iX = (index % tsWidth) * 24;
                var iY = (index / tsWidth) * 16;
                g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
            }

        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
        for (int tX = 0; tX < mapWidth; tX++)
            for (int tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];
                var groundY = tile.getgroundY();
                g.drawRect(x + 24 * tX, y + 16 * tY, 24, 16);
                if(groundY > 0) {
                    var text = Integer.toString(groundY);
                    g.drawString(text, x + 24 * tX + 12 - g.getFontMetrics().stringWidth(text) / 2, y + 16 * tY + 16 - 2);
                }
            }
    }

    @Override
    public boolean omMouseWheelMoved(MapData mapData, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private void setTile(MapData map, int x, int y, int tilesetIndex, int index){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        Tileset tileset;
        var mapTilesets = map.getTilesets();
        if(tilesetIndex >= mapTilesets.length || tilesetIndex < 0)
            tileset = null;
        else
            tileset = window.getSingleton(Resources.class).tileset_get(mapTilesets[tilesetIndex]);
        tile.setDataTo(tilesetIndex, index, tileset == null ? null : tileset.getPropertie(index));
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
