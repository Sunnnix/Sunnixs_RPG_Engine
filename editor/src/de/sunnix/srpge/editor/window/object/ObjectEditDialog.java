package de.sunnix.srpge.editor.window.object;

import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.ClosableTitledBorder;
import de.sunnix.srpge.editor.window.object.components.Component;
import de.sunnix.srpge.editor.window.object.components.ComponentCreateDialog;
import de.sunnix.srpge.editor.window.object.events.EventList;
import de.sunnix.srpge.editor.window.object.events.IEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

public class ObjectEditDialog extends JDialog {


    private final Window window;
    private final MapData map;
    private final GameObject object;

    private JTextField name;
    private JSpinner x, y, z;
    private JList<EventList> el;
    private List<EventList> eventLists;
    private JTabbedPane eventTabView;

    private List<Component> componentList;
    private JPanel componentsView;

    private final Map<JComponent, Runnable> loopFunctions = new HashMap<>();

    public ObjectEditDialog(Window window, MapData map, GameObject obj) {
        super(window, WINDOW_NAME + " - " + getString("dialog_object.title"), true);
        this.window = window;
        this.map = map;
        this.object = obj;
        this.eventLists = new ArrayList<>(obj.getEventLists().stream().map(EventList::clone).toList());

        setLayout(new BorderLayout(5, 5));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5 ,5 ,5 , 5));

        add(setupProperties(), BorderLayout.EAST);
        add(eventTabView = setupEventList(), BorderLayout.CENTER);
        add(setupButtons(), BorderLayout.SOUTH);

        addWindowListener(createWindowListener());

        if(!eventLists.isEmpty()) {
            var list = eventLists.get(0);
            eventTabView.addTab("1 - " + list.toString(), list.genGUI(window, map, object));
        }

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

        gbc.gridwidth = 2;
        panel.add(new JLabel("Event lists"), gbc);
        gbc.gridy++;
        el = new JList<>(new DefaultListModel<>());
        ((DefaultListModel<EventList>) el.getModel()).addAll(eventLists);
        var scroll = new JScrollPane(el);
        scroll.setPreferredSize(new Dimension(0, 100));
        panel.add(scroll, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        el.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
                    var s = el.getSelectedIndex();
                    if(s < 0)
                        return;
                    var value = el.getSelectedValue();
                    var name = (s + 1) + " - " + value.toString();
                    var index = eventTabView.indexOfTab(name);
                    if(index == -1) {
                        eventTabView.addTab(name, value.genGUI(window, map, object));
                        index = eventTabView.indexOfTab(name);
                    }
                    eventTabView.setSelectedIndex(index);
                } else if(e.getButton() == MouseEvent.BUTTON3) {
                    new JPopupMenu(){
                        {
                            var addEL = new JMenuItem("Add EventList");
                            addEL.addActionListener(l -> {
                                eventLists.add(new EventList());
                                var model = (DefaultListModel<EventList>)el.getModel();
                                model.clear();
                                model.addAll(eventLists);
                            });
                            add(addEL);
                        }
                    }.show(el, e.getX(), e.getY());
                }
            }
        });

        mainPanel.add(panel, BorderLayout.NORTH);

        mainPanel.add(createComponentPanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createComponentPanel(){
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        componentList = new ArrayList<>(object.getComponents().stream().map(Component::clone).toList());
        componentsView = panel;

        var addbtn = new JButton(getString("dialog_object.add_component"));
        addbtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, addbtn.getMinimumSize().height));
        addbtn.addActionListener(a -> {
            var component = ComponentCreateDialog.show(this, componentList);
            if(!component.isEmpty()) {
                componentList.addAll(component);
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
        synchronized (loopFunctions) {
            loopFunctions.clear();
        }
        while(componentsView.getComponents().length > 2)
            componentsView.remove(0);
        var comps = componentList;
        for(var i = 0; i < comps.size(); i++){
            var component = comps.get(i);
            var panel = ClosableTitledBorder.createClosableTitledPanel(component.genName(), c -> {
                if(JOptionPane.showConfirmDialog(window, getString("dialog_object.remove_component.text"), getString("dialog_object.remove_component.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                    return;
                removeComponent(object.getComponents(), componentList, component);
                loadComponentsView();
            });
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            var loop = component.createView(window, object, panel);
            if(loop != null)
                loopFunctions.put(panel, loop);
            componentsView.add(panel, i);
        }
        componentsView.revalidate();
        componentsView.repaint();
    }

    private void removeComponent(List<Component> objectComponents, List<Component> viewComponents, Component toRemove){
        var boundComps = viewComponents.stream().filter(c -> Arrays.stream(c.getDependencies()).anyMatch(s -> s.equals(toRemove.ID))).toList();
        viewComponents.remove(toRemove);
        boundComps.forEach(comp -> removeComponent(objectComponents, viewComponents, comp));
    }

    private JTabbedPane setupEventList() {
        var panel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(600, 800));
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

    private void applyData() {
        var elm = new ArrayList<IEvent>();
//        listModel.elements().asIterator().forEachRemaining(elm::add);  TODO apply lists
        if(object.getEventLists().isEmpty())
            object.getEventLists().add(new EventList());
        object.getEventLists().get(0).putEvents(elm);
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
                            synchronized (loopFunctions) {
                                loopFunctions.values().forEach(Runnable::run);
                            }
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
