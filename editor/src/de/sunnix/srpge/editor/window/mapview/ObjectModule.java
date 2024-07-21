package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.ObjectEditDialog;
import de.sunnix.srpge.editor.window.resource.Resources;

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
    public void onDraw(Graphics2D g, MapView view, MapData map, int screenWidth, int screenHeight, int offsetX, int offsetY) {
        var mapWidth = map.getWidth();
        var mapHeight = map.getHeight();
        var TW = (int)(TILE_WIDTH * view.getZoom());
        var TH = (int)(TILE_HEIGHT * view.getZoom());
        var x = screenWidth / 2 - (mapWidth * TW / 2) + offsetX;
        var y = screenHeight / 2 - (mapHeight * TH / 2) + offsetY;

        var tilesets = loadTilesets(map.getTilesets());
        var tiles = map.getTiles();
        // ground tex
        for (var tX = 0; tX < mapWidth; tX++)
            for (var tY = 0; tY < mapHeight; tY++){
                var tile = tiles[tX + tY * mapWidth];

                var floorY = tile.getgroundY();

                var dX = x + tX * TW;
                var dY = y + (tY - floorY) * TH;

                var tex = tile.getGroundTex();
                for(var layer = 0; layer < 2; layer++){
                    var tsID = tex[layer * 2];
                    var texID = tex[layer * 2 + 1];
                    if(tsID < 0 || tsID > tilesets.length || texID < 0)
                        continue;
                    var tileset = tilesets[tsID];
                    var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                    var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;

                    var iX = (texID % tsWidth) * TILE_WIDTH;
                    var iY = (texID / tsWidth) * TILE_HEIGHT;
                    g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                }
                // wall tex
                for(var wall = 0; wall < tile.getWallHeight(); wall++){
                    for(var layer = 0; layer < 2; layer++){
                        tex = tile.getWallTex(wall);
                        var wallTS = tex[layer * 2];
                        var wallIndex = tex[layer * 2 + 1];
                        if(wallTS == -1 || wallIndex == -1)
                            continue;
                        var tileset = tilesets[wallTS];
                        var tsWidth = tileset == null ? 1 : tileset.getWidth() / TILE_WIDTH;
                        var tsHeight = tileset == null ? 1 : tileset.getHeight() / TILE_HEIGHT;
                        dY = y + (tY - wall) * TH;
                        var iX = (wallIndex % tsWidth) * TILE_WIDTH;
                        var iY = (wallIndex / tsWidth) * TILE_HEIGHT;
                        g.drawImage(tileset, dX, dY, dX + TW, dY + TH, iX, iY, iX + TILE_WIDTH, iY + TILE_HEIGHT, null);
                    }
                }
            }

        if(window.isShowGrid()) {
            g.setColor(Color.BLACK);
            for (int i = 0; i < mapWidth; i++)
                for (int j = 0; j < mapHeight; j++)
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

    private BufferedImage[] loadTilesets(String[] tilesets){
        var images = new BufferedImage[tilesets.length];
        var res = window.getSingleton(Resources.class);
        for(var i = 0; i < tilesets.length; i++) {
            var ts = res.tileset_get(tilesets[i]);
            images[i] = ts == null ? null : ts.getImage(window);
        }
        return images;
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
