package de.sunnix.srpge.editor.window.tileset;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class TilesetTabView extends JTabbedPane {

    private final Window window;

    public TilesetTabView(Window window) {
        this.window = window;
        setPreferredSize(new Dimension(400, 0));
    }

    public void reload() {
        removeAll();
        var mapView = window.getMapView();
        if(mapView == null)
            return;
        var map = window.getSingleton(GameData.class).getMap(mapView.getMapID());
        if(map == null)
            return;
        var tilesets = map.getTilesets();
        for (var ts: tilesets) {
            var scroll = new JScrollPane(new TilesetView(window, this, ts));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getHorizontalScrollBar().setUnitIncrement(16);
            addTab(ts, scroll);
        }
        var index = map.getSelectedTileset();
        if(index < getTabCount())
            setSelectedIndex(index);
        var sTile = map.getSelectedTilesetTile();
        setSelectedTile(sTile[0], sTile[1], sTile[2], sTile[3]);
    }

    public void setSelectedTile(int tileset, int index, int width, int height){
        for(var i = 0; i < getTabCount(); i++)
            ((TilesetView)((JScrollPane)getComponentAt(i)).getViewport().getView()).setSelected(i == tileset ? index : -1, width, height);
        repaint();
    }

    public void close() {
        removeAll();
    }
}
