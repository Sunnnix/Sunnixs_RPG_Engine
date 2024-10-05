package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class ProjectPropertiesDialog extends JDialog {

    public ProjectPropertiesDialog(Window window) {
        super(window, "Project properties", true);
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setLayout(new BorderLayout());

        var content = new JPanel();
        content.setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();

        content.add(new JLabel("Project name:"), gbc);
        gbc.gridy++;

        gbc.gridwidth = 2;
        var nameInput = new JTextField(window.getProjectName(), 20);
        content.add(nameInput, gbc);
        gbc.gridy++;

        var buttonsTab = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsTab.setBorder(BorderFactory.createTitledBorder("Button mode"));
        var group = new ButtonGroup();
        var psRadio = new JRadioButton("Playstation");
        group.add(psRadio);
        buttonsTab.add(psRadio);
        var xboxRadio = new JRadioButton("Xbox");
        group.add(xboxRadio);
        buttonsTab.add(xboxRadio);
        content.add(buttonsTab, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        if(window.isPsMode())
            psRadio.setSelected(true);
        else
            xboxRadio.setSelected(true);

        add(content, BorderLayout.CENTER);

        var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var apply = new JButton(getString("button.apply"));
        buttonsPanel.add(apply);
        apply.addActionListener(l -> {
            if(nameInput.getText().isBlank()){
                JOptionPane.showMessageDialog(ProjectPropertiesDialog.this, "The project name is not valid!", "Invalid project name", JOptionPane.ERROR_MESSAGE);
                return;
            }
            window.setProjectName(nameInput.getText());
            window.setPsMode(psRadio.isSelected());
            window.updateTitle();
            dispose();
        });
        var cancel = new JButton(getString("button.cancel"));
        buttonsPanel.add(cancel);
        cancel.addActionListener(l -> dispose());

        add(buttonsPanel, BorderLayout.SOUTH);

        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
    }
}
