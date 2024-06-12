package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.util.Tuple;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

public class LanguageDialog extends JDialog {

    private final Window window;

    private JList<Tuple.Tuple3<String, String, Boolean>> langList;
    private JCheckBox enFallback;

    private int selectedLang;

    public LanguageDialog(Window window){
        super(window, WINDOW_NAME + " - " + getString("menu.help.language"), true);
        this.window = window;

        setup();

        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setVisible(true);
    }

    private void setup(){
        var panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        var model = new DefaultListModel<Tuple.Tuple3<String, String, Boolean>>();
        langList = new JList<>(model);
        model.addAll(Arrays.asList(Language.getLanguagePacks()));
        selectedLang = Language.getSelectedLanguage();
        langList.setCellRenderer(genCellRenderer());
        var scroll = new JScrollPane(langList);
        scroll.setPreferredSize(new Dimension(200, 200));

        panel.add(scroll, BorderLayout.CENTER);

        var buttonsParent = new JPanel(new BorderLayout(10, 10));

        var config = window.getSingleton(Config.class);

        enFallback = new JCheckBox(getString("dialog.language.en_fallback"));
        enFallback.setSelected(config.get("en_fallback", true));
        enFallback.addActionListener(l -> {
            config.set("en_fallback", enFallback.isSelected());
            Language.setUseEnglishForMissing(enFallback.isSelected());
        });

        buttonsParent.add(enFallback, BorderLayout.NORTH);

        var buttons = new JPanel(new GridLayout(0, 2, 10, 10));

        var imp = new JButton(getString("name.import"));
        imp.addActionListener(this::importLanguage);
        var remove = new JButton(getString("name.remove"));
        remove.addActionListener(this::removeLanguage);
        var load = new JButton(getString("name.load"));
        load.addActionListener(l -> loadLanguage());
        var close = new JButton(getString("button.close"));
        close.addActionListener(l -> dispose());

        buttons.add(imp);
        buttons.add(load);
        buttons.add(remove);
        buttons.add(close);

        buttonsParent.add(buttons, BorderLayout.CENTER);

        panel.add(buttonsParent, BorderLayout.SOUTH);

        langList.addListSelectionListener(l -> {
            var value = langList.getSelectedValue();
            remove.setEnabled(value != null && !value.t3());
        });

        langList.setSelectedIndex(selectedLang);

        add(panel, BorderLayout.CENTER);
    }

    private void importLanguage(ActionEvent e) {
        var chooser = new JFileChooser(window.getSingleton(Config.class).get("chooser_project_path", (String)null));
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".lang");
            }

            @Override
            public String getDescription() {
                return getString("name.language_file") + " (.lang)";
            }
        });
        chooser.showOpenDialog(this);
        var file = chooser.getSelectedFile();
        if(file == null)
            return;
        if(Language.loadLanguagePack(file.getPath(), window.getSingleton(Config.class)))
            reloadList();
    }

    private void reloadList(){
        var selectedEntry = langList.getSelectedValue();
        var sLang = selectedEntry == null ? null : selectedEntry.t1();
        var model = (DefaultListModel<Tuple.Tuple3<String, String, Boolean>>)langList.getModel();
        model.clear();
        var langs = Arrays.asList(Language.getLanguagePacks());
        model.addAll(langs);
        langList.setSelectedIndex(langs.indexOf(langs.stream().filter(l -> l.t1().equals(sLang)).findFirst().orElse(null)));
        langList.repaint();
    }

    private void removeLanguage(ActionEvent e) {
        var selected = langList.getSelectedValue();
        if(selected == null)
            return;
        if(selected.t3()) {
            JOptionPane.showMessageDialog(this,
                    getString("dialog.language.remove_lang_pack.cant_remove_default"),
                    getString("dialog.language.remove_lang_pack.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(JOptionPane.showConfirmDialog(this,
                getString("dialog.language.remove_lang_pack.text", selected.t1()),
                getString("dialog.language.remove_lang_pack.title"),
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        try {
            Language.removeLanguagePack(selected.t1(), window.getSingleton(Config.class));
        } catch (RuntimeException ex){
            System.err.println(ex.getMessage());
            return;
        }
        ((DefaultListModel<Tuple.Tuple3<String, String, Boolean>>)langList.getModel()).removeElement(selected);
    }

    private void loadLanguage(){
        if(langList.getSelectedIndex() == -1){
            JOptionPane.showMessageDialog(this,
                    getString("dialog.language.no_lang_selected"),
                    getString("dialog.language.load_lang.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(langList.getSelectedIndex() == selectedLang)
            return;
        var value = langList.getSelectedValue();
        if(value == null) {
            JOptionPane.showMessageDialog(this,
                    getString("dialog.language.lang_not_found"),
                    getString("dialog.language.load_lang.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(Language.setLanguage(value.t1())) {
            window.getSingleton(Config.class).set("language", langList.getSelectedValue().t1());
            window.revalidate();
            window.repaint();
            JOptionPane.showMessageDialog(this, getString("dialog.language.reload_editor.text"), getString("dialog.language.reload_editor.title"), JOptionPane.WARNING_MESSAGE);
            dispose();
        } else
            JOptionPane.showMessageDialog(this, getString("dialog.language.set_lang_failed.text"), getString("dialog.language.set_lang_failed.title"), JOptionPane.ERROR_MESSAGE);
    }

    private DefaultListCellRenderer genCellRenderer(){
        return new DefaultListCellRenderer(){

            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                var label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                var tuple = (Tuple.Tuple3<String, String, Boolean>) value;
                label.setText(tuple.t1() + " - " + tuple.t2());
                if(!tuple.t3())
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                if(index == selectedLang) {
                    if (!isSelected)
                        label.setBackground(new Color(0f, .45f, 0f));
                } else if(!isSelected)
                    if(index % 2 == 0){
                        var c = label.getBackground();
                        var FACTOR = .8f;
                        label.setBackground(new Color(Math.max((int)(c.getRed() * FACTOR), 0),
                                Math.max((int)(c.getGreen() * FACTOR), 0),
                                Math.max((int)(c.getBlue() * FACTOR), 0),
                                c.getAlpha()));
                    }
                return label;
            }
        };
    }

}
