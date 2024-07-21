package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.States;
import de.sunnix.srpge.engine.ecs.State;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class StatesView extends JPanel {

    private Window window;

    public StatesView(Window window, JPanel parent) {
        this.window = window;
        setLayout(new BorderLayout());

        add(new JScrollPane(createTable()), BorderLayout.CENTER);
    }

    private JTable createTable() {
        var table = new JTable(new TableModel());
        table.setShowGrid(true);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        var defRenderer = new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                var comp = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if(column == 1)
                    comp.setHorizontalAlignment(SwingConstants.CENTER);
                else {
                    comp.setHorizontalAlignment(SwingConstants.LEFT);
                    if(!States.isRemovable((String) value))
                        comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                }
                return comp;
            }
        };
        table.getColumnModel().getColumns().asIterator().forEachRemaining(x -> x.setCellRenderer(defRenderer));

        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(999);
        formatter.setAllowsInvalid(false);
        JFormattedTextField formattedTextField = new JFormattedTextField(formatter);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(formattedTextField));

//        table.setAutoCreateRowSorter(true);
//        var sorter = new TableRowSorter<>((TableModel) table.getModel());
//        table.setRowSorter(sorter);
//        sorter.toggleSortOrder(1);

        var popupMenu = new JPopupMenu();
        var addItem = new JMenuItem(getString("view.dialog_resources.variables.states.add_state"));
        var removeItem = new JMenuItem(getString("view.dialog_resources.variables.states.remove_state"));

        addItem.addActionListener(e -> {
            ((TableModel) table.getModel()).addNewState();
            window.setProjectChanged();
        });
        removeItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                ((TableModel) table.getModel()).removeState(row);
                window.setProjectChanged();
            }
        });

        popupMenu.add(addItem);
        popupMenu.add(removeItem);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        table.setRowSelectionInterval(row, row);
                        removeItem.setEnabled(table.getModel().isCellEditable(row, 0));
                    } else {
                        table.clearSelection();
                        removeItem.setEnabled(false);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        return table;
    }

    private class TableModel extends AbstractTableModel {

        private List<State> states;

        private String[] columnNames = { getString("view.dialog_resources.variables.states.id"), getString("view.dialog_resources.variables.states.prio") };

        public TableModel(){
            states = new ArrayList<>(States.getStates());
        }

        @Override
        public int getRowCount() {
            return States.getStates().size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex == -1)
                return null;
            var state = states.get(rowIndex);
            return switch (columnIndex){
                case 0 -> state.id();
                case 1 -> state.priority();
                default -> null;
            };
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return States.isRemovable((String) getValueAt(rowIndex, 0));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(rowIndex == -1)
                return;
            var state = states.get(rowIndex);
            states.set(states.indexOf(state), switch (columnIndex){
                case 0 -> {
                    var id = (String) aValue;
                    if(id.equals(state.id()) || id.isBlank() || states.stream().anyMatch(x -> id.equals(x.id())))
                        yield state;
                    yield States.changeStateId(window, state, id);
                }
                case 1 -> States.changeStatePrio(window, state, Integer.parseInt((String) aValue));
                default -> state;
            });
            fireTableDataChanged();
        }

        public void addNewState() {
            String newStateName;
            do {
                newStateName = "state_" + (int)(Math.random() * 10000);
            } while (States.hasStateID(newStateName));
            states.add(States.addState(newStateName, 0));
            fireTableDataChanged();
        }

        public void removeState(int row){
            var value = (String) getValueAt(row, 0);
            states.remove(States.removeState(window, value));
            fireTableDataChanged();
        }

    }

}
