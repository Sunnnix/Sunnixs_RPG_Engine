package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.ObjectEditDialog;
import de.sunnix.srpge.editor.window.object.components.Component;
import de.sunnix.srpge.editor.window.object.components.PhysicComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.StringToHTMLConverter.convertToHTML;
import static de.sunnix.srpge.engine.ecs.components.PhysicComponent.*;

public class EventList extends de.sunnix.srpge.engine.ecs.event.EventList implements Cloneable{

    private static final Map<Byte, RunType> readableRunTypes = new HashMap<>();

    @SafeVarargs
    public static void addRunTypeName(byte type, String name, Class<? extends Component>... requires){
        readableRunTypes.put(type, new RunType(type, name, requires));
    }

    static {
        addRunTypeName(RUN_TYPE_AUTO, "Auto");
        addRunTypeName(RUN_TYPE_PLAYER_CONSULT, "Player consult", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH, "Touch", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_TOP, "Step on", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_BOTTOM, "Touch bottom", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_SOUTH, "Touch south", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_EAST, "Touch east", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_WEST, "Touch west", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_TOUCH_NORTH, "Touch north", PhysicComponent.class);
    }

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
        runType = dso.getByte("type", RUN_TYPE_AUTO);
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
        dso.putByte("type", runType);
        return dso;
    }

    public List<IEvent> getEventsCopy(){
        return events.stream().map(x -> (IEvent) x.clone()).toList();
    }

    public void putEvents(List<IEvent> events) {
        this.events.clear();
        this.events.addAll(events);
    }

    public JPanel genGUI(Window window, MapData map, ObjectEditDialog parent, GameObject object){
        var panel = new JPanel(new BorderLayout());

        var eventPropsPanel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.set(3, 3, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        eventPropsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx++;
        var fieldName = new JTextField(name, 10);
        eventPropsPanel.add(fieldName, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        eventPropsPanel.add(new JLabel("Block type:"), gbc);
        gbc.gridx++;
        var selectBlockType = new JComboBox<>(BlockType.values());
        eventPropsPanel.add(selectBlockType, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        eventPropsPanel.add(new JLabel("Run type:"), gbc);
        gbc.gridx++;
        var selectRunType = new JComboBox<>(readableRunTypes.values().stream().filter(rt -> Arrays.stream(rt.requires).allMatch(object::hasComponent)).toArray(RunType[]::new));
        eventPropsPanel.add(selectRunType, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // actions / listeners
        fieldName.addActionListener(l -> {
            name = fieldName.getText();
            parent.changeTabName(this, name);
            parent.repaint();
        });
        selectBlockType.addActionListener(l -> blockType = (BlockType) selectBlockType.getSelectedItem());
        selectRunType.addActionListener(l -> runType = ((RunType) selectRunType.getSelectedItem()).id);

        // set values
        selectBlockType.setSelectedItem(blockType);
        selectRunType.setSelectedItem(readableRunTypes.get(runType));

        eventPropsPanel.setPreferredSize(new Dimension(0, 150));
        panel.add(eventPropsPanel, BorderLayout.NORTH);

        el = new JList<>(listModel = new DefaultListModel<>());
        reloadEL();
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
                                menuRemoveEvent.addActionListener(e -> {
                                    events.remove(index);
                                    reloadEL();
                                });
                                add(menuRemoveEvent);
                            }
                        }

                    }.show(el, e.getX(), e.getY());
                }
            }

            private void showEventSelection() {
                var event = EventSelectionDialog.show(window, DialogUtils.getWindowForComponent(panel), map, object);
                if(event != null) {
                    events.add(el.getSelectedIndex() + 1, event);
                    reloadEL();
                }
            }

            private void showEditEventDialog(IEvent event){
                if(event.openDialog(window, DialogUtils.getWindowForComponent(panel), window.getSingleton(GameData.class), map, object))
                    el.repaint();
            }

        });

        el.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                var sIndex = el.getSelectedIndex();
                if(sIndex < 0)
                    return;
                if(e.getKeyCode() == KeyEvent.VK_DELETE){
                    if(JOptionPane.showConfirmDialog(panel, "Do you want to delete this event?", "Delete event", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                    events.remove(el.getSelectedIndex());
                    reloadEL();
                } else if(!e.isControlDown())
                    return;
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    if(sIndex == 0)
                        return;
                    var event = events.remove(sIndex--);
                    events.add(sIndex, event);
                    reloadEL();
                    el.setSelectedIndex(sIndex);
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    if(sIndex == events.size() - 1)
                        return;
                    var event = events.remove(sIndex++);
                    events.add(sIndex, event);
                    reloadEL();
                    el.setSelectedIndex(sIndex);
                }
            }
        });

        var scroll = new JScrollPane(el);
        scroll.setPreferredSize(new Dimension(600, 500));
        scroll.setBorder(BorderFactory.createEtchedBorder());

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void reloadEL(){
        listModel.clear();
        listModel.addAll(events);
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

    private record RunType(byte id, String name, Class<? extends Component>[] requires){

        @Override
        public String toString() {
            return name;
        }
    }

}
