package de.sunnix.aje.editor.util;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static de.sunnix.aje.editor.util.FunctionUtils.createButton;

public class DialogUtils {

    public static boolean showMultiInputDialog(Component parent, String title, String message, String[] names, JComponent[] components){
        if(names.length != components.length)
            throw new IllegalArgumentException(String.format("Names must be same size as components: (%s/%s)", names.length, components.length));
        var dialog = new MultiInputDialog(parent, title, message, names, components);
        dialog.show();
        return dialog.applied;
    }

    public static String[] showMultiTextInputDialog(Component parent, String title, String message, String[] names, String[] defaults){
        if(names.length != defaults.length)
            throw new IllegalArgumentException(String.format("Names must be same size as defaults: (%s/%s)", names.length, defaults.length));
        var components = new JTextField[names.length];
        for (int i = 0; i < defaults.length; i++)
            components[i] = new JTextField(defaults[i], 15);
        if(showMultiInputDialog(parent, title, message, names, components))
            return Arrays.stream(components).map(JTextField::getText).toArray(String[]::new);
        return null;
    }

    public static String[] showMultiTextInputDialog(Component parent, String title, String message, String[] names){
        var defaults = new String[names.length];
        return showMultiTextInputDialog(parent, title, message, names, defaults);
    }

    private static class MultiInputDialog extends JDialog {

        private boolean applied;

        private MultiInputDialog(Component parent, String title, String message, String[] names, JComponent[] components){
            super((JFrame) null, title, true);
            var content = new JPanel(new BorderLayout(10, 10));
            content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            var label = new JLabel("<html>" + message.replaceAll("\n", "<br>") + "</html>");
            content.add(label, BorderLayout.NORTH);
            content.add(genContent(names, components), BorderLayout.CENTER);
            content.add(genButtons(), BorderLayout.SOUTH);
            setContentPane(content);
            setResizable(false);
            pack();
            setLocationRelativeTo(parent);
        }

        private JPanel genContent(String[] names, JComponent[] components) {
            var panel = new JPanel(new GridBagLayout());
            var gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 5, 5, 0);
            for (int i = 0; i < names.length; i++) {
                panel.add(new JLabel(names[i]), gbc);
                gbc.gridx++;
                panel.add(components[i], gbc);
                gbc.gridx = 0;
                gbc.gridy++;
            }
            return panel;
        }

        private JPanel genButtons() {
            var panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            panel.add(createButton("Apply", l -> {
                applied = true;
                dispose();
            }));
            panel.add(createButton("Cancel", l -> dispose()));
            return panel;
        }

    }

}
