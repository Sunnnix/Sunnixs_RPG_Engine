package de.sunnix.srpge.editor.window.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class NumberCondition extends de.sunnix.srpge.engine.evaluation.NumberCondition implements ICondition {

    private IValueProvider provider;
    private IValueProvider tmpProvider; // For GUI

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        ICondition.super.save(dso);
        dso.putByte("type", (byte) type.ordinal());
        dso.putDouble("number", number.doubleValue());
        if(provider != null)
            dso.putObject("provider", provider.save(new DataSaveObject()));
        return dso;
    }

    @Override
    public void load(DataSaveObject dso) {
        super.load(dso);
        var pDSO = dso.getObject("provider");
        if(pDSO != null)
            provider = EvaluationRegistry.loadProvider(pDSO.getString("id", null), pDSO);
    }

    @Override
    public String getString(Window window, MapData map, GameObject object) {
        if(provider == null)
            return "false";
        var sb = new StringBuilder(provider.getText(window, map, object));
        sb.append(" ");
        sb.append(type.text);
        sb.append(" ");
        sb.append(number.doubleValue());
        return sb.toString();
    }

    @Override
    public Runnable getEditGUI(Window window, MapData map, GameObject object, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.insets.set(3, 3, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

//        gbc.gridwidth = 3;
//        var setProviderBtn = new JButton("Set new provider");
//        content.add(setProviderBtn, gbc);
//        gbc.gridy++;
//        gbc.gridwidth = 1;

        var providerText = new JTextField("Not Set!");
        providerText.setEditable(false);
        providerText.setPreferredSize(new Dimension(200, 22));
        content.add(providerText, gbc);
        gbc.gridx++;
        var typeCombo = new JComboBox<>(Arrays.stream(NumEvalType.values()).map(t -> t.text).toArray(String[]::new));
        typeCombo.setPreferredSize(new Dimension(45, typeCombo.getPreferredSize().height));
        content.add(typeCombo, gbc);
        gbc.gridx++;
        var valueSpinner = new JSpinner(new SpinnerNumberModel(number.doubleValue(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        valueSpinner.setPreferredSize(new Dimension(100, valueSpinner.getPreferredSize().height));
        content.add(valueSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        tmpProvider = provider == null ? null : (IValueProvider) provider.clone();

        // Listeners
        providerText.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                    if(tmpProvider == null) {
                        var selector = new JComboBox<>(new String[]{ "Global", "Local" });
                        selector.setSelectedIndex(0);
                        if(!DialogUtils.showMultiInputDialog(content, "Add Provider", null, new String[]{"Provider:"}, new JComponent[]{selector}))
                            return;
                        tmpProvider = createProvider(window, content, map, object, selector.getSelectedIndex() == 0 ? new NumberVariableProvider() : new ObjectVariableProvider());
                        if(tmpProvider != null)
                            providerText.setText(tmpProvider.getText(window, map, object));
                    } else
                        if(editProvider(window, content, map, object, tmpProvider))
                            providerText.setText(tmpProvider.getText(window, map, object));
            }
        });

        // Set values
        if(tmpProvider != null)
            providerText.setText(tmpProvider.getText(window, map, object));
        typeCombo.setSelectedIndex(type.ordinal());

        return () -> {
            provider = tmpProvider;
            type = NumEvalType.values()[typeCombo.getSelectedIndex()];
            number = (Number)valueSpinner.getValue();
        };
    }

    private IValueProvider createProvider(Window window, JPanel content, MapData map, GameObject object, IValueProvider provider){
        if(editProvider(window, content, map, object, provider))
            return provider;
        else
            return null;
    }

    private boolean editProvider(Window window, JPanel parent, MapData map, GameObject object, IValueProvider provider){
        var dialog = new JDialog(DialogUtils.getWindowForComponent(parent), "Edit value provider", Dialog.ModalityType.APPLICATION_MODAL){
            boolean applied;
            {
                ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setLayout(new BorderLayout());
                var content = new JPanel();
                var onApply = provider.getEditGUI(window, map, object, content);
                add(content, BorderLayout.CENTER);

                var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                var applyBtn = new JButton(Language.getString("button.apply"));
                applyBtn.addActionListener(l -> {
                    if(onApply != null)
                        onApply.run();
                    applied = true;
                    dispose();
                });
                buttons.add(applyBtn);
                var cancelBtn = new JButton(Language.getString("button.cancel"));
                cancelBtn.addActionListener(l -> dispose());
                buttons.add(cancelBtn);
                add(buttons, BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(parent);
                setVisible(true);
            }
        };
        return dialog.applied;
    }

    @Override
    public Object clone() {
        try {
            var clone = (NumberCondition) super.clone();
            if(clone.provider != null)
                clone.provider = (IValueProvider) provider.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
