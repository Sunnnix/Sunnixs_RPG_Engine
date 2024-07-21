package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MapTabsView extends JTabbedPane {

    private final Window window;

    public MapTabsView(Window window) {
        this.window = window;
        setBorder(BorderFactory.createTitledBorder((String)null));
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        addMouseListener(genMouseListener());
        addChangeListener(this::onTabChanged);
    }

    private MouseListener genMouseListener(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3){
                    var index = indexAtLocation(e.getX(), e.getY());
                    if(index >= 0)
                        remove(index);
                }
            }
        };
    }

    public void openMap(int id) {
        var tabName = window.getSingleton(GameData.class).getMapNameOf(id);
        var index = indexOfTab(tabName);
        if(index == -1) {
            addTab(tabName, new MapView(window, id));
            setSelectedIndex(getTabCount() - 1);
        }
        else
            setSelectedIndex(index);
    }

    public void closeMap(int id) {
        var tabName = window.getSingleton(GameData.class).getMapNameOf(id);
        var index = indexOfTab(tabName);
        if(index != -1)
            removeTabAt(index);
    }

    private void onTabChanged(ChangeEvent changeEvent) {
        window.loadMapView(getSelectedIndex() == -1 ? null : (MapView) getSelectedComponent());
        window.getObjectListView().openWorldData(getSelectedIndex() == -1 ? null : window.getSingleton(GameData.class).getMap(((MapView) getSelectedComponent()).getMapID()));
        window.reloadTilesetView();
    }

    public void close() {
        removeAll();
    }

}
