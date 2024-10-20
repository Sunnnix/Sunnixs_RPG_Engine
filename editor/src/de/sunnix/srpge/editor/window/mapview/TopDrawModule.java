package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;
import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.srpge.editor.window.undoredo.UndoableDrawEdit;
import de.sunnix.srpge.engine.util.Tuple.*;
import de.sunnix.srpge.editor.window.undoredo.UndoableDrawEdit.TileRecord;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

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

    private final List<TileRecord> records = new ArrayList<>();

    public TopDrawModule(Window window) {
        super(window);
    }

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY) {
        if(map.getSelectedTileset() == -1)
            return false;
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
                case Window.DRAW_TOOL_FILL -> {
                    startFillTiles(map, tileX, tileY, layer, texID[0], texID[1]);
                    if(!records.isEmpty())
                        createUndoEdit(view, "Fill Tiles");
                }
                default -> setTiles(map, tileX, tileY, layer, texID[0], texID[1], texID[2], texID[3]);
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
                case Window.DRAW_TOOL_FILL -> {
                    startFillTiles(map, tileX, tileY, layer, -1, 0);
                    if(!records.isEmpty())
                        createUndoEdit(view, "Fill Tiles");
                }
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
                if(!records.isEmpty())
                    createUndoEdit(view, "Drag fill Tiles");
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
                        setTile(map, x, y, dragFillLayer, -1, 0, false);
                if(!records.isEmpty())
                    createUndoEdit(view, "Drag fill Tiles");
                window.setProjectChanged();
                dragFillRootX = -1;
                dragFillRootY = -1;
                dragFillStartX = -1;
                dragFillStartY = -1;
                dragFillWidth = 1;
                dragFillHeight = 1;
                return true;
            }
        } else if(window.getDrawTool() == Window.DRAW_TOOL_SINGLE)
            if(!records.isEmpty())
                if(records.size() == 1)
                    createUndoEdit(view, "Draw Tile");
                else
                    createUndoEdit(view, "Draw Tiles");
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
                case Window.DRAW_TOOL_SINGLE -> setTiles(map, tileX, tileY, layer, texID[0], texID[1], texID[2], texID[3]);
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
                case Window.DRAW_TOOL_SINGLE -> setTile(map, tileX, tileY, layer, -1, 0);
            }
        return true;
    }

    @Override
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, float offsetX, float offsetY, long animTime) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var TW = TILE_WIDTH * view.getZoom();
        var TH = TILE_HEIGHT * view.getZoom();
        // shift position by half a tile because (0, 0) is the center of a tile
        var x = screenWidth / 2f - (mapWidth * TW / 2) + offsetX - TW / 2;
        var y = screenHeight / 2f - (mapHeight * TH / 2) + offsetY - TH / 2;

        var minX = -x / TW;
        var minY = Math.floor(-y / TH);
        var maxX = minX + screenWidth / TW + 2;
        var maxY = minY + screenHeight / TH + 2;

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        for (var tX = (int)Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++)
            for (var tY = (int)Math.max(0, minY); tY < Math.min(mapHeight, maxY); tY++) {
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
                        Tileset tileset;
                        BufferedImage image;
                        {
                            var tuple = tilesets[tex[0]];
                            tileset = (Tileset) tuple.t1();
                            image = (BufferedImage) tuple.t2();
                        }
                        var tsWidth = tileset == null ? 1 : tileset.getWidth();
                        var tsHeight = tileset == null ? 1 : tileset.getHeight();

                        var dX = x + tX * TW;
                        var dY = y + tY * TH;
                        var prop = tileset.getProperty(tex[1]);
                        if(prop == null)
                            continue;

                        int iX, iY;
                        if(prop.getAnimationParent() != -1 || prop.getAnimation() != null){
                            TilesetPropertie parent;
                            if(prop.getAnimationParent() != -1) {
                                var parentI = prop.getAnimationParent();
                                parent = tileset.getProperty(parentI % tileset.getWidth(), parentI / tileset.getWidth());
                            } else
                                parent = prop;
                            var animation = parent.getAnimation();
                            var animSpeed = parent.getAnimationTempo();
                            var offset = animation.indexOf((short)(x + y * tileset.getWidth()));
                            var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                            if(index < 0)
                                continue;
                            var animTex = animation.get(index);
                            iX = animTex % tileset.getWidth() * TILE_WIDTH;
                            iY = animTex / tileset.getWidth() * TILE_HEIGHT;
                        } else {
                            iX = (tex[1] % tsWidth) * TILE_WIDTH;
                            iY = (tex[1] / tsWidth) * TILE_HEIGHT;
                        }
                        g.drawImage(image, (int)dX, (int)dY, (int)(dX + TW), (int)(dY + TH), iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        continue;
                    }
                    var tex = tile.getGroundTex();
                    var tsID = tex[layer * 2];
                    var texID = tex[layer * 2 + 1];
                    if (tsID < 0 || tsID > tilesets.length || texID < 0)
                        continue;
                    var dX = x + tX * TW;
                    var dY = y + tY * TH;

                    Tileset tileset;
                    BufferedImage image;
                    {
                        var tuple = tilesets[tsID];
                        tileset = (Tileset) tuple.t1();
                        image = (BufferedImage) tuple.t2();
                    }
                    var tsWidth = tileset == null ? 1 : tileset.getWidth();
                    var tsHeight = tileset == null ? 1 : tileset.getHeight();

                    var prop = tileset.getProperty(texID);
                    if(prop == null)
                        continue;

                    int iX, iY;
                    if(prop.getAnimationParent() != -1 || prop.getAnimation() != null){
                        TilesetPropertie parent;
                        if(prop.getAnimationParent() != -1) {
                            var parentI = prop.getAnimationParent();
                            parent = tileset.getProperty(parentI % tileset.getWidth(), parentI / tileset.getWidth());
                        } else
                            parent = prop;
                        var animation = parent.getAnimation();
                        var animSpeed = parent.getAnimationTempo();
                        var offset = animation.indexOf((short)(x + y * tileset.getWidth()));
                        var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                        if(index < 0)
                            return;
                        var animTex = animation.get(index);
                        iX = animTex % tileset.getWidth() * TILE_WIDTH;
                        iY = animTex / tileset.getWidth() * TILE_HEIGHT;
                    } else {
                        iX = (texID % tsWidth) * TILE_WIDTH;
                        iY = (texID / tsWidth) * TILE_HEIGHT;
                    }

                    g.drawImage(image, (int)dX, (int)dY, (int)(dX + TW), (int)(dY + TH), iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
            for (var tX = (int)Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++)
                for (var tY = (int)Math.max(0, minY); tY < Math.min(mapHeight, maxY); tY++) {
                    var tile = tiles[tX + tY * mapWidth];
                    var groundY = tile.getgroundY();
                    g.drawRect((int)(x + TW * tX), (int)(y + TH * tY), (int)TW, (int)TH);
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

        records.add(new TileRecord(x, y, tile, layer, Arrays.copyOfRange(tile.getGroundTex(), layer == 0 ? 0 : 2, layer == 0 ? 2 : 4), new int[] { tilesetIndex, index }, 0));

        tile.setGroundTex(layer, tilesetIndex, index);
        if(noticeChanged)
            window.setProjectChanged();
    }

    private void setTiles(MapData map, int tX, int tY, int layer, int tilesetIndex, int index, int width, int height){
        var tileset = window.getSingleton(Resources.class).tileset_get(map.getTilesets()[map.getSelectedTileset()]);
        var mW = map.getWidth();
        var mH = map.getHeight();
        for(var x = 0; x < width; x++)
            for(var y = 0; y < height; y++){
                var nTX = tX + x;
                var nTY = tY + y;
                if(nTX >= mW || nTY >= mH)
                    continue;
                var nextIndex = index + x + y * tileset.getWidth();
                setTile(map, nTX, nTY, layer, tilesetIndex, nextIndex);
            }
    }

    private void setTile(MapData map, int x, int y, int layer, int tilesetIndex, int index){
        setTile(map, x, y, layer, tilesetIndex, index, true);
    }

    private void startFillTiles(MapData map, int x, int y, int layer, int tilesetIndex, int index){
        if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight())
            return;
        int tTS, tIndex;
        var tiles = map.getTiles();
        var sTile = tiles[x + y * map.getWidth()];
        tTS = sTile.getGroundTex()[layer * 2];
        tIndex = sTile.getGroundTex()[layer * 2 + 1];
        if (tTS == tilesetIndex && tIndex == index)
            return;
        fillTiles(map, x, y, layer, tTS, tIndex, tilesetIndex, index);
        window.setProjectChanged();
    }

    private void fillTiles(MapData map, int startX, int startY, int layer, int rootTS, int rootIndex, int changeTS, int changeIndex){
        var width = map.getWidth();
        var height = map.getHeight();
        var tiles = map.getTiles();

        var stack = new Stack<int[]>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0];
            int y = pos[1];

            if (x < 0 || x >= width || y < 0 || y >= height)
                continue;

            var tile = tiles[x + y * width];
            var tex = tile.getGroundTex();

            if (tex[layer * 2] != rootTS || tex[layer * 2 + 1] != rootIndex)
                continue;

            setTile(map, x, y, layer, changeTS, changeIndex, false);

            stack.push(new int[]{x, y - 1});
            stack.push(new int[]{x, y + 1});
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x + 1, y});
        }
    }

    private Tuple2[] loadTilesets(String[] tilesetNames){
        var tilesets = new Tuple2[tilesetNames.length];
        var res = window.getSingleton(Resources.class);
        for(var i = 0; i < tilesets.length; i++) {
            var ts = res.tileset_get(tilesetNames[i]);
            var img = ts.getImage(window);
            tilesets[i] = new Tuple2<>(ts, img);
        }
        return tilesets;
    }

    private void createUndoEdit(MapView view, String presentationName){
        window.getUndoManager().addEdit(new UndoableDrawTileEdit(records, presentationName, window, view));
    }

    private static class UndoableDrawTileEdit extends UndoableDrawEdit {

        public UndoableDrawTileEdit(List<TileRecord> records, String presentationName, Window window, MapView view) {
            super(records, presentationName, window, view);
        }

        @Override
        public void undo() throws CannotUndoException {
            for(var record: records)
                record.tile().setGroundTex(record.layer(), record.preTexture()[0], record.preTexture()[1]);
            view.repaint();
            window.setProjectChanged();
        }

        @Override
        public void redo() throws CannotRedoException {
            for(var record: records)
                record.tile().setGroundTex(record.layer(), record.postTexture()[0], record.postTexture()[1]);
            view.repaint();
            window.setProjectChanged();
        }

    }

}
