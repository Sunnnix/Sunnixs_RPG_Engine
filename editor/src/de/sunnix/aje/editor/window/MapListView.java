package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.resource.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import static de.sunnix.aje.editor.util.DialogUtils.showMultiInputDialog;
import static de.sunnix.aje.editor.util.FunctionUtils.createMenuItem;

public class MapListView extends JScrollPane {

    private final Window window;
    private final JList<String> mapList;

    public MapListView(Window window) {
        this.window = window;
        setViewportView(mapList = createMapList());
        setPreferredSize(new Dimension(200, 400));
    }

    private JList<String> createMapList(){
        var list = new JList<String>();
        list.setModel(new DefaultListModel<>());
        list.addMouseListener(genListMouseListener(list));
        return list;
    }

    private MouseListener genListMouseListener(JList<String> list){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!window.isProjectOpen())
                    return;
                var selected = list.getSelectedValue();
                if(e.getButton() == MouseEvent.BUTTON1){
                    if(e.getClickCount() <= 1 || selected == null)
                        return;
                    window.openMap(Integer.parseInt(selected.substring(0, 4)));
                } else if(e.getButton() == MouseEvent.BUTTON3){
                    openPopup(selected, e.getX(), e.getY());
                }
            }
        };
    }

    private void openPopup(String item, int x, int y){
        var popup = new JPopupMenu();
        popup.add(createMenuItem("Create", this::createNewMap));
        if(item != null){
            var label = new JLabel(item);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            popup.add(label, 0);
            popup.add(new JSeparator(), 1);
            var setStart = createMenuItem("Set as start", this::setMapAsStartMap);
            setStart.setEnabled(window.getStartMap() != Integer.parseInt(item.substring(0, 4)));
            popup.add(setStart);
            popup.add(createMenuItem("Set tileset", this::setMapTileset));
            popup.add(createMenuItem("Set title", this::setMapTitle));
            popup.add(createMenuItem("Set size", this::setMapSize));
            popup.add(createMenuItem("Delete", this::deleteMap));
        }
        popup.show(mapList, x, y);
    }

    private void createNewMap(ActionEvent e) {
        ((DefaultListModel<String>)mapList.getModel()).clear();
        var data = window.getSingleton(GameData.class);
        var id = data.genNewMap();
        loadMapList();
        mapList.setSelectedValue(data.getMapNameOf(id), true);
        window.setProjectChanged();
    }

    private void setMapAsStartMap(ActionEvent e) {
        window.setStartMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        window.setProjectChanged();
    }

    private void setMapTileset(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var tsArray = map.getTilesets();
        String tileset = null;
        if(tsArray.length > 0)
            tileset = tsArray[0];
        var cat = window.getSingleton(Resources.class).imageResources;
        var comboCat = new JComboBox<>(cat.keySet().toArray(String[]::new));
        comboCat.setPreferredSize(new Dimension(250, comboCat.getPreferredSize().height));
        var comboImages = new JComboBox<String>();
        comboCat.addItemListener(l -> {
            var selected = (String) comboCat.getSelectedItem();
            if(selected == null)
                return;
            comboImages.removeAllItems();
            cat.get(selected).forEach(x -> comboImages.addItem(x.getName()));
        });
        if(tileset == null) {
            comboCat.setSelectedIndex(-1);
            comboImages.setSelectedIndex(-1);
        } else {
            comboCat.setSelectedItem(tileset.substring(0, tileset.indexOf('/')));
            comboImages.setSelectedItem(tileset.substring(tileset.indexOf('/') + 1));
        }

        if(!showMultiInputDialog(window, "Set tileset", "Set the tileset for the map:", new String[]{ "Category:", "Image:" }, new JComponent[]{ comboCat, comboImages }))
            return;
        var sCat = (String)comboCat.getSelectedItem();
        var sImage = (String)comboImages.getSelectedItem();
        if(sCat == null || sCat.isEmpty())
            tileset = null;
        else if(sImage == null || sImage.isEmpty())
            tileset = null;
        else
            tileset = sCat + "/" + sImage;
        map.setTilesets(tileset == null ? new String[0] : new String[] { tileset });
        window.reloadTilesetView();
        window.setProjectChanged();
    }

    private void setMapTitle(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var name = (String) JOptionPane.showInputDialog(window, "Enter new map name", "Change map name", JOptionPane.PLAIN_MESSAGE, null, null, map.getName());
        if(name == null || name.equals(map.getName()))
            return;
        map.setName(name);
        reloadMaps();
        window.setProjectChanged();
    }

    private void setMapSize(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var width = new JSpinner(new SpinnerNumberModel(map.getWidth(), MapData.MINIMUM_WIDTH, 1000, 1));
        var height = new JSpinner(new SpinnerNumberModel(map.getHeight(), MapData.MINIMUM_HEIGHT, 1000, 1));
        if(!showMultiInputDialog(window, "Set map size", "Set new map size:", new String[]{"Width:", "Height:"}, new JComponent[]{ width, height }))
            return;
        if(width.getValue().equals(map.getWidth()) && height.getValue().equals(map.getHeight()))
            return;
        map.setSize((int) width.getValue(), (int) height.getValue());
        window.reloadMap();
        reloadMaps();
        window.setProjectChanged();
    }

    private void deleteMap(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(window, "Do you really want to delete the map?", "Delete map", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        window.closeMap(map.getID());
        window.getSingleton(GameData.class).deleteMap(map.getID());
        reloadMaps();
        window.setProjectChanged();
    }

    private void reloadMaps(){
        var index = mapList.getSelectedIndex();
        loadMapList();
        if(index < 0)
            return;
        mapList.setSelectedIndex(index);
    }

    public void loadMapList(){
        var model = (DefaultListModel<String>)mapList.getModel();
        model.clear();
        Arrays.stream(window.getSingleton(GameData.class).getMapListNames()).forEach(model::addElement);
    }

    public void close() {
        ((DefaultListModel<String>)mapList.getModel()).clear();
        setEnabled(false);
    }

}
