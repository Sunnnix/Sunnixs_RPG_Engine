package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.window.menubar.MenuBar;
import de.sunnix.aje.editor.window.resource.Resources;
import lombok.Getter;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static de.sunnix.aje.editor.window.Texts.WINDOW_NAME;

public class Window extends JFrame {

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    public final MenuBar menuBar;

    private File projectPath;
    @Getter
    private String projectName;

    @Getter
    private boolean projectOpen;
    @Getter
    private boolean projectChanged;

    public Window(){
        super();
        initSingletons();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setJMenuBar(menuBar = new MenuBar(this));
        var wl = setupWindowListener();
        addWindowListener(wl);
        addWindowStateListener(wl);
        var config = getSingleton(Config.class);
        var size = new int[] { config.get("window_width", 1600), config.get("window_height", 900) };
        var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if(screenSize.width < size[0])
            size[0] = screenSize.width;
        if(screenSize.height < size[1])
            size[1] = screenSize.height;
        setLocation(config.get("window_x", screenSize.width / 2 - size[0] / 2), config.get("window_y", screenSize.height / 2 - size[1] / 2));
        setSize(size[0], size[1]);
        if(config.get("window_extended", false))
            setExtendedState(MAXIMIZED_BOTH);
        setProjectOpen(false);
        updateTitle();
        setVisible(true);
    }

    private void initSingletons(){
        var config = new Config();
        config.loadConfig();
        singletons.put(Config.class, config);
        singletons.put(Resources.class, new Resources());
    }

    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> clazz){
        return (T) singletons.get(clazz);
    }

    public void updateTitle(){
        var title = new StringBuilder(WINDOW_NAME);
        if(projectOpen){
            title.append(" - ");
            title.append(Objects.requireNonNullElse(projectName, "New Project"));
        }
        if(projectChanged)
            title.append(" *");
        setTitle(title.toString());
    }

    /**
     * Empties all Data from the Window
     */
    private void cleanProject(){
        projectName = null;
        projectPath = null;
        projectChanged = false;
        getSingleton(Resources.class).reset();
        setProjectOpen(false);
        updateTitle();
    }

    public void setProjectOpen(boolean projectOpen) {
        this.projectOpen = projectOpen;
        menuBar.enableProjectOptions(projectOpen);
    }

    public boolean checkForSaving(){
        if(projectOpen && projectChanged){
            var option = JOptionPane.showConfirmDialog(this,
                    "There are unsaved changes. Do you want to save them?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            return option != JOptionPane.CLOSED_OPTION && option != JOptionPane.CANCEL_OPTION && (option != JOptionPane.YES_OPTION || saveProject(false));
        }
        return true;
    }

    public void newProject() {
        if(!checkForSaving())
            return;
        cleanProject();
        var input = (String) JOptionPane.showInputDialog(this, "Write a name for the id of the image:", "Create new Image Resource", JOptionPane.PLAIN_MESSAGE, null, null, "New Project");
        projectName = input == null ? "New Project" : input;
        setProjectOpen(true);
        setProjectChanged();
    }

    public void loadProject(String path) {
        if(!checkForSaving())
            return;
        File file;
        if(path == null){
            file = chooseGameFile(true);
            if(file == null)
                return;
        } else
            file = new File(path);
        if(!file.exists()){
            JOptionPane.showMessageDialog(
                    this,
                    String.format("The File %s was not found!", file),
                    "File not found!",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        cleanProject();

        try(var zip = new ZipFile(file)){
            JSONObject config;
            try {
                config = new JSONObject(new String(zip.getInputStream(new ZipEntry("game.config")).readAllBytes()));
            } catch (NullPointerException e){
                JOptionPane.showMessageDialog(
                        this,
                        "File missing game.config!",
                        "Missing config",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            if(config.has("project_name"))
                projectName = config.getString("project_name");
            else
                projectName = "Unnamed Project";
            getSingleton(Resources.class).loadResources(zip, config);
        } catch (Exception e){
            e.printStackTrace();
        }

        projectPath = file;

        getSingleton(Config.class).change("recent_projects", Collections.<String>emptyList(), list -> {
            var p = file.getPath();
            list.remove(p);
            list.add(0, p);
            return list;
        });
        menuBar.genRecentProjectsList();

        setProjectOpen(true);
        updateTitle();
    }

    public boolean saveProject(boolean openFilechooser) {
        File saveFile;
        if(projectName == null){
            String input = "";
            while(input != null) {
                input = (String) JOptionPane.showInputDialog(this, "Write a name for the id of the image:", "Create new Image Resource", JOptionPane.PLAIN_MESSAGE, null, null, input);
                if(input != null && !input.isEmpty())
                    break;
                JOptionPane.showMessageDialog(
                        this,
                        "Project name can't be empty!",
                        "Wrong project name",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            if(input == null)
                return false;
            projectName = input;
        }
        if(openFilechooser || projectPath == null)
            saveFile = chooseGameFile(false);
        else
            saveFile = projectPath;
        if(saveFile == null)
            return false;
        if(!saveFile.toString().endsWith(".aegf"))
            saveFile = new File(saveFile + ".aegf");
        projectPath = saveFile;
        // create save file
        try(var zip = new ZipOutputStream(new FileOutputStream(projectPath))){
            var config = new JSONObject();
            config.put("project_name", projectName);
            getSingleton(Resources.class).saveResources(zip, config);
            zip.putNextEntry(new ZipEntry("game.config"));
            zip.write(config.toString(2).getBytes());
        } catch (Exception e){
            JOptionPane.showMessageDialog(
                    this,
                    "There was a problem saving the project!",
                    "Error saving project",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        var path = saveFile.getPath();
        getSingleton(Config.class).change("recent_projects", Collections.<String>emptyList(), list -> {
            list.remove(path);
            list.add(0, path);
            return list;
        });
        menuBar.genRecentProjectsList();
        projectChanged = false;
        updateTitle();
        return true;
    }

    public void closeProject(){
        if(!checkForSaving())
            return;
        cleanProject();
    }

    private File chooseGameFile(boolean open){
        var filter = new FileNameExtensionFilter("Alundra Engine Game File (.aegf)", "aegf");
        JFileChooser fileChooser = new JFileChooser(getSingleton(Config.class).get("chooser_project_path", (String) null));
        fileChooser.setFileFilter(filter);
        if(open)
            fileChooser.showOpenDialog(this);
        else
            fileChooser.showSaveDialog(this);
        var file = fileChooser.getSelectedFile();
        if(file != null)
            getSingleton(Config.class).set("chooser_project_path", file.getParent());
        return file;
    }

    public void exit() {
        if(checkForSaving())
            dispose();
    }

    private WindowAdapter setupWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(checkForSaving())
                    dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                var config = getSingleton(Config.class);
                var location = getBounds();
                var windowExtended = getExtendedState() == MAXIMIZED_BOTH;
                config.set("window_extended", windowExtended);
                config.set("window_x", location.x);
                config.set("window_y", location.y);
                config.set("window_width", location.width);
                config.set("window_height", location.height);
                config.saveConfig();
            }

        };
    }

    public void setProjectChanged(){
        projectChanged = true;
        updateTitle();
    }
}
