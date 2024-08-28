package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.menubar.resource.LuaScriptView;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.ScriptList;
import de.sunnix.srpge.editor.window.script.ScriptEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LuaScriptEvent extends de.sunnix.srpge.engine.ecs.event.LuaScriptEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("script", script);
        return dso;
    }

    @Override
    public String getGUIText(MapData map) {
        return script;
    }

    @Override
    public String getMainColor() {
        return "/cd6f";
    }

    @Override
    public String getEventDisplayName() {
        return "Run Lua Script";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel contentPanel) {
        contentPanel.setLayout(new BorderLayout());
        var scriptManager = window.getSingleton(Resources.class).scriptList;
        var topPanel = new JPanel(new BorderLayout());
        var combo = new JComboBox<>(scriptManager.getScriptNames(ScriptList.ScriptType.Event).toArray(String[]::new));
        topPanel.add(combo, BorderLayout.CENTER);
        var editScripts = new JButton("Edit scripts");
        editScripts.addActionListener(l -> {
            var s = (String) combo.getSelectedItem();
            new ScriptDialog(window, contentPanel);
            combo.removeAllItems();
            scriptManager.getScriptNames(ScriptList.ScriptType.Event).forEach(combo::addItem);
            combo.setSelectedItem(s);
        });
        topPanel.add(editScripts, BorderLayout.EAST);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        var editor = new ScriptEditor();
        var area = editor.getTextArea();
        area.setEditable(false);
        combo.setSelectedIndex(-1);
        combo.addActionListener(l -> {
            area.setText(combo.getSelectedIndex() == -1 ? "" : scriptManager.getScript(ScriptList.ScriptType.Event, (String) combo.getSelectedItem()));
        });
        contentPanel.add(editor, BorderLayout.CENTER);
        contentPanel.setPreferredSize(new Dimension(500, 300));
        return () -> {
            script = (String) combo.getSelectedItem();
        };
    }

    private class ScriptDialog extends JDialog {

        public ScriptDialog(Window window, Component parent){
            super(DialogUtils.getWindowForComponent(parent), "Script Editor", ModalityType.APPLICATION_MODAL);
            var panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            var view = new LuaScriptView(window, panel);
            view.setSelectedScriptType(ScriptList.ScriptType.Event);
            panel.add(view, BorderLayout.CENTER);

            view.onViewAttached();

            setContentPane(panel);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    view.onViewClosed();
                }
            });

            setSize(new Dimension(500, 600));
            setLocationRelativeTo(parent);
            setVisible(true);
        }

    }

}
