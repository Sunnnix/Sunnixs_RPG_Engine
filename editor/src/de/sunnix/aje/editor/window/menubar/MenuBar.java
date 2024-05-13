package de.sunnix.aje.editor.window.menubar;

import de.sunnix.aje.editor.docu.UserGuide;
import de.sunnix.aje.editor.window.Config;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.menubar.resource.ResourceDialog;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuBar extends JMenuBar {

    private final Window parent;

    private final List<JComponent> projectDependentMenus = new LinkedList<>();
    private JMenu recentProjects;

    public MenuBar(Window parent){
        this.parent = parent;
        add(setUpMMFile());
        add(setUpMMGame());
        add(setUpMMHelp());
    }

    private JMenu setUpMMFile(){
        var mm = new JMenu("File");
        mm.add(createDefaultMenuItem("New Project", KeyEvent.VK_N, ActionEvent.CTRL_MASK, e -> parent.newProject()));
        mm.add(createDefaultMenuItem("Open Project", KeyEvent.VK_O, ActionEvent.CTRL_MASK, e -> parent.loadProject(null)));
        mm.add(recentProjects = new JMenu("Recent Projects..."));
        genRecentProjectsList();

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem("Save Project", KeyEvent.VK_S, ActionEvent.CTRL_MASK, e -> parent.saveProject(false))));
        mm.add(addProjectDependentComponent(createDefaultMenuItem("Save Project as ...", KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, e -> parent.saveProject(true))));

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem("Open Resource Manager", e -> new ResourceDialog(parent))));

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem("Close Project", KeyEvent.VK_X, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, e -> parent.closeProject())));

        mm.add(new JSeparator());

        mm.add(createDefaultMenuItem("Exit", e -> parent.exit()));
        return mm;
    }

    private JMenu setUpMMGame() {
        var mm = new JMenu("Game");
        var config = parent.getSingleton(Config.class);
        var gameConfig = config.getJSONObject("game");
        mm.add(createCheckboxMenu("Show Profiler", gameConfig.get("show_profiler", false), b -> {
            gameConfig.put("show_profiler", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu("Power Save Mode", gameConfig.get("power_save_mode", false), b -> {
            gameConfig.put("power_save_mode", b);
            config.set("game", gameConfig);
        }));
        mm.add(createCheckboxMenu("VSync", gameConfig.get("vsync", true), b -> {
            gameConfig.put("vsync", b);
            config.set("game", gameConfig);
        }));
        return mm;
    }

    private static JCheckBoxMenuItem createCheckboxMenu(String text, boolean selected, Consumer<Boolean> onChange){
        var menu = new JCheckBoxMenuItem(text, selected);
        menu.addActionListener(l -> onChange.accept(menu.isSelected()));
        return menu;
    }

    private JMenu setUpMMHelp() {
        var mm = new JMenu("Help");
        mm.add(createDefaultMenuItem("User Guide", e -> new UserGuide(parent)));
        var langMenu = createDefaultMenuItem("Language (WIP)", null);
        langMenu.setEnabled(false);
        mm.add(langMenu);
        mm.add(createDefaultMenuItem("About", e -> new AboutDialog(parent)));
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
        List<String> recentProjectsList = parent.getSingleton(Config.class).get("recent_projects", Collections.emptyList());
        if(recentProjectsList.isEmpty()) {
            var noProject = new JMenuItem("No project found!");
            noProject.setEnabled(false);
            recentProjects.add(noProject);
        } else {
            recentProjectsList.forEach(project -> {
                var item = new JMenuItem(project);
                item.addActionListener(a -> parent.loadProject(project));
                if(!new File(project).exists())
                    item.setEnabled(false);
                recentProjects.add(item);
            });
            recentProjects.add(new JSeparator());
            var clearList = new JMenuItem("Clear List");
            clearList.addActionListener(a -> {
                if(JOptionPane.showConfirmDialog(parent,
                        "Are you sure, to clear the recent projects list?",
                        "Clear list",
                        JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
                    parent.getSingleton(Config.class).set("recent_projects", Collections.emptyList());
                genRecentProjectsList();
            });
            recentProjects.add(clearList);
            if(Arrays.stream(recentProjects.getMenuComponents()).anyMatch(c -> c instanceof JMenuItem i && !i.isEnabled())){
                var removeDisabled = new JMenuItem("Remove not found projects");
                removeDisabled.addActionListener(a -> {
                    parent.getSingleton(Config.class).change("recent_projects", Collections.<String>emptyList(), list -> {
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
