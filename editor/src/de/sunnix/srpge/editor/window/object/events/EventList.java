package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.StringToHTMLConverter.convertToHTML;

public class EventList extends de.sunnix.srpge.engine.ecs.event.EventList implements Cloneable{

    @Getter
    private List<IEvent> events;

    private JList<IEvent> el;
    private DefaultListModel<IEvent> listModel;

    public EventList(){
        super(new DataSaveObject());
    }

    public EventList(DataSaveObject dso) {
        super(dso);
    }

    public DataSaveObject load(DataSaveObject dso) {
        events = new ArrayList<>(dso.<DataSaveObject>getList("events").stream().map(data -> EventRegistry.loadEvent(data.getString("ID", null), data)).toList());
        name = dso.getString("name", null);
        blockType = BlockType.values()[dso.getByte("block", (byte) BlockType.NONE.ordinal())];
        runType = dso.getString("type", "auto");
        return dso;
    }

    public DataSaveObject save(DataSaveObject dso) {
        dso.putList("events", events.stream().map(e -> {
            var eDSO = e.save(new DataSaveObject());
            eDSO.putString("ID", e.getID());
            return eDSO;
        }).toList());
        dso.putString("name", name);
        dso.putByte("block", (byte) blockType.ordinal());
        dso.putString("type", runType);
        return dso;
    }

    public List<IEvent> getEventsCopy(){
        return events.stream().map(x -> (IEvent) x.clone()).toList();
    }

    public void putEvents(List<IEvent> events) {
        this.events.clear();
        this.events.addAll(events);
    }

    public JPanel genGUI(Window window, MapData map, GameObject object){
        var panel = new JPanel(new BorderLayout());

        el = new JList<>(listModel = new DefaultListModel<>());
        listModel.addAll(events);
        el.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> cellRenderer(window, map, list, value, index, isSelected, cellHasFocus));

        el.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    showEventSelection();
                } else if (e.getButton() == MouseEvent.BUTTON3){
                    new JPopupMenu(){
                        {
                            var menuAdd = new JMenuItem(getString("dialog_object.add_event"));
                            menuAdd.addActionListener(e -> showEventSelection());
                            add(menuAdd);

                            var index = el.getSelectedIndex();
                            if(index > -1){
                                var menuEditEvent = new JMenuItem(getString("dialog_object.edit_event"));
                                menuEditEvent.addActionListener(e -> showEditEventDialog(el.getSelectedValue()));
                                add(menuEditEvent);
                                var menuRemoveEvent = new JMenuItem(getString("dialog_object.remove_event"));
                                menuRemoveEvent.addActionListener(e -> listModel.removeElementAt(index));
                                add(menuRemoveEvent);
                            }
                        }

                    }.show(el, e.getX(), e.getY());
                }
            }

            private void showEventSelection() {
                var event = EventSelectionDialog.show(window, DialogUtils.getWindowForComponent(panel), map, object);
                if(event != null)
                    listModel.add(el.getSelectedIndex() + 1, event);
            }

            private void showEditEventDialog(IEvent event){
                if(event.openDialog(window, DialogUtils.getWindowForComponent(panel), window.getSingleton(GameData.class), map, object))
                    el.repaint();
            }

        });

        var scroll = new JScrollPane(el);
        scroll.setPreferredSize(new Dimension(600, 800));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private final Color panleBG = UIManager.getColor("Panel.background");
    private final Color panleBG_B = panleBG.brighter();

    private JComponent cellRenderer(Window window, MapData map, JList<? extends IEvent> jList, IEvent event, int index, boolean selected, boolean b) {
        var label = new JLabel(convertToHTML(event.getString(window, map)));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,5));
        label.setPreferredSize(new Dimension(label.getPreferredSize().width, 20));

        label.setOpaque(true);
        if(selected) {
            label.setBackground(UIManager.getColor("List.selectionBackground"));
            label.setForeground(UIManager.getColor("List.selectionForeground"));
        } else if(index % 2 == 0)
            label.setBackground(panleBG_B);
        else
            label.setBackground(panleBG);
        return label;
    }

    @Override
    public EventList clone() {
        try {
            var clone = (EventList) super.clone();
            clone.events = new ArrayList<>(this.events.stream().map(e -> (IEvent) e.clone()).toList());
            return clone;
        } catch (CloneNotSupportedException e){
            // should never happen
            throw new RuntimeException(e);
        }
    }


}
