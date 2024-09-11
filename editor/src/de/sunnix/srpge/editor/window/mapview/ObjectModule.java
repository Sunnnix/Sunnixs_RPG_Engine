package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.ObjectEditDialog;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;
import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.srpge.engine.util.Tuple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.FunctionUtils.createMenuItem;
import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;

public class ObjectModule extends MapViewModule {

    public ObjectModule(Window window) {
        super(window);
    }

    private int preX, preY;

    @Override
    public boolean onMousePresses(MapView view, MapData map, MouseEvent me, int mapX, int mapY, int tileX, int tileY) {
        var button = me.getButton();
        var TW = (int)(TILE_WIDTH * view.getZoom());
        var TH = (int)(TILE_HEIGHT * view.getZoom());
        float x, y;
        x = ((float)mapX / TW);
        y = ((float)mapY / TH);
        if(button == MouseEvent.BUTTON1) {
            preX = mapX;
            preY = mapY;
            var obj = map.getObjectAt(x, y);
            map.setSelectedObject(obj != null ? obj.ID : -1);
            if(me.getClickCount() == 2)
                if(obj == null) {
                    obj = createNewObject(map, (float) mapX / TW, (float) mapY / TH);
                    map.setSelectedObject(obj.getID());
                } else
                    openObjectEditor(map, obj);
        } else {
            var obj = map.getObjectAt(x, y);
            map.setSelectedObject(obj != null ? obj.ID : -1);
            showPopUp(view, map, obj, me.getX(), me.getY(), (float)mapX / TW, (float)mapY / TH);
        }
        window.getObjectListView().reloadObjectsList();
        updateInfo(view, map, mapX, mapY, tileX, tileY);
        return true;
    }

    @Override
    public boolean onMouseReleased(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    @Override
    public boolean onMouseMoved(MapView view, MapData map, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        updateInfo(view, map, mapX, mapY, tileX, tileY);
        return false;
    }

    @Override
    public boolean onMouseDragged(MapView view, MapData map, int button, int mask, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY, boolean sameTile) {
        if(button == MouseEvent.BUTTON1){
            var obj = map.getObject(map.getSelectedObject());
            if(obj == null)
                return false;
            var TW = (int)(TILE_WIDTH * view.getZoom());
            var TH = (int)(TILE_HEIGHT * view.getZoom());
            float diffX, diffY;
            diffX = ((float)(mapX - preX) / TW);
            diffY = ((float)(mapY - preY) / TH);
            preX = mapX;
            preY = mapY;
            obj.setX(obj.getX() + diffX);
            obj.setZ(obj.getZ() + diffY);
            updateInfo(view, map, mapX, mapY, tileX, tileY);
            window.setProjectChanged();
            return true;
        }
        updateInfo(view, map, mapX, mapY, tileX, tileY);
        return false;
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

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        // ground tex
        for (var tX = Math.max(0, minX); tX < Math.min(mapWidth, maxX); tX++)
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
                    g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
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
                            var offset = animation.indexOf((short)(x + y * tileset.getWidth()));
                            var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                            if(index < 0)
                                continue;
                            var animTex = animation.get(index);
                            iX = animTex % tileset.getWidth() * TILE_WIDTH;
                            iY = animTex / tileset.getWidth() * TILE_HEIGHT;
                        } else {
                            iX = (wallIndex % tsWidth) * TILE_WIDTH;
                            iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                        }
                        g.drawImage(image, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                    }
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            for (var i = Math.max(0, minX); i < Math.min(mapWidth, maxX); i++)
                for (var j = Math.max(0, minY); j < Math.min(mapHeight, maxY); j++)
                    g.drawRect(x + TW * i, y + TH * j, TW, TH);
        }

        map.drawObjects(window, g, view.getZoom(), x, y);
    }

    @Override
    public boolean omMouseWheelMoved(MapView view, MapData map, int mask, boolean scrollIn, int screenX, int screenY, int mapX, int mapY, int tileX, int tileY) {
        return false;
    }

    private void updateInfo(MapView view, MapData map, int mapX, int mapY, int tileX, int tileY){
        var obj = map.getObject(map.getSelectedObject());
        window.getInfo().setText(getString("view.map.module.object.info", tileX, tileY, mapX, mapY, (int)(view.getZoom() * 100), obj == null ? getString("view.map.module.object.no_object") : obj));
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

    private void showPopUp(MapView view, MapData map, GameObject obj, int x, int y, float mapX, float mapY) {
        new JPopupMenu(){
            {
                if(obj != null){
                    var label = new JLabel(obj.toString());
                    label.getInsets().set(3, 5, 3, 5);
                    add(label);
                    add(new JSeparator(JSeparator.HORIZONTAL));
                    add(createMenuItem(getString("name.edit"), l -> {
                        openObjectEditor(map, obj);
                        view.repaint();
                    }));
                    add(createMenuItem(getString("name.remove"), l -> {
                        removeObject(map, obj);
                        map.setSelectedObject(-1);
                        view.repaint();
                        window.setProjectChanged();
                    }));
                }
                add(createMenuItem(getString("view.map.module.object.create_object"), l -> {
                    createNewObject(map, mapX, mapY);
                    view.repaint();
                    window.getObjectListView().reloadObjectsList();
                    window.setProjectChanged();
                }));
            }

            private void removeObject(MapData map, GameObject obj) {
                map.removeObject(obj);
                window.getObjectListView().reloadObjectsList();
            }

        }.show(view, x, y);
    }

    private void openObjectEditor(MapData map, GameObject obj) {
        new ObjectEditDialog(window, map, obj);
    }

    private GameObject createNewObject(MapData map, float x, float y) {
        return map.createNewObject(x, y);
    }

}
