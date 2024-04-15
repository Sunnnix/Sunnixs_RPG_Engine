package de.sunnix.aje.editor.window.tileset;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.window.Window;

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
        for (var ts: tilesets)
            addTab(ts, new JScrollPane(new TilesetView(window, this, ts)));
        var index = map.getSelectedTileset();
        if(index < getTabCount())
            setSelectedIndex(index);
        var sTile = map.getSelectedTilesetTile();
        setSelectedTile(sTile[0], sTile[1]);
    }

    public void setSelectedTile(int tileset, int index){
        for(var i = 0; i < getTabCount(); i++)
            ((TilesetView)((JScrollPane)getComponentAt(i)).getViewport().getView()).setSelected(i == tileset ? index : -1);
        repaint();
    }

    public void close() {
        removeAll();
    }
}
