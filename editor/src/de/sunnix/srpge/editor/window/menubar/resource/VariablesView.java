package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class VariablesView extends JPanel {

    private final Window window;
    private final JPanel parent;

    public VariablesView(Window window, JPanel parent) {
        this.window = window;
        this.parent = parent;

        setLayout(new GridLayout(1, 0, 5, 5));

        add(createVarsView("Integer Values", Variables::getIntsSize, Variables::setIntsSize, Variables::getIntName, Variables::setIntName));
        add(createVarsView("Floating Values", Variables::getFloatsSize, Variables::setFloatsSize, Variables::getFloatName, Variables::setFloatName));
        add(createVarsView("Boolean Values", Variables::getBoolsSize, Variables::setBoolsSize, Variables::getBoolName, Variables::setBoolName));
    }

    private JPanel createVarsView(String name, Supplier<Integer> getSize, Consumer<Integer> setSize, Function<Integer, String> getName, BiConsumer<Integer, String> setName) {
        var panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(name));

        var model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        model.addTableModelListener(e -> {
            var col = e.getColumn();
            var row = e.getFirstRow();
            if (col != 1 || e.getType() != TableModelEvent.UPDATE)
                return;
            setName.accept(row, (String) model.getValueAt(row, col));
            window.setProjectChanged();
        });
        model.setColumnIdentifiers( new Object[]{ "I", "Description" });
        var table = new JTable(model);
        var column0 = table.getColumnModel().getColumn(0);
        column0.setMaxWidth(26);
        column0.setMinWidth(26);
        table.setShowGrid(true);
        setUpTable(model, getSize, getName);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        var changeSize = new JButton("Change size");
        changeSize.addActionListener(l -> {
            var spinner = new JSpinner(new SpinnerNumberModel((int) getSize.get(), 10, 1000, 1));
            if(!DialogUtils.showMultiInputDialog(VariablesView.this, "Set new size", null, new String[]{ "Size:" }, new JComponent[]{ spinner }))
                return;
            var newSize = ((Number)spinner.getValue()).intValue();
            if(newSize == getSize.get())
                return;
            setSize.accept(newSize);
            setUpTable(model, getSize, getName);
            window.setProjectChanged();
        });
        panel.add(changeSize, BorderLayout.SOUTH);

        return panel;
    }

    private void setUpTable(DefaultTableModel model, Supplier<Integer> getSize, Function<Integer, String> getName){
        model.setRowCount(0);
        var size = getSize.get();
        for(var i = 0; i < size; i++)
            model.addRow(new Object[]{String.format("%03d", i), getName.apply(i)});
    }

}
