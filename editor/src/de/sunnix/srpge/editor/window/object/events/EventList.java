package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.evaluation.EvaluationRegistry;
import de.sunnix.srpge.editor.window.evaluation.ICondition;
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

/**
 * The EventList subclass extends the base EventList of the engine and
 * adds functionality for managing and displaying a list of {@link IEvent}
 * objects in a graphical user interface. This class also supports cloning
 * and handling custom event run types.
 * @see IEvent
 * @see GameObject
 * @see ObjectEditDialog
 */
public class EventList extends de.sunnix.srpge.engine.ecs.event.EventList implements Cloneable{

    /** Map of readable run types for user-friendly display in the GUI. */
    private static final Map<Byte, RunType> readableRunTypes = new HashMap<>();

    /**
     * Registers a new run type with a name and required components.
     *
     * @param type     the run type byte identifier.
     * @param name     the readable name of the run type.
     * @param requires the required components for this run type to display in the GUI.
     */
    @SafeVarargs
    public static void addRunTypeName(byte type, String name, Class<? extends Component>... requires){
        readableRunTypes.put(type, new RunType(type, name, requires));
    }

    static {
        addRunTypeName(RUN_TYPE_AUTO, "Auto");
        addRunTypeName(RUN_TYPE_INIT, "<html><span style='color: #ff8888; font-weight: bold;'>Init</span></html>");
        addRunTypeName(RUN_TYPE_PLAYER_CONSULT, "Player consult", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH, "Touch", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_TOP, "Step on", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_BOTTOM, "Touch bottom", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_SOUTH, "Touch south", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_EAST, "Touch east", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_WEST, "Touch west", PhysicComponent.class);
        addRunTypeName(RUN_TYPE_PLAYER_TOUCH_NORTH, "Touch north", PhysicComponent.class);
    }

    /** List of events managed by this EventList. */
    private List<IEvent> events;
    /**  */
    public List<ICondition> conditions;

    /**
     * Constructor for creating new EventList's.
     */
    public EventList(){
        super(new DataSaveObject());
    }
    /**
     * Constructor for loading an EventList.
     */
    public EventList(DataSaveObject dso) {
        super(dso);
    }

    public DataSaveObject load(DataSaveObject dso) {
        events = new ArrayList<>(dso.<DataSaveObject>getList("events").stream().map(data -> EventRegistry.loadEvent(data.getString("ID", null), data)).toList());
        name = dso.getString("name", null);
        blockType = BlockType.values()[dso.getByte("block", (byte) BlockType.NONE.ordinal())];
        runType = dso.getByte("type", RUN_TYPE_AUTO);
        conditions = new ArrayList<>(dso.<DataSaveObject>getList("conditions").stream().map(cDSO -> EvaluationRegistry.loadCondition(cDSO.getString("id", null), cDSO)).toList());
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
        dso.putList("conditions", conditions.stream().map(c -> c.save(new DataSaveObject())).toList());
        return dso;
    }

    /**
     * Returns a copy of the current event list as a new list of cloned events.
     *
     * @return a list of cloned {@code IEvent} objects.
     */
    public List<IEvent> getEventsCopy(){
        return events.stream().map(x -> (IEvent) x.clone()).toList();
    }

    /**
     * Replaces the current event list with a new list of {@code IEvent} objects.
     *
     * @param events the new list of events to replace the current list.
     */
    public void putEvents(List<IEvent> events) {
        this.events.clear();
        this.events.addAll(events);
    }

    /**
     * Generates a graphical user interface (GUI) component with properties for editing the event list in the {@link ObjectEditDialog}.
     *
     * @param window  the parent window of the editor.
     * @param map     the map data of the current map.
     * @param parent  the parent dialog for object editing.
     * @param object  the game object that owns the event list.
     * @return the generated {@link JPanel} containing the GUI.
     */
    public JPanel genGUI(Window window, MapData map, ObjectEditDialog parent, GameObject object){
        var panel = new JPanel(new BorderLayout());

        // Event properties panel
        var eventPropsPanel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.set(3, 3, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        eventPropsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx++;
        var fieldName = new JTextField(name, 10);
        eventPropsPanel.add(fieldName, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        eventPropsPanel.add(new JLabel("Block type:"), gbc);
        gbc.gridx++;

        // Block type dropdown
        var selectBlockType = new JComboBox<>(BlockType.values());
        eventPropsPanel.add(selectBlockType, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        eventPropsPanel.add(new JLabel("Run type:"), gbc);
        gbc.gridx++;

        // Run type dropdown
        var selectRunType = new JComboBox<>(readableRunTypes.values().stream().filter(rt -> Arrays.stream(rt.requires).allMatch(object::hasComponent)).toArray(RunType[]::new));
        eventPropsPanel.add(selectRunType, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // listeners
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

        var tmpPanel = new JPanel(new BorderLayout(5, 5));
        tmpPanel.add(eventPropsPanel, BorderLayout.WEST);
        tmpPanel.add(genConditions(window, map, object), BorderLayout.CENTER);
        tmpPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        panel.add(tmpPanel, BorderLayout.NORTH);

        var elScroll = genSmallGUI(window, map, panel, object);
        elScroll.setPreferredSize(new Dimension(600, 500));
        panel.add(elScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel genConditions(Window window, MapData map, GameObject object){
        var conditionsPanel = new JPanel(new BorderLayout());
        conditionsPanel.setBorder(BorderFactory.createTitledBorder("Conditions"));
        var conditionModel = new DefaultListModel<ICondition>();
        conditionModel.addAll(conditions);
        var conditionsList = new JList<>(conditionModel);
        var cRenderer = conditionsList.getCellRenderer();
        conditionsList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            var comp = (JLabel) cRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            comp.setText(value.getString(window, map, object));
            if(index % 2 == 0)
                comp.setBackground(comp.getBackground().brighter());
            return comp;
        });
        conditionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    new JPopupMenu() {
                        {
                            var index = conditionsList.getSelectedIndex();
                            var createConditionMenu = new JMenuItem("Create condition");
                            createConditionMenu.addActionListener(a -> {
                                var newCondition = createCondition(window, map, object, conditionsList);
                                if(newCondition != null) {
                                    conditions.add(newCondition);
                                    conditionModel.addElement(newCondition);
                                }
                            });
                            add(createConditionMenu);
                            if (index >= 0) {
                                var editConditionMenu = new JMenuItem("Edit condition");
                                editConditionMenu.addActionListener(l -> {
                                    if(editCondition(window, map, object, conditionsList, conditions.get(index)))
                                        conditionsList.repaint();
                                });
                                add(editConditionMenu);
                                var removeConditionMenu = new JMenuItem("Remove condition");
                                removeConditionMenu.addActionListener(l -> {
                                    conditions.remove(index);
                                    conditionModel.removeElementAt(index);
                                });
                                add(removeConditionMenu);
                            }
                        }
                    }.show(conditionsList, e.getX(), e.getY());
                } else if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
                    var index = conditionsList.getSelectedIndex();
                    if(index == -1){
                        var newCondition = createCondition(window, map, object, conditionsList);
                        if(newCondition != null) {
                            conditions.add(newCondition);
                            conditionModel.addElement(newCondition);
                        }
                    }
                    else if(editCondition(window, map, object, conditionsList, conditions.get(index)))
                        conditionsList.repaint();
                }
            }
        });
        var scroll = new JScrollPane(conditionsList);
        scroll.setPreferredSize(new Dimension(0, 0));
        conditionsPanel.add(scroll);
        return conditionsPanel;
    }

    private ICondition createCondition(Window window, MapData map, GameObject object, JComponent parent){
        var condition = EvaluationRegistry.showConditionCreateDialog(parent);
        if(condition != null && editCondition(window, map, object, parent, condition))
            return condition;
        else
            return null;
    }

    /**
     * Shows the GUI for editing an condition.
     * @return if the edit was successfully otherwise canceled.
     */
    private boolean editCondition(Window window, MapData map, GameObject object, JComponent parent, ICondition condition){
        var dialog = new JDialog(DialogUtils.getWindowForComponent(parent), "Edit condition", Dialog.ModalityType.APPLICATION_MODAL){
            boolean successfully;
            {
                ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setLayout(new BorderLayout());
                var content = new JPanel();
                var onApply = condition.getEditGUI(window, map, object, content);
                add(content, BorderLayout.CENTER);

                var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                var applyBtn = new JButton(getString("button.apply"));
                applyBtn.addActionListener(l -> {
                    if(onApply != null)
                        onApply.run();
                    successfully = true;
                    dispose();
                });
                buttons.add(applyBtn);
                var cancelBtn = new JButton(getString("button.cancel"));
                cancelBtn.addActionListener(l -> dispose());
                buttons.add(cancelBtn);
                add(buttons, BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(parent);
                setVisible(true);
            }
        };
        return dialog.successfully;
    }

    /**
     * Generates an un-sized graphical user interface (GUI) component without properties for editing the event list in any Component.
     *
     * @param window  the parent window of the editor.
     * @param map     the map data of the current map.
     * @param parent  the parent dialog for object editing.
     * @param object  the game object that owns the event list.
     * @return the generated {@link JScrollPane} containing the GUI.
     */
    public JScrollPane genSmallGUI(Window window, MapData map, JComponent parent, GameObject object) {
        var el = new JList<IEvent>(new DefaultListModel<>()){
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                if(enabled)
                    setBackground(UIManager.getColor("List.background"));
                else
                    setBackground(UIManager.getColor("List.background").darker());
            }
        };
        reloadEL(el);
        el.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> cellRenderer(window, map, list, value, index, isSelected, cellHasFocus));

        el.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(!el.isEnabled())
                    return;
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
                                    reloadEL(el);
                                });
                                add(menuRemoveEvent);
                            }
                        }

                    }.show(el, e.getX(), e.getY());
                }
            }

            private void showEventSelection() {
                var event = EventSelectionDialog.show(window, DialogUtils.getWindowForComponent(parent), map, object);
                if(event != null) {
                    events.add(el.getSelectedIndex() + 1, event);
                    reloadEL(el);
                }
            }

            private void showEditEventDialog(IEvent event){
                if(event.openDialog(window, DialogUtils.getWindowForComponent(parent), window.getSingleton(GameData.class), map, object))
                    el.repaint();
            }

        });

        el.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(!el.isEnabled())
                    return;
                var sIndex = el.getSelectedIndex();
                if(sIndex < 0)
                    return;
                if(e.getKeyCode() == KeyEvent.VK_DELETE){
                    if(JOptionPane.showConfirmDialog(parent, "Do you want to delete this event?", "Delete event", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                    events.remove(el.getSelectedIndex());
                    reloadEL(el);
                } else if(!e.isControlDown())
                    return;
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    if(sIndex == 0)
                        return;
                    var event = events.remove(sIndex--);
                    events.add(sIndex, event);
                    reloadEL(el);
                    el.setSelectedIndex(sIndex);
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    if(sIndex == events.size() - 1)
                        return;
                    var event = events.remove(sIndex++);
                    events.add(sIndex, event);
                    reloadEL(el);
                    el.setSelectedIndex(sIndex);
                }
            }
        });

        var scroll = new JScrollPane(el);
        scroll.setBorder(BorderFactory.createEtchedBorder());
        return scroll;
    }

    /**
     * Reloads the event list in the GUI.
     */
    private void reloadEL(JList<IEvent> list){
        var model = (DefaultListModel<IEvent>) list.getModel();
        model.clear();
        model.addAll(events);
    }

    private final Color panleBG = UIManager.getColor("Panel.background");
    private final Color panleBG_B = panleBG.brighter();

    /**
     * Creates a cell renderer for displaying the events in the list
     */
    private JComponent cellRenderer(Window window, MapData map, JList<? extends IEvent> jList, IEvent event, int index, boolean selected, boolean b) {
        var label = new JLabel(convertToHTML(event.getString(window, map)));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,5));
        label.setPreferredSize(new Dimension(label.getPreferredSize().width, 20));

        label.setOpaque(true);
        if(selected) {
            label.setBackground(UIManager.getColor("List.selectionBackground"));
            label.setForeground(UIManager.getColor("List.selectionForeground"));
        } else if(index % 2 == 0)
            if(jList.isEnabled())
                label.setBackground(panleBG_B);
            else
                label.setBackground(panleBG_B.darker());
        else
            if(jList.isEnabled())
                label.setBackground(panleBG);
            else
                label.setBackground(panleBG.darker());
        return label;
    }

    @Override
    public EventList clone() {
        try {
            var clone = (EventList) super.clone();
            clone.events = new ArrayList<>(this.events.stream().map(e -> (IEvent) e.clone()).toList());
            clone.conditions = new ArrayList<>(this.conditions.stream().map(c -> (ICondition) c.clone()).toList());
            return clone;
        } catch (CloneNotSupportedException e){
            // should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility class for storing information about event run types.
     */
    private record RunType(byte id, String name, Class<? extends Component>[] requires){

        @Override
        public String toString() {
            return name;
        }
    }

}
