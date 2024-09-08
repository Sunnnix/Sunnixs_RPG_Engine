package de.sunnix.srpge.editor.window;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.object.ObjectEditDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.FunctionUtils.createMenuItem;

public class ObjectListView extends JScrollPane {

    private final Window window;
    private MapData map;
    private final JList<GameObject> objectList;

    private boolean reload;

    public ObjectListView(Window window) {
        this.window = window;
        setBorder(BorderFactory.createTitledBorder(getString("name.objects")));
        setViewportView(objectList = createObjectList());
        setPreferredSize(new Dimension(0, 0));
    }

    private JList<GameObject> createObjectList(){
        var list = new JList<GameObject>();
        list.setModel(new DefaultListModel<>());
        list.addMouseListener(genListMouseListener(list));
        list.addListSelectionListener(l -> {
            if(reload)
                return;
            if(map != null)
                map.setSelectedObject(objectList.getSelectedIndex());
            window.getMapTabsView().repaint();
        });
        return list;
    }

    private MouseListener genListMouseListener(JList<GameObject> list){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(map == null || !window.isProjectOpen())
                    return;
                var selected = list.getSelectedValue();
                if(e.getButton() == MouseEvent.BUTTON1){
                    if(e.getClickCount() == 2 && selected != null)
                        new ObjectEditDialog(window, map, objectList.getSelectedValue());
                } else if(e.getButton() == MouseEvent.BUTTON3){
                    openPopup(selected, e.getX(), e.getY());
                }
            }
        };
    }

    public void openWorldData(MapData map) {
        this.map = map;
        if(map == null){
            close();
            return;
        }
        setEnabled(true);
        reloadObjectsList();
    }

    private void openPopup(GameObject item, int x, int y){
        if(!isEnabled())
            return;
        var popup = new JPopupMenu();
        popup.add(createMenuItem(getString("name.create"), this::createNewObject));
        if(item != null){
            var label = new JLabel(item.toString());
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            popup.add(label, 0);
            popup.add(new JSeparator(), 1);
            popup.add(createMenuItem(getString("view.map_list.popup.set_tileset"), l -> editObject(item)));
            popup.add(createMenuItem(getString("name.delete"), l -> deleteObject(item)));
        }
        popup.show(objectList, x, y);
    }

    private void createNewObject(ActionEvent e) {
        var object = map.createNewObject(0,0);
        reloadObjectsList();
        window.setProjectChanged();
        window.getMapTabsView().repaint();
        editObject(object);
    }

    private void editObject(GameObject object) {
        new ObjectEditDialog(window, map, object);
    }

    private void deleteObject(GameObject object){
        if(JOptionPane.showConfirmDialog(window, getString("view.map_list.dialog.delete_object.text"), getString("view.map_list.dialog.delete_object.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        map.removeObject(object);
        reloadObjectsList();
        window.setProjectChanged();
        window.getMapTabsView().repaint();
    }

    public void reloadObjectsList(){
        var model = (DefaultListModel<GameObject>) objectList.getModel();
        reload = true;
        model.clear();
        model.addAll(map.getObjects());
        var index = map.getSelectedObject();
        objectList.setSelectedIndex(index);
        objectList.ensureIndexIsVisible(index);
        reload = false;
    }

    public void close() {
        ((DefaultListModel<GameObject>) objectList.getModel()).clear();
        setEnabled(false);
    }
}
