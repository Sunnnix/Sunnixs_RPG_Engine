package de.sunnix.srpge.editor.window;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.resource.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Objects;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.DialogUtils.showMultiInputDialog;
import static de.sunnix.srpge.editor.util.FunctionUtils.createComboBox;
import static de.sunnix.srpge.editor.util.FunctionUtils.createMenuItem;

public class MapListView extends JScrollPane {

    private final Window window;
    private final JList<String> mapList;

    public MapListView(Window window) {
        this.window = window;
        setBorder(BorderFactory.createTitledBorder(getString("name.maps")));
        setViewportView(mapList = createMapList());
        setPreferredSize(new Dimension(200, 200));
    }

    private JList<String> createMapList(){
        var list = new JList<String>();
        list.setModel(new DefaultListModel<>());
        list.addMouseListener(genListMouseListener(list));
        list.setCellRenderer(genCellRenderer());
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
        popup.add(createMenuItem(getString("name.create"), this::createNewMap));
        if(item != null){
            var label = new JLabel(item);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            popup.add(label, 0);
            popup.add(new JSeparator(), 1);
            var setStart = createMenuItem(getString("view.map_list.popup.set_as_start"), l -> window.setStart(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)), 0, 0, 0));
            setStart.setEnabled(window.getStartMap() != Integer.parseInt(item.substring(0, 4)));
            popup.add(setStart);
            popup.add(createMenuItem(getString("view.map_list.popup.set_tileset"), this::setMapTileset));
            popup.add(createMenuItem(getString("view.map_list.popup.set_title"), this::setMapTitle));
            popup.add(createMenuItem(getString("view.map_list.popup.set_size"), this::setMapSize));
            popup.add(createMenuItem(getString("view.map_list.popup.set_bgm"), this::setMapBGM));
            popup.add(createMenuItem(getString("name.delete"), this::deleteMap));
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

    private void setMapTileset(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var tsArray = map.getTilesets();
        String tileset = null;
        if(tsArray.length > 0)
            tileset = tsArray[0];
        var res = window.getSingleton(Resources.class);
        var tilesets = new JComboBox<>(res.tileset_getTilesetnames().toArray(String[]::new));
        tilesets.setPreferredSize(new Dimension(250, tilesets.getPreferredSize().height));
        if(tileset == null)
            tilesets.setSelectedIndex(-1);
        else
            tilesets.setSelectedItem(tileset);

        if(!showMultiInputDialog(window, getString("view.map_list.dialog.set_tileset.title"), getString("view.map_list.dialog.set_tileset.text"), new String[]{ getString("name.tileset") }, new JComponent[]{ tilesets }))
            return;
        var sTileset = (String)tilesets.getSelectedItem();
        if(sTileset == null || sTileset.isEmpty())
            tileset = null;
        else
            tileset = sTileset;
        map.setTilesets(tileset == null ? new String[0] : new String[] { tileset });
        window.reloadTilesetView();
        window.reloadMap();
        window.setProjectChanged();
    }

    private void setMapTitle(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var name = (String) JOptionPane.showInputDialog(window, getString("view.map_list.dialog.change_map_name.title"), getString("view.map_list.dialog.change_map_name.text"), JOptionPane.PLAIN_MESSAGE, null, null, map.getName());
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
        var maxTiles = 62500;
        var width = new JSpinner(new SpinnerNumberModel(map.getWidth(), MapData.MINIMUM_WIDTH,  Math.max(map.getWidth(), maxTiles / map.getHeight()), 1));
        var height = new JSpinner(new SpinnerNumberModel(map.getHeight(), MapData.MINIMUM_HEIGHT, Math.max(map.getHeight(), maxTiles / map.getWidth()), 1));
        width.addChangeListener(l -> {
            var value = ((Number)width.getValue()).intValue();
            var maxH = maxTiles / value;
            ((SpinnerNumberModel)height.getModel()).setMaximum(maxH);
        });
        height.addChangeListener(l -> {
            var value = ((Number)height.getValue()).intValue();
            var maxW = maxTiles / value;
            ((SpinnerNumberModel)width.getModel()).setMaximum(maxW);
        });
        if(!showMultiInputDialog(window, getString("view.map_list.dialog.change_map_size.title"), getString("view.map_list.dialog.change_map_size.text"), new String[]{getString("name.width"), getString("name.height")}, new JComponent[]{ width, height }))
            return;
        if(width.getValue().equals(map.getWidth()) && height.getValue().equals(map.getHeight()))
            return;
        map.setSize((int) width.getValue(), (int) height.getValue());
        window.reloadMap();
        reloadMaps();
        window.setProjectChanged();
    }

    private void setMapBGM(ActionEvent e) {
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        var bgm = map.getBackgroundMusic();
        var res = window.getSingleton(Resources.class);
        var soundCats = createComboBox(getString("name.none"), res.audio.getCategoryNames().toArray(String[]::new));
        soundCats.setPreferredSize(new Dimension(200, soundCats.getPreferredSize().height));

        var sounds = new JComboBox<String>();

        soundCats.addActionListener(l -> {
            sounds.removeAllItems();
            if(soundCats.getSelectedIndex() > 0)
                ((DefaultComboBoxModel<String>)sounds.getModel()).addAll(res.audio.getDataNames((String)soundCats.getSelectedItem()));
        });

        if(bgm != null) {
            var split = bgm.split("/");
            soundCats.setSelectedItem(split[0]);
            sounds.setSelectedItem(split[1]);
        }

        if(!showMultiInputDialog(window, getString("view.map_list.popup.set_bgm"), getString("view.map_list.dialog.set_bgm.text"), new String[]{getString("name.category"), getString("name.audio")}, new JComponent[]{ soundCats, sounds }))
            return;

        String newBGM = null;
        if(soundCats.getSelectedIndex() > 0 && sounds.getSelectedIndex() >= 0)
            newBGM = soundCats.getSelectedItem() + "/" + sounds.getSelectedItem();
        if(Objects.equals(bgm, newBGM))
            return;
        map.setBackgroundMusic(newBGM);
        window.setProjectChanged();
    }

    private void deleteMap(ActionEvent e) {
        if(JOptionPane.showConfirmDialog(window, getString("view.map_list.dialog.delete_map.text"), getString("view.map_list.dialog.delete_map.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        var map = window.getSingleton(GameData.class).getMap(Integer.parseInt(mapList.getSelectedValue().substring(0, 4)));
        if(map == null)
            return;
        window.closeMap(map.getID());
        window.getSingleton(GameData.class).deleteMap(map.getID());
        reloadMaps();
        window.setProjectChanged();
    }

    private ListCellRenderer<Object> genCellRenderer(){
        return new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(Integer.parseInt(((String)value).substring(0, 4)) == window.getStartMap())
                    label.setForeground(Color.GREEN);
                return label;
            }
        };
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
