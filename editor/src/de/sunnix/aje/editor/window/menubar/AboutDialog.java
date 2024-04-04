package de.sunnix.aje.editor.window.menubar;

import de.sunnix.aje.editor.window.Window;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;

import static de.sunnix.aje.editor.window.Texts.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Window parent) {
        super(parent, WINDOW_NAME + " - About", true);
        setContentPane(createContent());
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JPanel createContent() {
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        var text = genAboutText();
        panel.add(text);

        var btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        panel.add(btnPanel);

        return panel;
    }

    private JTextPane genAboutText() {
        var text = new JTextPane();
        text.setContentType("text/html");
        text.setEditable(false);
        text.setText(ABOUT_HTML_TEXT);
        text.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AboutDialog.this,
                            "An exception was thrown!\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        return text;
    }
}
