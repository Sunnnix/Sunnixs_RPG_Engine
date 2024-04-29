package de.sunnix.aje.editor.window.mapview;

import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class WallDrawModule extends MapViewModule {

    public WallDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapData map, int button, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();
            var yDiff = wallDrawLayer - tileY;
            if(yDiff < 0)
                return false;
            var wallHeight = map.getTiles()[tileX + wallDrawLayer * map.getWidth()].getWallHeight();
            if(yDiff > wallHeight)
                return false;
            setTileWall(map, tileX, wallDrawLayer, yDiff, texID[0], texID[1]);
        } else if(button == MouseEvent.BUTTON3){
            setTile(map, tileX, tileY, -1, -1);
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

        var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        g.setColor(new Color(0f, 0f, 0f, .75f));
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++){
                var tile = tiles[tX + tY * mapWidth];
                var texID = tile.getTexID();
                var tsID = texID[0];
                var index = texID[1];
                if(tsID < 0 || tsID > tilesets.length || index < 0)
                    continue;
                var tileset = tilesets[tsID];
                var tsWidth = tileset.getWidth() / 24;
                var tsHeight = tileset.getHeight() / 16;

                var floorY = tile.getgroundY();

                var dX = x + tX * 24;
                var dY = y + (tY - floorY) * 16;
                var iX = (index % tsWidth) * 24;
                var iY = (index / tsWidth) * 16;
                g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
                g.fillRect(dX, dY, 24, 16);
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
                    g.fillRect(dX, dY, 24, 16);
                }
                g.fillRect(dX, dY, 24, 16);
            }

        g.setColor(Color.BLACK);
        for (var tX = 0; tX < mapWidth; tX++){
            var tile = tiles[tX + wallDrawLayer * mapWidth];
            var texID = tile.getTexID();
            var tsID = texID[0];
            var index = texID[1];
            if(tsID < 0 || tsID > tilesets.length || index < 0)
                continue;
            var tileset = tilesets[tsID];
            var tsWidth = tileset.getWidth() / 24;
            var tsHeight = tileset.getHeight() / 16;

            var floorY = tile.getgroundY();

            var dX = x + tX * 24;
            var dY = y + (wallDrawLayer - floorY) * 16;
            var iX = (index % tsWidth) * 24;
            var iY = (index / tsWidth) * 16;
            g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);

            for(var wall = 0; wall < tile.getWallHeight(); wall++) {
                var wallTS = tile.getWallTileset(wall);
                var wallIndex = tile.getWallTexIndex(wall);
                dY = y + (wallDrawLayer - wall) * 16;
                iX = (wallIndex % tsWidth) * 24;
                iY = (wallIndex / tsWidth) * 16;
                if(wallTS != -1 && wallIndex != -1) {
                    tileset = tilesets[tsID];
                    g.drawImage(tileset, dX, dY, dX + 24, dY + 16, iX, iY, iX + 24, iY + 16, null);
                }
                g.drawRect(dX, dY, 24, 16);
            }
        }
    }

    @Override
    public boolean omMouseWheelMoved(MapData mapData, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private void setTile(MapData map, int x, int y, int tileset, int index){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        tile.setTexID(tileset, index);
        window.setProjectChanged();
    }

    private void setTileWall(MapData map, int x, int y, int wall, int tileset, int index) {
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        var wallHeight = tile.getWallHeight();
        if(wall >= wallHeight)
            return;
        tile.setWall(wall, tileset, index);
        window.setProjectChanged();
    }

    private BufferedImage[] loadTilesets(String[] tilesets){
        var images = new BufferedImage[tilesets.length];
        for(var i = 0; i < tilesets.length; i++)
            images[i] = window.getSingleton(Resources.class).image_getRaw(tilesets[i]);
        return images;
    }

}
