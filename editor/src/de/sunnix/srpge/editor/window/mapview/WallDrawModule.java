package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;
import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;

public class WallDrawModule extends MapViewModule {

    private int dragFillRootX = -1;
    private int dragFillRootY = -1;
    private int dragFillStartX = -1;
    private int dragFillStartY = -1;
    private int dragFillWidth = 1;
    private int dragFillHeight = 1;
    private int dragFillLayer = 0;
    private boolean dragFillPrimaryMouse = true;

    public WallDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY) {
        var button = me.getButton();
        var mask = me.getModifiersEx();
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
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillPrimaryMouse = true;
                    dragFillLayer = layer;
                    dragFillRootX = tileX;
                    dragFillRootY = yDiff;
                    dragFillStartX = tileX;
                    dragFillStartY = yDiff;
                    dragFillWidth = 1;
                    dragFillHeight = 1;
                }
                case Window.DRAW_TOOL_FILL -> startFillTiles(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1]);
                default -> setTileWall(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1]);
            }
        } else if(button == MouseEvent.BUTTON3)
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillPrimaryMouse = false;
                    dragFillLayer = layer;
                    dragFillRootX = tileX;
                    dragFillRootY = yDiff;
                    dragFillStartX = tileX;
                    dragFillStartY = yDiff;
                    dragFillWidth = 1;
                    dragFillHeight = 1;
                }
                case Window.DRAW_TOOL_FILL -> startFillTiles(map, tileX, wallDrawLayer, layer, yDiff, -1, 0);
                default -> setTileWall(map, tileX, wallDrawLayer, layer, yDiff, -1, 0);
            }
        else if(button == MouseEvent.BUTTON2){
            var tile = map.getTiles()[tileX + wallDrawLayer * map.getWidth()];
            var tex = tile.getWallTex(yDiff);
            if(layer == 0)
                window.setSelectedTile(tex[0], tex[1], 1, 1);
            else
                window.setSelectedTile(tex[2], tex[3], 1, 1);
        }
        return true;
    }

    @Override
    public boolean onMouseReleased(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        if(window.getDrawTool() == Window.DRAW_TOOL_MULTI_RECT){
            if(button == MouseEvent.BUTTON1 && dragFillPrimaryMouse) {
                var tex = map.getSelectedTilesetTile();
                for(var x = Math.max(0, dragFillStartX); x < Math.min(map.getWidth(), dragFillStartX + dragFillWidth); x++)
                    setTileWalls(map, x, window.getPropertiesView().getWallDrawLayer(), dragFillStartY, dragFillHeight, dragFillLayer, tex[0], tex[1]);
                window.setProjectChanged();
                dragFillRootX = -1;
                dragFillRootY = -1;
                dragFillStartX = -1;
                dragFillStartY = -1;
                dragFillWidth = 1;
                dragFillHeight = 1;
                return true;
            } else if(button == MouseEvent.BUTTON3 && !dragFillPrimaryMouse) {
                for(var x = Math.max(0, dragFillStartX); x < Math.min(map.getWidth(), dragFillStartX + dragFillWidth); x++)
                    setTileWalls(map, x, window.getPropertiesView().getWallDrawLayer(), dragFillStartY, dragFillHeight, dragFillLayer, -1, 0);
                window.setProjectChanged();
                dragFillRootX = -1;
                dragFillRootY = -1;
                dragFillStartX = -1;
                dragFillStartY = -1;
                dragFillWidth = 1;
                dragFillHeight = 1;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMoved(MapView view, MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        window.getInfo().setText(getString("view.map.module.wall_draw.info", tileX, tileY, mapX, mapY, (int)(view.getZoom() * 100)));
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
        var wallHeight = tileX < 0 ? 0 : map.getTiles()[tileX + wallDrawLayer * map.getWidth()].getWallHeight();
        if(yDiff > wallHeight)
            return false;
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillWidth = Math.max(1, Math.abs(dragFillRootX - tileX) + 1);
                    dragFillHeight = Math.max(1, Math.abs(dragFillRootY - yDiff) + 1);
                    dragFillStartX = Math.min(tileX, dragFillRootX);
                    dragFillStartY = Math.min(yDiff, dragFillRootY);
                    dragFillLayer = layer;
                }
                case Window.DRAW_TOOL_SINGLE -> setTileWall(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1]);
            }
        } else if(button == MouseEvent.BUTTON3)
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillWidth = Math.max(1, Math.abs(dragFillRootX - tileX) + 1);
                    dragFillHeight = Math.max(1, Math.abs(dragFillRootY - yDiff) + 1);
                    dragFillStartX = Math.min(tileX, dragFillRootX);
                    dragFillStartY = Math.min(yDiff, dragFillRootY);
                    dragFillLayer = layer;
                }
                case Window.DRAW_TOOL_SINGLE -> setTileWall(map, tileX, wallDrawLayer, layer, yDiff, -1, 0);
            }
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
        // draw complete with black layer
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

        // draw selected layer
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
                    dY = y + (wallDrawLayer - wall) * TH;
                    // dragFillTool
                    if(window.getDrawTool() == Window.DRAW_TOOL_MULTI_RECT && dragFillLayer == layer && tX >= dragFillStartX && tX < dragFillStartX + dragFillWidth && wall >= dragFillStartY && wall < dragFillStartY + dragFillHeight){
                        if(!dragFillPrimaryMouse)
                            continue;
                        var tex = map.getSelectedTilesetTile();
                        if(tex[0] == -1)
                            continue;
                        if (tex[0] < 0 || tex[0] > tilesets.length || tex[1] < 0)
                            continue;
                        var tileset = tilesets[tex[0]];
                        var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                        var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                        var iX = (tex[1] % tsWidth) * TILE_WIDTH;
                        var iY = (tex[1] / tsWidth) * TILE_HEIGHT;
                        g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        continue;
                    }
                    var tex = tile.getWallTex(wall);
                    var wallTS = tex[layer * 2];
                    var wallIndex = tex[layer * 2 + 1];

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

    private void setTileWall(MapData map, int x, int y, int layer, int wall, int tileset, int index, boolean noticeChanged) {
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        var wallHeight = tile.getWallHeight();
        if(wall >= wallHeight)
            return;
        tile.setWallTex(wall, layer, tileset, index);
        if(noticeChanged)
            window.setProjectChanged();
    }

    private void setTileWall(MapData map, int x, int y, int layer, int wall, int tileset, int index) {
        setTileWall(map, x, y, layer, wall, tileset, index, true);
    }


    private void setTileWalls(MapData map, int x, int y, int wallStart, int length, int layer, int tileset, int index) {
        for (var i = wallStart; i < wallStart + length; i++) {
            if (wallStart < 0)
                continue;
            setTileWall(map, x, y, layer, i, tileset, index, false);
        }
    }

    private void startFillTiles(MapData map, int x, int y, int layer, int wall, int tilesetIndex, int index){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        int tTS, tIndex;
        var tiles = map.getTiles();
        var sTile = tiles[x + y * map.getWidth()];
        var wallHeight = sTile.getWallHeight();
        if(wall >= wallHeight || wall < 0)
            return;
        var tex = sTile.getWallTex(wall);
        tTS = tex[layer * 2];
        tIndex = tex[layer * 2 + 1];
        if(tTS == tilesetIndex && tIndex == index)
            return;
        fillTiles(map, x, y, layer, wall, tTS, tIndex, tilesetIndex, index);
        window.setProjectChanged();
    }

    private void fillTiles(MapData map, int x, int y, int layer, int wall, int rootTS, int rootIndex, int changeTS, int changeIndex){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        var wallHeight = tile.getWallHeight();
        if(wall >= wallHeight || wall < 0)
            return;
        var tex = tile.getWallTex(wall);
        if(tex[layer * 2] != rootTS || tex[layer * 2 + 1] != rootIndex)
            return;
        setTileWall(map, x, y, layer, wall, changeTS, changeIndex, false);

        fillTiles(map, x, y, layer, wall - 1, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x, y, layer, wall + 1, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x - 1, y, layer, wall, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x + 1, y, layer, wall, rootTS, rootIndex, changeTS, changeIndex);
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
