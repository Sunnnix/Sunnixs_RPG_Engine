package de.sunnix.srpge.editor.window.object;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.events.Event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.StringToHTMLConverter.convertToHTML;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

public class ObjectEditDialog extends JDialog {


    private final Window window;
    private final MapData map;
    private final GameObject object;

    private JTextField name;
    private JSpinner x, y, z;
    private JList<Event> events;
    private DefaultListModel<Event> listModel;

    public ObjectEditDialog(Window window, MapData map, GameObject obj) {
        super(window, WINDOW_NAME + " - " + getString("dialog_object.title"), true);
        this.window = window;
        this.map = map;
        this.object = obj;

        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5 ,5 ,5 , 5));

        add(setupProperties(), BorderLayout.EAST);
        add(setupEventList(), BorderLayout.CENTER);
        add(setupButtons(), BorderLayout.SOUTH);

        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
    }

    private JPanel setupProperties() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
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

        var tPanel = new JPanel(new FlowLayout());
        tPanel.add(panel);
        return tPanel;
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
                var event = openEventSelectionDialog(map, object);
                if(event != null)
                    listModel.add(events.getSelectedIndex() + 1, event);
            }

            private void showEditEventDialog(Event event){
                if(event.openDialog(ObjectEditDialog.this, window.getSingleton(GameData.class), map, object))
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

    private Component cellRenderer(JList<? extends Event> jList, Event event, int index, boolean selected, boolean b) {
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

    private Event openEventSelectionDialog(MapData map, GameObject object){
        var dialog = new EventSelectionDialog(window, this, map, object);
        return dialog.getEvent();
    }

    private void applyData() {
        var elm = new ArrayList<Event>();
        listModel.elements().asIterator().forEachRemaining(elm::add);
        object.getEventList().putEvents(elm);
        object.setName(name.getText().isBlank() ? null : name.getText());

        object.setX(((Number)x.getValue()).floatValue());
        object.setY(((Number)y.getValue()).floatValue());
        object.setZ(((Number)z.getValue()).floatValue());
        window.setProjectChanged();
        window.getMapView().repaint();
    }

}
