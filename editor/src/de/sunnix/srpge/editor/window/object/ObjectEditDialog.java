package de.sunnix.srpge.editor.window.object;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.components.ComponentCreateDialog;
import de.sunnix.srpge.editor.window.object.events.IEvent;
import de.sunnix.srpge.editor.window.object.events.EventSelectionDialog;
import de.sunnix.srpge.editor.window.object.components.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.StringToHTMLConverter.convertToHTML;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

public class ObjectEditDialog extends JDialog {


    private final Window window;
    private final MapData map;
    private final GameObject object;

    private JTextField name;
    private JSpinner x, y, z;
    private JList<IEvent> events;
    private DefaultListModel<IEvent> listModel;

    private List<Component> componentList;
    private JPanel componentsView;

    private Map<JComponent, Runnable> loopFunctions = new HashMap<>();

    public ObjectEditDialog(Window window, MapData map, GameObject obj) {
        super(window, WINDOW_NAME + " - " + getString("dialog_object.title"), true);
        this.window = window;
        this.map = map;
        this.object = obj;

        setLayout(new BorderLayout(5, 5));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5 ,5 ,5 , 5));

        add(setupProperties(), BorderLayout.EAST);
        add(setupEventList(), BorderLayout.CENTER);
        add(setupButtons(), BorderLayout.SOUTH);

        addWindowListener(createWindowListener());

        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
    }

    private JPanel setupProperties() {
        var mainPanel = new JPanel(new BorderLayout());

        mainPanel.setPreferredSize(new Dimension(260, 0));

        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.set(0, 7, 5, 7);

        panel.add(new JLabel(getString("ID")), gbc);
        gbc.gridx++;

        var id = new JTextField(10);
        id.setText(Long.toString(object.ID));
        id.setEditable(false);
        panel.add(id, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        panel.add(new JLabel(getString("name.name")), gbc);
        gbc.gridx++;

        name = new JTextField(object.getName(), 10);
        panel.add(name, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // Position
        panel.add(new JLabel("X"), gbc);
        gbc.gridx++;

        x = new JSpinner(new SpinnerNumberModel(object.getX(), -10000, 10000, .1));
        panel.add(x, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        panel.add(new JLabel("Y"), gbc);
        gbc.gridx++;

        y = new JSpinner(new SpinnerNumberModel(object.getY(), 0, 10000, .1));
        panel.add(y, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        panel.add(new JLabel("Z"), gbc);
        gbc.gridx++;

        z = new JSpinner(new SpinnerNumberModel(object.getZ(), -10000, 10000, .1));
        panel.add(z, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        mainPanel.add(panel, BorderLayout.NORTH);

        mainPanel.add(createComponentPanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createComponentPanel(){
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        componentList = new ArrayList<>(object.getComponents().stream().map(Component::clone).toList());
        componentsView = panel;

        var addbtn = new JButton("+ Add Component");
        addbtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, addbtn.getMinimumSize().height));
        addbtn.addActionListener(a -> {
            var component = ComponentCreateDialog.show(this, object);
            if(component != null) {
                componentList.add(component);
                loadComponentsView();
            }
        });
        addbtn.setAlignmentX(JButton.CENTER_ALIGNMENT);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(addbtn);

        var pane = new JScrollPane(panel);
        loadComponentsView();
        return pane;
    }

    private void loadComponentsView(){
        while(componentsView.getComponents().length > 2)
            componentsView.remove(0);
        var comps = componentList;
        for(var i = 0; i < comps.size(); i++){
            var component = comps.get(i);
            var panel = new JPanel();
            panel.setBorder(BorderFactory.createTitledBorder(component.genName()));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            var loop = component.createView(window, object, panel);
            if(loop != null)
                loopFunctions.put(panel, loop);
            componentsView.add(panel, i);
        }
        componentsView.revalidate();
        componentsView.repaint();
    }

    private JPanel setupEventList() {
        var panel = new JPanel(new BorderLayout());

        events = new JList<>(listModel = new DefaultListModel<>());
        object.getEventList().getEventsCopy().forEach(listModel::addElement);
        events.setCellRenderer(this::cellRenderer);

        events.addMouseListener(new MouseAdapter() {
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

                            var index = events.getSelectedIndex();
                            if(index > -1){
                                var menuEditEvent = new JMenuItem(getString("dialog_object.edit_event"));
                                menuEditEvent.addActionListener(e -> showEditEventDialog(events.getSelectedValue()));
                                add(menuEditEvent);
                                var menuRemoveEvent = new JMenuItem(getString("dialog_object.remove_event"));
                                menuRemoveEvent.addActionListener(e -> listModel.removeElementAt(index));
                                add(menuRemoveEvent);
                            }
                        }

                    }.show(events, e.getX(), e.getY());
                }
            }

            private void showEventSelection() {
                var event = EventSelectionDialog.show(window, ObjectEditDialog.this, map, object);
                if(event != null)
                    listModel.add(events.getSelectedIndex() + 1, event);
            }

            private void showEditEventDialog(IEvent event){
                if(event.openDialog(window, ObjectEditDialog.this, window.getSingleton(GameData.class), map, object))
                    events.repaint();
            }

        });

        var scroll = new JScrollPane(events);
        scroll.setPreferredSize(new Dimension(600, 800));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel setupButtons() {
        var panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));

        var btnApply = new JButton(getString("button.apply"));
        btnApply.addActionListener(l -> {
            applyData();
            dispose();
        });
        panel.add(btnApply);

        var btnCancel = new JButton(getString("button.cancel"));
        btnCancel.addActionListener(l -> dispose());
        panel.add(btnCancel);

        return panel;
    }

    private final Color panleBG = UIManager.getColor("Panel.background");
    private final Color panleBG_B = panleBG.brighter();

    private JComponent cellRenderer(JList<? extends IEvent> jList, IEvent event, int index, boolean selected, boolean b) {
        var label = new JLabel(convertToHTML(event.getString(map)));
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

    private void applyData() {
        var elm = new ArrayList<IEvent>();
        listModel.elements().asIterator().forEachRemaining(elm::add);
        object.getEventList().putEvents(elm);
        object.getComponents().clear();
        object.getComponents().addAll(componentList);
        object.setName(name.getText().isBlank() ? null : name.getText());

        object.setX(((Number)x.getValue()).floatValue());
        object.setY(((Number)y.getValue()).floatValue());
        object.setZ(((Number)z.getValue()).floatValue());
        window.setProjectChanged();
        window.getMapView().repaint();
    }

    private WindowListener createWindowListener() {
        return new WindowAdapter() {
            boolean shouldRun;
            @Override
            public void windowOpened(WindowEvent e) {
                var t = new Thread(() -> {
                    while (shouldRun){
                        try {
                            Thread.sleep(16, 666666);
                            loopFunctions.values().forEach(Runnable::run);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }, "Object edit dialog looper");
                t.setDaemon(true);
                shouldRun = true;
                t.start();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                shouldRun = false;
            }
        };
    }

}
