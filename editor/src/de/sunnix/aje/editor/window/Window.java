package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.util.DialogUtils;
import de.sunnix.aje.engine.audio.OpenALContext;
import de.sunnix.aje.editor.window.mapview.*;
import de.sunnix.aje.editor.window.menubar.MenuBar;
import de.sunnix.aje.editor.window.resource.Resources;
import de.sunnix.aje.editor.window.tileset.TilesetTabView;
import de.sunnix.aje.engine.Core;
import de.sunnix.aje.engine.util.BetterJSONObject;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static de.sunnix.aje.editor.util.Texts.WINDOW_NAME;

public class Window extends JFrame {

    public static final int TILE_WIDTH = 24;
    public static final int TILE_HEIGHT = 16;
    public static final int DRAW_TOOL_SINGLE = 0;
    public static final int DRAW_TOOL_MULTI_RECT = 1;
    public static final int DRAW_TOOL_FILL = 2;
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    public final MenuBar menuBar;
    @Getter
    private MapTabsView mapTabsView;
    @Getter
    private MapView mapView;
    @Getter
    private MapListView mapListView;
    @Getter
    private TilesetTabView tilesetView;
    @Getter
    private PropertiesView propertiesView;
    @Getter
    private File projectPath;
    @Getter
    private String projectName;
    @Getter
    private boolean projectOpen;
    @Getter
    private boolean projectChanged;
    @Getter
    private final JLabel info;
    private Toolbar toolbar;
    private final List<Consumer<Config>> onCloseActions = new ArrayList<>();
    private int currentMapModule;
    private final MapViewModule[] mapModules;
    private final NullModule nullModule = new NullModule(this);
    @Setter
    @Getter
    private int drawTool = DRAW_TOOL_SINGLE;
    @Getter
    @Setter
    private int startMap = -1;
    @Getter
    private boolean showGrid = true;

    public Window(){
        super();
        setLayout(new BorderLayout());
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
        addClosingAction(conf -> {
            var location = getBounds();
            var windowExtended = getExtendedState() == MAXIMIZED_BOTH;
            conf.set("window_extended", windowExtended);
            conf.set("window_x", location.x);
            conf.set("window_y", location.y);
            conf.set("window_width", location.width);
            conf.set("window_height", location.height);
        });
        setupViews();

        info = new JLabel();
        info.setBorder(BorderFactory.createTitledBorder((String) null));
        info.setBackground(getBackground().darker());
        info.setPreferredSize(new Dimension(0, 25));
        add(info, BorderLayout.SOUTH);

        mapModules = genModules();

        OpenALContext.setUp();

        setProjectOpen(false);
        updateTitle();
        setVisible(true);
    }

    private void initSingletons(){
        var config = new Config();
        config.loadConfig();
        singletons.put(Config.class, config);
        singletons.put(Resources.class, new Resources());
        singletons.put(GameData.class, new GameData());
    }

    private void setupViews(){
        add(toolbar = new Toolbar(this), BorderLayout.NORTH);

        var centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel.setLeftComponent(new JScrollPane(tilesetView = new TilesetTabView(this)));
        centerPanel.setRightComponent(mapTabsView = new MapTabsView(this));
        add(centerPanel, BorderLayout.CENTER);

        var dataPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        dataPane.setTopComponent(new JScrollPane(propertiesView = new PropertiesView(this)));
        dataPane.setBottomComponent(mapListView = new MapListView(this));
        add(dataPane, BorderLayout.EAST);

        var config = getSingleton(Config.class);
        var mainSplitLocation = config.get("main_split_location", -1);
        var rightSplitLocation = config.get("right_split_location", -1);
        if(mainSplitLocation >= 0)
            centerPanel.setDividerLocation(mainSplitLocation);
        if(rightSplitLocation >= 0)
            dataPane.setDividerLocation(rightSplitLocation);

        addClosingAction(conf -> {
            conf.set("main-split-location", centerPanel.getDividerLocation());
//            conf.set("right-split-location", dataPane.getDividerLocation());
        });
    }

    private MapViewModule[] genModules(){
        return new MapViewModule[]{
                new SelectTileModule(this),
                new TopDrawModule(this),
                new WallDrawModule(this)
        };
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

    public void addClosingAction(Consumer<Config> action){
        onCloseActions.add(action);
    }

    /**
     * Empties all Data from the Window
     */
    private void cleanProject(){
        projectName = null;
        projectPath = null;
        projectChanged = false;
        startMap = -1;
        getSingleton(Resources.class).reset();
        getSingleton(GameData.class).reset();
        setProjectOpen(false);
        updateTitle();
    }

    public void setProjectOpen(boolean projectOpen) {
        this.projectOpen = projectOpen;
        menuBar.enableProjectOptions(projectOpen);
        if(projectOpen){
            mapListView.loadMapList();
            mapListView.setEnabled(true);
        } else {
            mapListView.close();
            mapTabsView.close();
            tilesetView.close();
        }
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
        DialogUtils.showLoadingDialog(this, "Setup new Project", dialog -> {
            cleanProject();
            dialog.addProgress(80);
            var input = (String) JOptionPane.showInputDialog(this, "Write a name for the id of the image:", "Create new Image Resource", JOptionPane.PLAIN_MESSAGE, null, null, "New Project");
            if(input == null)
                return;
            projectName = input;
            dialog.addProgress(10);
            setProjectOpen(true);
            setProjectChanged();
            dialog.addProgress(10);
        });
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

        if(!loadGameFile(file))
            return;

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

    private boolean loadGameFile(File file){
        return (boolean) DialogUtils.showLoadingDialog(this, "Load game file...", dialog -> {
            dialog.setMaxProgress(1000 + 500 + 5000 + 3500);
            try (var zip = new ZipFile(file)) {
                BetterJSONObject config;
                try {
                    config = new BetterJSONObject(new String(zip.getInputStream(new ZipEntry("game.config")).readAllBytes()));
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "File missing game.config!",
                            "Missing config",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return false;
                }
                dialog.addProgress(1000);
                var version = Arrays.stream(config.get("editor_version", "0.0").split("\\.")).mapToInt(Integer::parseInt).toArray();
                if (version[0] != Core.MAJOR_VERSION) {
                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, """
                            The major versions of the editor and the file are not the same.
                            It is very likely that loading the file will result in errors.
                                                    
                            Proceed anyway?""", "Version conflict!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
                        return false;
                } else if (version[1] > Core.MINOR_VERSION) {
                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, """
                            The version of the file is higher than that of the editor.
                            When the game file is loaded it may be that not all data can be loaded.
                                                    
                            Proceed anyway?""", "Version conflict!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
                        return false;
                }
                projectName = config.get("project_name", "Unnamed Project");
                startMap = config.get("start_map", -1);
                dialog.addProgress(500);
                getSingleton(Resources.class).loadResources(dialog, 5000, zip);
                getSingleton(GameData.class).loadData(dialog, 3500, zip, version);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "There was a problem loading the project!\n" + e.getMessage(),
                        "Error loading project",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
                closeProject();
                return false;
            }
            return true;
        });
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

        var tmpFile = new File(saveFile.getParent(), "_" + saveFile.getName() + ".tmp");

        if(!saveGameFile(tmpFile))
            return false;

        try {
            Files.move(tmpFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e){
            JOptionPane.showMessageDialog(
                    this,
                    "Problem writing file",
                    "Save project failed",
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

    private boolean saveGameFile(File file){
        return (boolean) DialogUtils.showLoadingDialog(this, "Saving project...", dialog -> {
            dialog.setMaxProgress(1000 + 500 + 5000 + 3500);
            try(var zip = new ZipOutputStream(new FileOutputStream(file))){
                var config = new JSONObject();
                dialog.addProgress(1000);
                config.put("project_name", projectName);
                config.put("start_map", startMap);
                config.put("editor_version", Core.VERSION);
                dialog.addProgress(500);
                getSingleton(Resources.class).saveResources(dialog, 5000, zip);
                getSingleton(GameData.class).saveData(dialog, 3500, zip);

                zip.putNextEntry(new ZipEntry("game.config"));
                zip.write(config.toString(2).getBytes());
            } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "There was a problem saving the project!",
                        "Error saving project",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
            return true;
        });
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
                if(!checkForSaving())
                    return;
                toolbar.closeProcess();
                OpenALContext.close();
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                var config = getSingleton(Config.class);
                for(var action: onCloseActions)
                    action.accept(config);
                config.saveConfig();
            }

        };
    }

    public void setProjectChanged(){
        if(projectChanged)
            return;
        projectChanged = true;
        updateTitle();
    }

    public void openMap(int id) {
        mapTabsView.openMap(id);
    }

    public void closeMap(int id) {
        mapTabsView.closeMap(id);
    }

    public void loadMapView(MapView mapView) {
        this.mapView = mapView;
        this.propertiesView.onLoadMap(mapView == null ? null : getSingleton(GameData.class).getMap(mapView.getMapID()));
    }

    public void reloadMap() {
        if(mapView != null)
            mapView.repaint();
    }

    public void reloadTilesetView() {
        tilesetView.reload();
    }

    public void setSelectedTile(int tileset, int index, int width, int height) {
        tilesetView.setSelectedTile(tileset, index, width, height);
        mapView.setSelectedTilesetTile(tileset, index, width, height);
    }

    public void setMapModule(int module){
        currentMapModule = module;
        mapTabsView.repaint();
    }

    public MapViewModule getCurrentMapModule(){
        if(currentMapModule >= mapModules.length)
            return nullModule;
        return mapModules[currentMapModule];
    }

    public void setShowGrid(boolean show){
        showGrid = show;
        mapTabsView.repaint();
    }

}
