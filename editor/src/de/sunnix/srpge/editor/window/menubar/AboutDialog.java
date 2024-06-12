package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.Texts.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Window parent) {
        super(parent, WINDOW_NAME + " - " + getString("menu.help.about"), true);
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

        var bdList = loadBDList();

        if(!bdList.isEmpty()){
            panel.add(new JSeparator(JSeparator.HORIZONTAL));

            var tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tmpPanel.getInsets().set(10, 0, 10, 0);

            var sb = new StringBuilder();
            for(var bdName: bdList)
                sb.append(bdName).append(", ");
            sb.delete(sb.length() - 2, sb.length());
            var bdlabel = new JLabel(getString("dialog_about.bd_text", sb.toString()));
            tmpPanel.add(bdlabel);
            panel.add(tmpPanel);
        }

        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        var btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var closeBtn = new JButton(getString("button.close"));
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
                            getString("dialog_about.exception_thrown", ex.getMessage()),
                            getString("name.error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        return text;
    }

    private List<String> loadBDList(){
        var currentDate = Calendar.getInstance();
        var m = currentDate.get(Calendar.MONTH);
        var d = currentDate.get(Calendar.DAY_OF_MONTH);
        var dtf = new SimpleDateFormat("MM.dd");
        var list = new ArrayList<String>();
        try(var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/de/sunnix/srpge/editor/window/about/bdl.txt")))){
            String line;
            while((line = reader.readLine()) != null){
                if(!line.contains("="))
                    continue;
                var split = line.split("=");
                try {
                    var date = new Calendar.Builder().setInstant(dtf.parse(split[0].trim())).build();
                    if(date.get(Calendar.MONTH) == m && date.get(Calendar.DAY_OF_MONTH) == d)
                        list.add(split[1].trim());
                } catch (Exception e){
                    continue;
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

}
