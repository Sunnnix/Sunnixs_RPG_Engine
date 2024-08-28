package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.ScriptList;
import de.sunnix.srpge.editor.window.script.ScriptEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LuaScriptView  extends JPanel implements IResourceView{

    private Window window;
    private JPanel parent;

    private JComboBox<ScriptList.ScriptType> scriptType;
    private JList<String> scriptList;
    private ScriptEditor editor;

    public LuaScriptView(Window window, JPanel parent) {
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(createEditorPanel(), BorderLayout.CENTER);
        add(createSelectionPanel(editor), BorderLayout.WEST);
    }

    private JPanel createSelectionPanel(ScriptEditor editor){
        var panel = new JPanel(new BorderLayout());

        var scriptType = new JComboBox<>(ScriptList.ScriptType.values());
        panel.add(scriptType, BorderLayout.NORTH);

        var lModel = new DefaultListModel<String>();
        var list = new JList<>(lModel);
        list.setBorder(BorderFactory.createTitledBorder("Scripts")); // TODO language file
        list.setPreferredSize(new Dimension(200, 0));
        scriptType.addActionListener(l -> {
            lModel.clear();
            editor.resetParams();
            if(scriptType.getSelectedIndex() >= 0) {
                var selectedType = (ScriptList.ScriptType) scriptType.getSelectedItem();
                lModel.addAll(window.getSingleton(Resources.class).scriptList.getScriptNames(selectedType));
                editor.addParams(selectedType.defaultParams);
            }
            editor.getTextArea().requestFocus();
        });
        this.scriptType = scriptType;
        list.addListSelectionListener(l -> {
            // TODO check prev Index and ask for saving
            editor.getTextArea().setText(
                    list.getSelectedIndex() == -1 ?
                            "" :
                            window.getSingleton(Resources.class).scriptList.getScript((ScriptList.ScriptType) scriptType.getSelectedItem(), list.getSelectedValue())
            );
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                editor.getTextArea().requestFocus();
            }
        });
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        scriptList = list;

        scriptType.setSelectedIndex(0);

        return panel;
    }

    private JPanel createEditorPanel(){
        var panel = new JPanel(new BorderLayout());

        editor = new ScriptEditor();
        editor.getTextArea().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(!e.isControlDown())
                    return;
                switch (e.getKeyCode()){
                    case KeyEvent.VK_N -> newScript();
                    case KeyEvent.VK_S -> saveScript();
                    case KeyEvent.VK_R -> resetScript();
                }
            }
        });
        panel.add(editor, BorderLayout.CENTER);

        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        var neW = new JButton("New");
        neW.addActionListener(l -> newScript());
        var apply = new JButton("Save");
        apply.addActionListener(l -> saveScript());
        var reset = new JButton("Reset");
        reset.addActionListener(l -> resetScript());

        buttons.add(neW);
        buttons.add(apply);
        buttons.add(reset);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private void newScript(){
        scriptList.setSelectedValue(null, false);
        editor.getTextArea().requestFocus();
    }

    private void saveScript(){
        var scriptManager = window.getSingleton(Resources.class).scriptList;
        var selectedType = (ScriptList.ScriptType) scriptType.getSelectedItem();
        if(scriptList.getSelectedIndex() == -1){
            var name = "";
            do {
                name = JOptionPane.showInputDialog(parent, "Insert script name", name);
            } while(name != null && !DialogUtils.validateInput(parent, name, scriptManager.getScriptNames(selectedType)));
            if(name == null) {
                editor.getTextArea().requestFocus();
                return;
            }
            scriptManager.setScript(selectedType, name, editor.getTextArea().getText());
            ((DefaultListModel<String>)scriptList.getModel()).addElement(name);
            scriptList.setSelectedValue(name, true);
        } else
            scriptManager.setScript(selectedType, scriptList.getSelectedValue(), editor.getTextArea().getText());
        window.setProjectChanged();
        editor.getTextArea().requestFocus();
    }

    private void resetScript(){
        var i = scriptList.getSelectedIndex();
        scriptList.setSelectedValue(null, false);
        scriptList.setSelectedIndex(i);
        editor.getTextArea().requestFocus();
    }

    public void setSelectedScriptType(ScriptList.ScriptType type){
        scriptType.setSelectedItem(type);
    }

    @Override
    public void onViewAttached() {
        editor.getTextArea().requestFocus();
    }

    @Override
    public void onViewClosed() {}

}
