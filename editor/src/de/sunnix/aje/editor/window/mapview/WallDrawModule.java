package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.aje.editor.window.Window.TILE_WIDTH;
import static de.sunnix.aje.editor.window.Window.TILE_HEIGHT;

public class WallDrawModule extends MapViewModule {

    public WallDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();
        var yDiff = wallDrawLayer - tileY;
        if(yDiff < 0)
            return false;
        var layer = (mask & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ? 1 : 0;
        var wallHeight = map.getTiles()[tileX + wallDrawLayer * map.getWidth()].getWallHeight();
        if(yDiff > wallHeight)
            return false;
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            setTileWall(map, layer, tileX, wallDrawLayer, yDiff, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3)
            setTileWall(map, layer, tileX, wallDrawLayer, yDiff, -1, 0);
        else if(button == MouseEvent.BUTTON2){
            var tile = map.getTiles()[tileX + wallDrawLayer * map.getWidth()];
            var tex = tile.getWallTex(yDiff);
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
        window.getInfo().setText(String.format("Tile(%s, %s) | Mouse: (%s, %s)", tileX, tileY, mapX, mapY));
        return false;
    }

    @Override
    public boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        onMouseMoved(view, map, screenX, screenY, mapX, mapY, tileX, tileY);
        if(sameTile)
            return false;
        var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();
        var yDiff = wallDrawLayer - tileY;
        if(yDiff < 0)
            return false;
        var layer = (mask & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ? 1 : 0;
        var wallHeight = map.getTiles()[tileX + wallDrawLayer * map.getWidth()].getWallHeight();
        if(yDiff > wallHeight)
            return false;
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            setTileWall(map, layer, tileX, wallDrawLayer, yDiff, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3)
            setTileWall(map, layer, tileX, wallDrawLayer, yDiff, -1, 0);
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

        var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        g.setColor(new Color(0f, 0f, 0f, .75f));
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];

                var floorY = tile.getgroundY();

                var dX = x + tX * TW;
                var dY = y + (tY - floorY) * TH;

                for (var layer = 0; layer < 2; layer++) {
                    var texID = tile.getGroundTex();
                    var tsID = texID[layer * 2];
                    var index = texID[layer * 2 + 1];
                    if (tsID < 0 || tsID > tilesets.length || index < 0)
                        continue;
                    var tileset = tilesets[tsID];
                    var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                    var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                    var iX = (index % tsWidth) * TILE_WIDTH;
                    var iY = (index / tsWidth) * TILE_HEIGHT;
                    g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                g.fillRect(dX, dY, TW, TH);
                // draw walls
                for (var wall = 0; wall < tile.getWallHeight(); wall++) {
                    for (var layer = 0; layer < 2; layer++) {
                        var tex = tile.getWallTex(wall);
                        var wallTS = tex[layer * 2];
                        var wallIndex = tex[layer * 2 + 1];
                        if (wallTS == -1 || wallIndex == -1)
                            continue;
                        var tileset = tilesets[wallTS];
                        var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                        var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;
                        dY = y + (tY - wall) * TH;
                        var iX = (wallIndex % tsWidth) * TILE_WIDTH;
                        var iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                        g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        g.fillRect(dX, dY, TW, TH);
                    }
                }
            }

        g.setColor(Color.BLACK);
        for (var tX = 0; tX < mapWidth; tX++){
            var tile = tiles[tX + wallDrawLayer * mapWidth];

            var floorY = tile.getgroundY();

            var dX = x + tX * TW;
            var dY = y + (wallDrawLayer - floorY) * TH;

            for(var layer = 0; layer < 2; layer++) {
                var tex = tile.getGroundTex();
                var tsID = tex[layer * 2];
                var index = tex[layer * 2 + 1];
                if (tsID < 0 || tsID > tilesets.length || index < 0)
                    continue;
                var tileset = tilesets[tsID];
                var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                var iX = (index % tsWidth) * TILE_WIDTH;
                var iY = (index / tsWidth) * TILE_HEIGHT;
                g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
            }

            for(var wall = 0; wall < tile.getWallHeight(); wall++) {
                for(var layer = 0; layer < 2; layer++) {
                    var tex = tile.getWallTex(wall);
                    var wallTS = tex[layer * 2];
                    var wallIndex = tex[layer * 2 + 1];

                    dY = y + (wallDrawLayer - wall) * TH;

                    if(wallTS == -1)
                        continue;

                    var tileset = tilesets[wallTS];
                    var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                    var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                    var iX = (wallIndex % tsWidth) * TILE_WIDTH;
                    var iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                    tileset = tilesets[wallTS];
                    g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                if(window.isShowGrid())
                    g.drawRect(dX, dY, TW, TH);
            }
        }
    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData mapData, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private void setTileWall(MapData map, int layer, int x, int y, int wall, int tileset, int index) {
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        var wallHeight = tile.getWallHeight();
        if(wall >= wallHeight)
            return;
        tile.setWallTex(wall, layer, tileset, index);
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
