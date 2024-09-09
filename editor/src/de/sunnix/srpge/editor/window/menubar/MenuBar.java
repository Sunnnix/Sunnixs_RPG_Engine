package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.docu.UserGuide;
import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.menubar.resource.ResourceDialog;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class MenuBar extends JMenuBar {

    private final Window window;

    private final List<JComponent> projectDependentMenus = new LinkedList<>();
    private JMenu recentProjects;

    public MenuBar(Window parent){
        this.window = parent;
        add(setUpMMFile());
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

        var config = window.getSingleton(Config.class);
        mm.add(createCheckboxMenu(getString("menu.file.animate_tiles"), config.get("animate_tiles", false), b -> {
            config.set("animate_tiles", b);
        }));

        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.open_resource_manager"), e -> new ResourceDialog(window))));

        mm.add(new JSeparator());

        mm.add(addProjectDependentComponent(createDefaultMenuItem(getString("menu.file.close_project"), KeyEvent.VK_X, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, e -> window.closeProject())));

        mm.add(new JSeparator());

        mm.add(createDefaultMenuItem(getString("menu.file.exit"), e -> window.exit()));
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
        mm.add(createDefaultMenuItem(getString("menu.help.user_guide"), e -> new UserGuide(window)));
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
