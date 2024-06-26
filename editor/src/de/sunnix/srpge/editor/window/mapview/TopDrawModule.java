package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;
import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;

public class TopDrawModule extends MapViewModule {

    private int dragFillRootX = -1;
    private int dragFillRootY = -1;
    private int dragFillStartX = -1;
    private int dragFillStartY = -1;
    private int dragFillWidth = 1;
    private int dragFillHeight = 1;
    private int dragFillLayer = 0;
    private boolean dragFillPrimaryMouse = true;

    public TopDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY) {
        var button = me.getButton();
        var mask = me.getModifiersEx();
        var layer = (mask & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK ? 1 : 0;
        if(button == MouseEvent.BUTTON1){
            var texID = map.getSelectedTilesetTile();
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillPrimaryMouse = true;
                    dragFillLayer = layer;
                    dragFillRootX = tileX;
                    dragFillRootY = tileY;
                    dragFillStartX = tileX;
                    dragFillStartY = tileY;
                    dragFillWidth = 1;
                    dragFillHeight = 1;
                }
                case Window.DRAW_TOOL_FILL -> startFillTiles(map, tileX, tileY, layer, texID[0], texID[1]);
                default -> setTile(map, tileX, tileY, layer, texID[0], texID[1]);
            }
        } else if(button == MouseEvent.BUTTON3){
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillPrimaryMouse = false;
                    dragFillLayer = layer;
                    dragFillRootX = tileX;
                    dragFillRootY = tileY;
                    dragFillStartX = tileX;
                    dragFillStartY = tileY;
                    dragFillWidth = 1;
                    dragFillHeight = 1;
                }
                case Window.DRAW_TOOL_FILL -> startFillTiles(map, tileX, tileY, layer, -1, 0);
                default -> setTile(map, tileX, tileY, layer, -1, 0);
            }
        } else if (button == MouseEvent.BUTTON2) {
            if(tileX < 0 || tileX >= map.getWidth() || tileY < 0 || tileY >= map.getHeight())
                return false;
            var tile = map.getTiles()[tileX + tileY * map.getWidth()];
            var tex = tile.getGroundTex();
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
            var tex = map.getSelectedTilesetTile();
            if(button == MouseEvent.BUTTON1 && dragFillPrimaryMouse) {
                for(var x = dragFillStartX; x < dragFillStartX + dragFillWidth; x++)
                    for(var y = dragFillStartY; y < dragFillStartY + dragFillHeight; y++)
                        setTile(map, x, y, dragFillLayer, tex[0], tex[1], false);
                window.setProjectChanged();
                dragFillRootX = -1;
                dragFillRootY = -1;
                dragFillStartX = -1;
                dragFillStartY = -1;
                dragFillWidth = 1;
                dragFillHeight = 1;
                return true;
            } else if(button == MouseEvent.BUTTON3 && !dragFillPrimaryMouse) {
                for(var x = dragFillStartX; x < dragFillStartX + dragFillWidth; x++)
                    for(var y = dragFillStartY; y < dragFillStartY + dragFillHeight; y++)
                        setTile(map, x, y, dragFillLayer, -1, -1, false);
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
        window.getInfo().setText(getString("view.map.module.top_draw.info", tileX, tileY, mapX, mapY, (int)(view.getZoom() * 100)));
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
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillWidth = Math.max(1, Math.abs(dragFillRootX - tileX) + 1);
                    dragFillHeight = Math.max(1, Math.abs(dragFillRootY - tileY) + 1);
                    dragFillStartX = Math.min(tileX, dragFillRootX);
                    dragFillStartY = Math.min(tileY, dragFillRootY);
                    dragFillLayer = layer;
                }
                case Window.DRAW_TOOL_SINGLE -> setTile(map, tileX, tileY, layer, texID[0], texID[1]);
            }
        } else if(button == MouseEvent.BUTTON3)
            switch (window.getDrawTool()) {
                case Window.DRAW_TOOL_MULTI_RECT -> {
                    dragFillWidth = Math.max(1, Math.abs(dragFillRootX - tileX) + 1);
                    dragFillHeight = Math.max(1, Math.abs(dragFillRootY - tileY) + 1);
                    dragFillStartX = Math.min(tileX, dragFillRootX);
                    dragFillStartY = Math.min(tileY, dragFillRootY);
                    dragFillLayer = layer;
                }
                case Window.DRAW_TOOL_SINGLE -> setTile(map, tileX, tileY, layer, -1, -1);
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

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];
                for (var layer = 0; layer < 2; layer++) {
                    // dragFillTool
                    if(window.getDrawTool() == Window.DRAW_TOOL_MULTI_RECT && dragFillLayer == layer && tX >= dragFillStartX && tX < dragFillStartX + dragFillWidth && tY >= dragFillStartY && tY < dragFillStartY + dragFillHeight){
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

                        var dX = x + tX * TW;
                        var dY = y + tY * TH;
                        var iX = (tex[1] % tsWidth) * TILE_WIDTH;
                        var iY = (tex[1] / tsWidth) * TILE_HEIGHT;
                        g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        continue;
                    }
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
                    var iX = (index % tsWidth) * TILE_WIDTH;
                    var iY = (index / tsWidth) * TILE_HEIGHT;
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

    private void setTile(MapData map, int x, int y, int layer, int tilesetIndex, int index, boolean noticeChanged){
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
        if(noticeChanged)
            window.setProjectChanged();
    }

    private void setTile(MapData map, int x, int y, int layer, int tilesetIndex, int index){
        setTile(map, x, y, layer, tilesetIndex, index, true);
    }

    private void startFillTiles(MapData map, int x, int y, int layer, int tilesetIndex, int index){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        int tTS, tIndex;
        var tiles = map.getTiles();
        var sTile = tiles[x + y * map.getWidth()];
        tTS = sTile.getGroundTex()[layer * 2];
        tIndex = sTile.getGroundTex()[layer * 2 + 1];
        if(tTS == tilesetIndex && tIndex == index)
            return;
        fillTiles(map, x, y, layer, tTS, tIndex, tilesetIndex, index);
        window.setProjectChanged();
    }

    private void fillTiles(MapData map, int x, int y, int layer, int rootTS, int rootIndex, int changeTS, int changeIndex){
        if(x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        var tile = map.getTiles()[x + y * map.getWidth()];
        var tex = tile.getGroundTex();
        if(tex[layer * 2] != rootTS || tex[layer * 2 + 1] != rootIndex)
            return;
        setTile(map, x, y, layer, changeTS, changeIndex, false);

        fillTiles(map, x, y - 1, layer, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x, y + 1, layer, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x - 1, y, layer, rootTS, rootIndex, changeTS, changeIndex);
        fillTiles(map, x + 1, y, layer, rootTS, rootIndex, changeTS, changeIndex);
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
