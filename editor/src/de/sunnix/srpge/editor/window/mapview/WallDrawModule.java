package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;
import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.srpge.editor.window.undoredo.UndoableDrawEdit;
import de.sunnix.srpge.engine.util.Tuple.*;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

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

    private final List<UndoableDrawEdit.TileRecord> records = new ArrayList<>();

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
                case Window.DRAW_TOOL_FILL -> {
                    startFillTiles(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1]);
                    if(!records.isEmpty())
                        createUndoEdit(view, "Fill Walls");
                }
                default -> setTileWalls(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1], texID[2], texID[3]);
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
                case Window.DRAW_TOOL_FILL -> {
                    startFillTiles(map, tileX, wallDrawLayer, layer, yDiff, -1, 0);
                    if(!records.isEmpty())
                        createUndoEdit(view, "Fill Walls");
                }
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
                if(!records.isEmpty())
                    createUndoEdit(view, "Drag fill Walls");
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
                if(!records.isEmpty())
                    createUndoEdit(view, "Drag fill Walls");
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
                    createUndoEdit(view, "Draw Wall");
                else
                    createUndoEdit(view, "Draw Walls");
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
                case Window.DRAW_TOOL_SINGLE -> setTileWalls(map, tileX, wallDrawLayer, layer, yDiff, texID[0], texID[1], texID[2], texID[3]);
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
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, int offsetX, int offsetY, long animTime) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var TW = (int)(TILE_WIDTH * view.getZoom());
        var TH = (int)(TILE_HEIGHT * view.getZoom());
        var x = screenWidth / 2 - (mapWidth * TW / 2) + offsetX;
        var y = screenHeight / 2 - (mapHeight * TH / 2) + offsetY;

        var minX = -x / TW;
        var minY = -y / TH;
        var maxX = minX + screenWidth / TW + 2;
        var maxY = minY + screenHeight / TH + 2;

        var wallDrawLayer = window.getPropertiesView().getWallDrawLayer();

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        // draw complete with black layer
        g.setColor(new Color(0f, 0f, 0f, .75f));
        for (var tX = Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++)
            for (var tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];

                var floorY = tile.getgroundY();
                var wallHeight = tile.getWallHeight();

                if(tY < minY || tY - Math.max(floorY, wallHeight) > maxY)
                    continue;

                var dX = x + tX * TW;
                var dY = y + (tY - floorY) * TH;

                for (var layer = 0; layer < 2; layer++) {
                    var tex = tile.getGroundTex();
                    var tsID = tex[layer * 2];
                    var texID = tex[layer * 2 + 1];
                    if (tsID < 0 || tsID > tilesets.length || texID < 0)
                        continue;
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
                            continue;
                        var animTex = animation.get(index);
                        iX = animTex % tileset.getWidth() * TILE_WIDTH;
                        iY = animTex / tileset.getWidth() * TILE_HEIGHT;
                    } else {
                        iX = (texID % tsWidth) * TILE_WIDTH;
                        iY = (texID / tsWidth) * TILE_HEIGHT;
                    }
                    g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                g.fillRect(dX, dY, TW, TH);
                // draw walls
                for (var wall = 0; wall < wallHeight; wall++) {
                    for (var layer = 0; layer < 2; layer++) {
                        var tex = tile.getWallTex(wall);
                        var wallTS = tex[layer * 2];
                        var wallIndex = tex[layer * 2 + 1];
                        if (wallTS == -1 || wallIndex == -1)
                            continue;
                        Tileset tileset;
                        BufferedImage image;
                        {
                            var tuple = tilesets[wallTS];
                            tileset = (Tileset) tuple.t1();
                            image = (BufferedImage) tuple.t2();
                        }
                        var tsWidth = tileset == null ? 1 : tileset.getWidth();
                        var tsHeight = tileset == null ? 1 : tileset.getHeight();
                        dY = y + (tY - wall) * TH;
                        var prop = tileset.getProperty(wallIndex);
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
                            var offset = animation.indexOf((short)(x + y * tsWidth));
                            var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                            if(index < 0)
                                continue;
                            var animTex = animation.get(index);
                            iX = animTex % tsWidth * TILE_WIDTH;
                            iY = animTex / tsWidth * TILE_HEIGHT;
                        } else {
                            iX = (wallIndex % tsWidth) * TILE_WIDTH;
                            iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                        }
                        g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        g.fillRect(dX, dY, TW, TH);
                    }
                }
            }

        // draw selected layer
        g.setColor(Color.BLACK);
        for (var tX = Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++) {
            var tile = tiles[tX + wallDrawLayer * mapWidth];

            var floorY = tile.getgroundY();

            var dX = x + tX * TW;
            var dY = y + (wallDrawLayer - floorY) * TH;

            for(var layer = 0; layer < 2; layer++) {
                var tex = tile.getGroundTex();
                var tsID = tex[layer * 2];
                var texID = tex[layer * 2 + 1];
                if (tsID < 0 || tsID > tilesets.length || texID < 0)
                    continue;
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
                        continue;
                    var animTex = animation.get(index);
                    iX = animTex % tileset.getWidth() * TILE_WIDTH;
                    iY = animTex / tileset.getWidth() * TILE_HEIGHT;
                } else {
                    iX = (texID % tsWidth) * TILE_WIDTH;
                    iY = (texID / tsWidth) * TILE_HEIGHT;
                }
                g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
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
                        Tileset tileset;
                        BufferedImage image;
                        {
                            var tuple = tilesets[tex[0]];
                            tileset = (Tileset) tuple.t1();
                            image = (BufferedImage) tuple.t2();
                        }
                        var tsWidth = tileset == null ? 1 : tileset.getWidth();
                        var tsHeight = tileset == null ? 1 : tileset.getHeight();

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
                        g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                        continue;
                    }
                    var tex = tile.getWallTex(wall);
                    var wallTS = tex[layer * 2];
                    var wallIndex = tex[layer * 2 + 1];

                    if(wallTS == -1)
                        continue;

                    Tileset tileset;
                    BufferedImage image;
                    {
                        var tuple = tilesets[wallTS];
                        tileset = (Tileset) tuple.t1();
                        image = (BufferedImage) tuple.t2();
                    }
                    var tsWidth = tileset == null ? 1 : tileset.getWidth();
                    var tsHeight = tileset == null ? 1 : tileset.getHeight();

                    var prop = tileset.getProperty(wallIndex);
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
                        var offset = animation.indexOf((short)(x + y * tsWidth));
                        var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                        if(index < 0)
                            continue;
                        var animTex = animation.get(index);
                        iX = animTex % tsWidth * TILE_WIDTH;
                        iY = animTex / tsWidth * TILE_HEIGHT;
                    } else {
                        iX = (wallIndex % tsWidth) * TILE_WIDTH;
                        iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                    }
                    g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
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
        if(wall < 0 || wall >= wallHeight)
            return;

        records.add(new UndoableDrawEdit.TileRecord(x, y, tile, layer, Arrays.copyOfRange(tile.getWallTex(wall), layer == 0 ? 0 : 2, layer == 0 ? 2 : 4), new int[] { tileset, index }, wall));

        tile.setWallTex(wall, layer, tileset, index);
        if(noticeChanged)
            window.setProjectChanged();
    }

    private void setTileWalls(MapData map, int tX, int tY, int layer, int wall, int tilesetIndex, int index, int width, int height){
        var tileset = window.getSingleton(Resources.class).tileset_get(map.getTilesets()[map.getSelectedTileset()]);
        var mW = map.getWidth();
        for(var x = 0; x < width; x++)
            for(var y = 0; y < height; y++){
                var nTX = tX + x;
                if(nTX >= mW)
                    continue;
                var nextIndex = index + x + y * tileset.getWidth();
                setTileWall(map, nTX, tY, layer, wall - y, tilesetIndex, nextIndex);
            }
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

    private void fillTiles(MapData map, int startX, int startY, int layer, int startWall, int rootTS, int rootIndex, int changeTS, int changeIndex){
        var width = map.getWidth();
        var height = map.getHeight();
        var tiles = map.getTiles();

        var stack = new Stack<int[]>();
        stack.push(new int[]{startX, startY, startWall});

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0];
            int y = pos[1];
            int wall = pos[2];

            if (x < 0 || x >= width || y < 0 || y >= height)
                continue;

            var tile = tiles[x + y * width];
            var wallHeight = tile.getWallHeight();
            if(wall >= wallHeight || wall < 0)
                continue;

            var tex = tile.getWallTex(wall);

            if (tex[layer * 2] != rootTS || tex[layer * 2 + 1] != rootIndex)
                continue;

            setTileWall(map, x, y, layer, wall, changeTS, changeIndex, false);

            stack.push(new int[]{x, y, wall + 1});
            stack.push(new int[]{x, y, wall - 1});
            stack.push(new int[]{x - 1, y, wall});
            stack.push(new int[]{x + 1, y, wall});
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
        window.getUndoManager().addEdit(new UndoableDrawWallEdit(records, presentationName, window, view));
    }

    private static class UndoableDrawWallEdit extends UndoableDrawEdit{

        public UndoableDrawWallEdit(List<TileRecord> records, String presentationName, Window window, MapView view) {
            super(records, presentationName, window, view);
        }

        @Override
        public void undo() throws CannotUndoException {
            for(var record: records)
                record.tile().setWallTex(record.meta(), record.layer(), record.preTexture()[0], record.preTexture()[1]);
            view.repaint();
            window.setProjectChanged();
        }

        @Override
        public void redo() throws CannotRedoException {
            for(var record: records)
                record.tile().setWallTex(record.meta(), record.layer(), record.postTexture()[0], record.postTexture()[1]);
            view.repaint();
            window.setProjectChanged();
        }

    }

}
