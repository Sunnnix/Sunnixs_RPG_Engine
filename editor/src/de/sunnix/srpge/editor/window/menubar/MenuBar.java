package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.menubar.resource.ResourceDialog;
import lombok.Getter;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class MenuBar extends JMenuBar {

    private final Window window;

    private final List<JComponent> projectDependentMenus = new LinkedList<>();
    private JMenu recentProjects;

    @Getter
    private UndoManager undoManager;

    public MenuBar(Window parent){
        this.window = parent;
        add(setUpMMFile());
        add(addProjectDependentComponent(setUpMMEdit()));
        add(setUpMMGame());
        add(addProjectDependentComponent(setUpMMPlayer()));
        add(setUpMMHelp());
    }

    private JMenu setUpMMFile(){
        var mm = new JMenu(getString("menu.file"));
        mm.add(createDefaultMenuItem(getString("menu.file.new_project"), KeyEvent.VK_N, ActionEvent.CTRL_MASK, e -> window.newProject()));
        mm.add(createDefaultMenuItem(getString("menu.file.open_project"), KeyEvent.VK_O, ActionEvent.CTRL_MASK, e -> window.loadProject(null)));
        mm.add(recentProjects = new JMenu(getString("menu.file.recent_projects")));
        genRecentProjectsList();

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.save_project"), KeyEvent.VK_S, ActionEvent.CTRL_MASK, e -> window.saveProject(false))));
        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.save_project_as"), KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, e -> window.saveProject(true))));

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem("Project properties", e -> new ProjectPropertiesDialog(window))));
        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.open_resource_manager"), e -> new ResourceDialog(window))));

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.close_project"), KeyEvent.VK_X, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, e -> window.closeProject())));

        mm.add(new JSeparator());

        mm.add(createDefaultMenuItem(getString("menu.file.exit"), e -> window.exit()));
        return mm;
    }

    private JMenu setUpMMEdit(){
        var mm = new JMenu(getString("menu.edit"));
        var undoName = getString("menu.edit.undo");
        var redoName = getString("menu.edit.redo");
        var undo = createDefaultMenuItem(undoName, KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK, l -> undoManager.undo());
        undo.setEnabled(false);
        mm.add(undo);
        var redo = createDefaultMenuItem(redoName, KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK, l -> undoManager.redo());
        redo.setEnabled(false);
        mm.add(redo);
        undoManager = new UndoManager(){
            @Override
            public synchronized boolean addEdit(UndoableEdit anEdit) {
                var e = super.addEdit(anEdit);
                resetMenus();
                return e;
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                resetMenus();
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                resetMenus();
            }

            @Override
            public synchronized void discardAllEdits() {
                super.discardAllEdits();
                resetMenus();
            }

            private void resetMenus(){
                if(canUndo()) {
                    undo.setEnabled(true);
                    undo.setText(undoName + " - " + getUndoPresentationName());
                } else {
                    undo.setEnabled(false);
                    undo.setText(undoName);
                }
                if(canRedo()) {
                    redo.setEnabled(true);
                    redo.setText(redoName + " - " + getRedoPresentationName());
                } else {
                    redo.setEnabled(false);
                    redo.setText(redoName);
                }
            }
        };

        mm.add(new JSeparator());
        var config = window.getSingleton(Config.class);
        mm.add(createCheckboxMenu(getString("menu.file.animate_tiles"), config.get("animate_tiles", false), b -> config.set("animate_tiles", b)));

        mm.add(new JSeparator(JSeparator.HORIZONTAL));

        var paste = createDefaultMenuItem(getString("menu.edit.paste"), KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK, l -> window.onPaste());
        paste.setEnabled(false);
        mm.add(createDefaultMenuItem(getString("menu.edit.copy"), KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, l -> {
            if(window.onCopy() != null)
                paste.setEnabled(true);
        }));
        mm.add(paste);

        return mm;
    }

    private JMenu setUpMMGame() {
        var mm = new JMenu(getString("menu.game"));
        var config = window.getSingleton(Config.class);
        var gameConfig = config.getJSONObject("game");
        mm.add(createCheckboxMenu(getString("menu.game.show_profiler"), gameConfig.get("show_profiler", false), b -> {
            gameConfig.put("show_profiler", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu(getString("menu.game.power_save_mode"), gameConfig.get("power_save_mode", false), b -> {
            gameConfig.put("power_save_mode", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu(getString("menu.game.vsync"), gameConfig.get("vsync", true), b -> {
            gameConfig.put("vsync", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu(getString("menu.game.debug"), gameConfig.get("debug", false), b -> {
            gameConfig.put("debug", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu(getString("menu.game.use_manual_gc"), gameConfig.get("use_manual_gc", false), b -> {
            gameConfig.put("use_manual_gc", b);
            config.set("game", gameConfig);
        }));
        return mm;
    }

    private static JCheckBoxMenuItem createCheckboxMenu(String text, boolean selected, Consumer<Boolean> onChange){
        var menu = new JCheckBoxMenuItem(text, selected);
        menu.addActionListener(l -> onChange.accept(menu.isSelected()));
        return menu;
    }

    private JMenu setUpMMPlayer() {
        var mm = new JMenu(getString("menu.player"));
        mm.add(createDefaultMenuItem(getString("menu.player.manage_sprites"), e -> new PlayerSpriteManager(window)));
        return mm;
    }

    private JMenu setUpMMHelp() {
        var mm = new JMenu(getString("menu.help"));
        mm.add(createDefaultMenuItem(getString("menu.help.user_guide"), e -> {
            var url = "https://sunnix.de/wiki";
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(window, "There was an error opening the URL (" + url + ")!\n" + ex.getMessage(), "Error opening URL", JOptionPane.ERROR_MESSAGE);
            }
        }));
        var langMenu = createDefaultMenuItem(getString("menu.help.language"), e -> new LanguageDialog(window));
        mm.add(langMenu);
        mm.add(createDefaultMenuItem(getString("menu.help.about"), e -> new AboutDialog(window)));
        return mm;
    }

    private static JMenuItem createDefaultMenuItem(String text, int keyShortcut, int modifier, ActionListener al){
        var mi = new JMenuItem(text);
        if(keyShortcut != 0)
            mi.setAccelerator(KeyStroke.getKeyStroke(keyShortcut, modifier));
        if(al != null)
            mi.addActionListener(al);
        return mi;
    }

    private static JMenuItem createDefaultMenuItem(String text, int keyShortcut, ActionListener al){
        return createDefaultMenuItem(text, keyShortcut, 0, al);
    }

    private static JMenuItem createDefaultMenuItem(String text, ActionListener al){
        return createDefaultMenuItem(text, 0, al);
    }

    public void genRecentProjectsList(){
        recentProjects.removeAll();
        List<String> recentProjectsList = window.getSingleton(Config.class).get("recent_projects", Collections.emptyList());
        if(recentProjectsList.isEmpty()) {
            var noProject = new JMenuItem(getString("menu.file.recent_projects.no_project_found"));
            noProject.setEnabled(false);
            recentProjects.add(noProject);
        } else {
            recentProjectsList.forEach(project -> {
                var item = new JMenuItem(project);
                item.addActionListener(a -> window.loadProject(project));
                if(!new File(project).exists())
                    item.setEnabled(false);
                recentProjects.add(item);
            });
            recentProjects.add(new JSeparator());
            var clearList = new JMenuItem(getString("menu.file.recent_projects.clear_list"));
            clearList.addActionListener(a -> {
                if(JOptionPane.showConfirmDialog(window,
                        getString("menu.file.recent_projects.clear_list.dialog_txt"),
                        getString("menu.file.recent_projects.clear_list"),
                        JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
                    window.getSingleton(Config.class).set("recent_projects", Collections.emptyList());
                genRecentProjectsList();
            });
            recentProjects.add(clearList);
            if(Arrays.stream(recentProjects.getMenuComponents()).anyMatch(c -> c instanceof JMenuItem i && !i.isEnabled())){
                var removeDisabled = new JMenuItem(getString("menu.file.recent_projects.remove_not_found_projects"));
                removeDisabled.addActionListener(a -> {
                    window.getSingleton(Config.class).change("recent_projects", Collections.<String>emptyList(), list -> {
                        list.removeIf(s -> !new File(s).exists());
                        return list;
                    });
                    genRecentProjectsList();
                });
                recentProjects.add(removeDisabled);
            }
        }
    }

    /**
     * adds a component to a list that makes its components enabled when opening a project and disabled when closing a project
     */
    public <T extends JComponent> T addProjectDependentComponent(T comp){
        projectDependentMenus.add(comp);
        return comp;
    }

    public void enableProjectOptions(boolean enable) {
        projectDependentMenus.forEach(menu -> menu.setEnabled(enable));
    }

}
