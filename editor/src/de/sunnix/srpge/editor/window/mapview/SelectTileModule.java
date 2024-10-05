package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.copyobjects.ICopyObject;
import de.sunnix.srpge.editor.window.copyobjects.TileCopyObject;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;
import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.srpge.editor.data.Tile;
import de.sunnix.srpge.editor.window.undoredo.UndoableTilePasteEdit;
import de.sunnix.srpge.engine.util.Tuple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;
import static de.sunnix.srpge.engine.ecs.Tile.*;

public class SelectTileModule extends MapViewModule {

    public SelectTileModule(Window window) {
        super(window);
    }

    private int preX, preY;

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY) {
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
        if(me.getButton() == MouseEvent.BUTTON3){
            new JPopupMenu(){
                {
                    var slopeNone = new JMenuItem("Slope - None");
                    slopeNone.addActionListener(l -> setToSlope(SLOPE_DIRECTION_NONE));
                    var slopeSouth = new JMenuItem("Slope - South");
                    slopeSouth.addActionListener(l -> setToSlope(SLOPE_DIRECTION_SOUTH));
                    var slopeEast = new JMenuItem("Slope - East");
                    slopeEast.addActionListener(l -> setToSlope(SLOPE_DIRECTION_EAST));
                    var slopeWest = new JMenuItem("Slope - West");
                    slopeWest.addActionListener(l -> setToSlope(SLOPE_DIRECTION_WEST));
                    var slopeNorth = new JMenuItem("Slope - North");
                    slopeNorth.addActionListener(l -> setToSlope(SLOPE_DIRECTION_NORTH));
                    add(slopeNone);
                    add(slopeSouth);
                    add(slopeEast);
                    add(slopeWest);
                    add(slopeNorth);
                }

                private void setToSlope(int slope){
                    var sTiles = map.getSelectedTiles();
                    var tiles = map.getTiles();
                    System.out.println(slope);
                    for(var x = sTiles[0]; x < sTiles[2]; x++)
                        for(var z = sTiles[1]; z < sTiles[3]; z++)
                            tiles[x + z * map.getWidth()].setSlopeDirection(slope);
                }

            }.show(view, me.getX(), me.getY());
        }
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
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, float offsetX, float offsetY, long animTime) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var TW = TILE_WIDTH * view.getZoom();
        var TH = TILE_HEIGHT * view.getZoom();
        var x = screenWidth / 2f - (mapWidth * TW / 2) + offsetX;
        var y = screenHeight / 2f - (mapHeight * TH / 2) + offsetY;

        var minX = -x / TW;
        var minY = Math.floor(-y / TH);
        var maxX = minX + screenWidth / TW + 2;
        var maxY = minY + screenHeight / TH + 2;

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        // ground tex
        for (var tX = (int)Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++)
            for (var tY = 0; tY < mapHeight; tY++) {
                var tile = tiles[tX + tY * mapWidth];

                var floorY = tile.getgroundY();
                var wallHeight = tile.getWallHeight();

                if(tY < minY || tY - Math.max(floorY, wallHeight) > maxY)
                    continue;

                var dX = x + tX * TW;
                var dY = y + (tY - floorY) * TH;

                var tex = tile.getGroundTex();
                for(var layer = 0; layer < 2; layer++){
                    var tsID = tex[layer * 2];
                    var texID = tex[layer * 2 + 1];
                    if(tsID < 0 || tsID > tilesets.length || texID < 0)
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

                    g.drawImage(image, (int)dX, (int)dY, (int)(dX + TW), (int)(dY + TH), iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                // wall tex
                for(var wall = 0; wall < wallHeight; wall++){
                    for(var layer = 0; layer < 2; layer++){
                        tex = tile.getWallTex(wall);
                        var wallTS = tex[layer * 2];
                        var wallIndex = tex[layer * 2 + 1];
                        if(wallTS == -1 || wallIndex == -1)
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
                        g.drawImage(image, (int)dX, (int)dY, (int)(dX + TW), (int)(dY + TH), iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                    }
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            for (var i = (int)Math.max(0, minX); i < Math.min(mapWidth, maxX); i++)
                for (var j = (int)Math.max(0, minY); j < Math.min(mapHeight, maxY); j++)
                    g.drawRect((int)(x + TW * i), (int)(y + TH * j), (int)TW, (int)TH);
        }

        var selected = map.getSelectedTiles();
        var sX = selected[0];
        var sY = selected[1];
        var sW = selected[2];
        var sH = selected[3];

        var tile = tiles[sX + sY * map.getWidth()];
        var groundY = tile.getgroundY();
        var wallHeight = tile.getWallHeight();

        g.setColor(Color.MAGENTA);
        g.drawLine((int)(x + TW * sX), (int)(y + TH * sY), (int)(x + TW * sX + TW * sW), (int)(y + TH * sY));
        g.drawRect((int)(x + TW * sX), (int)(y + TH * (sY - wallHeight)), (int)(TW * sW), (int)(TH * (sH + wallHeight)));
        g.drawRect((int)(x + TW * sX), (int)(y + TH * (sY - wallHeight)), (int)(TW * sW), (int)(TH * sH));

        g.setColor(tile.getSlopeDirection() != SLOPE_DIRECTION_NONE ? Color.GREEN : Color.YELLOW);
        g.drawRect((int)(x + TW * sX), (int)(y + TH * (sY - groundY)), (int)(TW * sW), (int)(TH * sH));
    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData mapData, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private Tuple.Tuple2[] loadTilesets(String[] tilesetNames){
        var tilesets = new Tuple.Tuple2[tilesetNames.length];
        var res = window.getSingleton(Resources.class);
        for(var i = 0; i < tilesets.length; i++) {
            var ts = res.tileset_get(tilesetNames[i]);
            var img = ts.getImage(window);
            tilesets[i] = new Tuple.Tuple2<>(ts, img);
        }
        return tilesets;
    }

    @Override
    public ICopyObject onCopy(MapView view, MapData map) {
        var sTiles = map.getSelectedTiles();
        var sX = sTiles[0];
        var sY = sTiles[1];
        var sW = sTiles[2];
        var sH = sTiles[3];
        if(sX < 0 || sX >= map.getWidth() ||
                sY < 0 || sY >= map.getHeight() ||
                sW < 0 || (sX + sW - 1) >= map.getWidth() ||
                sH < 0 || (sY + sH - 1) >= map.getHeight())
            return null;
        var tiles = map.getTiles();
        var tilesToCopy = new Tile[sW * sH];
        for(var x = 0; x < sW; x++)
            for(var y = 0; y < sH; y++)
                tilesToCopy[x + y * sW] = tiles[sX + x + (sY + y) * map.getWidth()];
        return new TileCopyObject(tilesToCopy) {
            @Override
            public void paste() {
                if(!view.hasFocus())
                    return;
                var sTiles = map.getSelectedTiles();
                var cSX = sTiles[0];
                var cSY = sTiles[1];
                var cSW = sTiles[2];
                var cSH = sTiles[3];
                if(cSX < 0 || cSX >= map.getWidth() || cSY < 0 || cSY >= map.getHeight())
                    return;
                var width = Math.max(0, Math.min(sW, map.getWidth() - cSX));
                var height = Math.max(0, Math.min(sH, map.getHeight() - cSY));

                var preTiles = new Tile[width * height];
                var postTiles = new Tile[preTiles.length];

                var mTiles = map.getTiles();
                for(var x = 0; x < width; x++)
                    for(var y = 0; y < height; y++){
                        var tile = tiles[x + y * sW];
                        var tX = cSX + x;
                        var tY = cSY + y;
                        preTiles[x + y * width] = mTiles[tX + tY * map.getWidth()].clone();
                        postTiles[x + y * width] = tile.clone();
                        mTiles[tX + tY * map.getWidth()] = tile.clone();
                    }
                new UndoableTilePasteEdit(window, view, map, preTiles, postTiles, cSX, cSY, width);
                view.repaint();
                window.setProjectChanged();
            }
        };
    }

}
