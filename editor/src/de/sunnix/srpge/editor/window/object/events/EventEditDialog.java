package de.sunnix.srpge.editor.window.object.events;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class EventEditDialog extends JDialog {

    @Getter
    private boolean saved;

    public EventEditDialog(JDialog parent, String name, JPanel contentPanel) {
        super(parent, name, true);
        var mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10,10, 10));

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        var applyBtn = new JButton(getString("button.apply"));
        applyBtn.addActionListener(l -> {
            saved = true;
            dispose();
        });
        buttonsPanel.add(applyBtn);

        var cancelBtn = new JButton(getString("button.cancel"));
        cancelBtn.addActionListener(l -> dispose());
        buttonsPanel.add(cancelBtn);

        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
