package de.sunnix.srpge.editor.window;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.Variables;
import de.sunnix.srpge.editor.lang.Language;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.copyobjects.ICopyObject;
import de.sunnix.srpge.editor.window.evaluation.EvaluationRegistry;
import de.sunnix.srpge.editor.window.evaluation.NumberCondition;
import de.sunnix.srpge.editor.window.evaluation.NumberVariableProvider;
import de.sunnix.srpge.editor.window.mapview.*;
import de.sunnix.srpge.editor.window.menubar.MenuBar;
import de.sunnix.srpge.editor.window.object.components.ComponentRegistry;
import de.sunnix.srpge.editor.window.object.components.PhysicComponent;
import de.sunnix.srpge.editor.window.object.components.RenderComponent;
import de.sunnix.srpge.editor.window.object.events.*;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.tileset.TilesetTabView;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.audio.OpenALContext;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

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
    private ObjectListView objectListView;
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
    private float[] startMapPosition = new float[3];
    @Getter
    private boolean showGrid = true;

    @Getter
    @Setter
    private GameObject player;

    @Getter
    private final UndoManager undoManager;

    @Getter
    @Setter
    private ICopyObject copyObject;

    public Window(){
        super();

        try(var stream = getClass().getResourceAsStream("/de/sunnix/srpge/editor/window/icons/misc/editor_icon.png")){
            var icon = ImageIO.read(stream);
            setIconImage(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initSingletons();
        registerEvents();
        registerComponents();
        registerEvaluation();
        var config = getSingleton(Config.class);

        var lang = config.get("language", Locale.getDefault().getCountry().toLowerCase());
        config.set("language", lang);
        Language.setupConfig(config);
        Language.setLanguage(Arrays.asList(Language.getLanguages()).contains(lang) ? lang : "en");
        Language.setUseEnglishForMissing(config.get("en_fallback", true));

        setLayout(new BorderLayout());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setJMenuBar(menuBar = new MenuBar(this));
        undoManager = menuBar.getUndoManager();
        var wl = setupWindowListener();
        addWindowListener(wl);
        addWindowStateListener(wl);

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

        setupPlayer();

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

    private void registerEvents(){
        EventRegistry.registerEvent("move", getString("event.move.name"), MoveEvent::new);
        EventRegistry.registerEvent("wait", getString("event.wait.name"), WaitEvent::new);
        EventRegistry.registerEvent("message", getString("event.msg.name"), MessageEvent::new);
        EventRegistry.registerEvent("playsound", getString("event.play_sound.name"), PlaySoundEvent::new);
        EventRegistry.registerEvent("script-lua", "Lua Script (Experimental)", LuaScriptEvent::new);
        EventRegistry.registerEvent("global_color_tint", "Global Color Tint", GlobalColorTintEvent::new);
        EventRegistry.registerEvent("teleport", "Teleport", TeleportEvent::new);
        EventRegistry.registerEvent("look", "Look at", LookEvent::new);
        EventRegistry.registerEvent("camera", "Camera", CameraEvent::new);
        EventRegistry.registerEvent("change_state", "Change State", ChangeStateEvent::new);
        EventRegistry.registerEvent("change_var", "Change Variable", ChangeVariableEvent::new);
        EventRegistry.registerEvent("change_tile", "Change Tile", ChangeTileEvent::new);
    }

    private void registerComponents(){
        ComponentRegistry.registerComponent("render", "Renderer", RenderComponent::new);
        ComponentRegistry.registerComponent("physic", "Physic", PhysicComponent::new);
    }

    private void registerEvaluation(){
        EvaluationRegistry.registerCondition("number", NumberCondition::new);
        EvaluationRegistry.registerProvider("num_var", NumberVariableProvider::new);
    }

    private void setupViews(){
        add(toolbar = new Toolbar(this), BorderLayout.NORTH);

        var centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel.setLeftComponent(new JScrollPane(tilesetView = new TilesetTabView(this)));
        centerPanel.setRightComponent(mapTabsView = new MapTabsView(this));
        add(centerPanel, BorderLayout.CENTER);

        var dataPane = new JPanel(new BorderLayout());
        var tmpPanel = new JPanel(new BorderLayout());
        tmpPanel.add(propertiesView = new PropertiesView(this), BorderLayout.NORTH);
        var scroll = new JScrollPane(tmpPanel);
        scroll.setPreferredSize(new Dimension(0, 300));
        dataPane.add(scroll, BorderLayout.NORTH);
        var listsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listsPanel.setTopComponent(mapListView = new MapListView(this));
        listsPanel.setBottomComponent(objectListView = new ObjectListView(this));
        dataPane.add(listsPanel, BorderLayout.CENTER);
        add(dataPane, BorderLayout.EAST);

        var config = getSingleton(Config.class);
        var mainSplitLocation = config.get("main_split_location", -1);
        var rightSplitLocation = config.get("right_split_location", -1);
        if(mainSplitLocation >= 0)
            centerPanel.setDividerLocation(mainSplitLocation);
        if(rightSplitLocation >= 0)
            listsPanel.setDividerLocation(rightSplitLocation);

        addClosingAction(conf -> {
            conf.set("main-split-location", centerPanel.getDividerLocation());
            conf.set("right-split-location", listsPanel.getDividerLocation());
        });
    }

    private MapViewModule[] genModules(){
        return new MapViewModule[]{
                new SelectTileModule(this),
                new TopDrawModule(this),
                new WallDrawModule(this),
                new ObjectModule(this)
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
            title.append(Objects.requireNonNullElse(projectName, getString("name.new_project")));
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
        startMapPosition = new float[3];
        getSingleton(Resources.class).reset();
        getSingleton(GameData.class).reset();
        setProjectOpen(false);
        updateTitle();
        undoManager.discardAllEdits();
        copyObject = null;
        Variables.reset();
    }

    public void setProjectOpen(boolean projectOpen) {
        this.projectOpen = projectOpen;
        menuBar.enableProjectOptions(projectOpen);
        if(projectOpen){
            mapListView.loadMapList();
            mapListView.setEnabled(true);
        } else {
            mapListView.close();
            objectListView.close();
            mapTabsView.close();
            tilesetView.close();
        }
    }

    public boolean checkForSaving(){
        if(projectOpen && projectChanged){
            var option = JOptionPane.showConfirmDialog(this,
                    getString("dialog.unsaved_changes.text"),
                    getString("dialog.unsaved_changes.title"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            return option != JOptionPane.CLOSED_OPTION && option != JOptionPane.CANCEL_OPTION && (option != JOptionPane.YES_OPTION || saveProject(false));
        }
        return true;
    }

    public void newProject() {
        if(!checkForSaving())
            return;
        DialogUtils.showLoadingDialog(this, getString("dialog.loading.setup_project"), dialog -> {
            cleanProject();
            dialog.addProgress(80);
            var input = (String) JOptionPane.showInputDialog(this, getString("dialog.loading.setup_project_name"), getString("menu.file.new_project"), JOptionPane.PLAIN_MESSAGE, null, null, getString("name.new_project"));
            if(input == null)
                return;
            projectName = input.isEmpty() ? null : input;
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
                    getString("dialog.file_not_found.text", file),
                    getString("dialog.file_not_found.title"),
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
        return (boolean) DialogUtils.showLoadingDialog(this, getString("dialog.loading.load_game_file"), dialog -> {
            dialog.setMaxProgress(1000 + 500 + 5000 + 3500);
            try (var zip = new ZipFile(file)) {
                BetterJSONObject config;
                try {
                    config = new BetterJSONObject(new String(zip.getInputStream(new ZipEntry("game.config")).readAllBytes()));
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            getString("dialog.load_game_file.missing_config.text"),
                            getString("dialog.load_game_file.missing_config.title"),
                            JOptionPane.ERROR_MESSAGE
                    );
                    return false;
                }
                dialog.addProgress(1000);
                var version = Arrays.stream(config.get("editor_version", "0.0").split("\\.")).mapToInt(Integer::parseInt).toArray();
                if (version[0] != Core.MAJOR_VERSION) {
                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, getString("dialog.load_game_file.version_conflict.major.text"), getString("dialog.load_game_file.version_conflict.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
                        return false;
                } else if (version[1] > Core.MINOR_VERSION) {
                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, getString("dialog.load_game_file.version_conflict.minor.text"), getString("dialog.load_game_file.version_conflict.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
                        return false;
                }
                projectName = config.get("project_name", getString("name.unnamed_project"));
                startMap = config.get("start_map", -1);
                startMapPosition = config.getFloatArr("start_map_pos", 3);
                dialog.addProgress(500);
                // load global variables
                var stream = zip.getInputStream(new ZipEntry("res/variables"));
                if(stream != null){
                    Variables.load(new DataSaveObject().load(stream));
                    stream.close();
                } else
                    Variables.load(new DataSaveObject());

                getSingleton(Resources.class).loadResources(dialog, 5000, zip, version);
                getSingleton(GameData.class).loadData(dialog, 3500, zip, version);

                if(version[1] > 5)
                    try (var pStream = zip.getInputStream(new ZipEntry("player"))){
                        loadPlayerData(new DataSaveObject().load(pStream), version);
                    } catch (NullPointerException e){
                        System.err.println("Playerdata not found!");
                    }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        getString("dialog.load_game_file.problem_loading_project.text", e.getMessage()),
                        getString("dialog.load_game_file.problem_loading_project.title"),
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
        if(projectName == null) {
            String input = "";
            while(input != null) {
                input = (String) JOptionPane.showInputDialog(this, getString("dialog.loading.setup_project_name"), getString("menu.file.save_project"), JOptionPane.PLAIN_MESSAGE, null, null, input);
                if(input != null && !input.isEmpty())
                    break;
                JOptionPane.showMessageDialog(
                        this,
                        getString("dialog.name_cant_be_empty.text"),
                        getString("dialog.name_cant_be_empty.title"),
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
        if(!saveFile.toString().endsWith(".sgf") && !saveFile.toString().endsWith(".aegf"))
            saveFile = new File(saveFile + ".sgf");
        projectPath = saveFile;

        var tmpFile = new File(saveFile.getParent(), "_" + saveFile.getName() + ".tmp");

        if(!saveGameFile(tmpFile))
            return false;

        try {
            Files.move(tmpFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e){
            JOptionPane.showMessageDialog(
                    this,
                    getString("dialog.problem_writing_file"),
                    getString("dialog.save_file_failed"),
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
        return (boolean) DialogUtils.showLoadingDialog(this, getString("dialog.loading.saving_project"), dialog -> {
            dialog.setMaxProgress(1000 + 500 + 5000 + 3500);
            try(var zip = new ZipOutputStream(new FileOutputStream(file))){
                var config = new JSONObject();
                dialog.addProgress(1000);
                config.put("project_name", projectName);
                config.put("start_map", startMap);
                config.put("start_map_pos", startMapPosition);
                config.put("editor_version", Core.VERSION);
                dialog.addProgress(500);
                // save global variables
                zip.putNextEntry(new ZipEntry("res/variables"));
                var varDSO = Variables.save(new DataSaveObject());
                var byteOutput = new ByteArrayOutputStream();
                varDSO.save(byteOutput);
                zip.write(byteOutput.toByteArray());
                byteOutput.close();

                getSingleton(Resources.class).saveResources(dialog, 5000, zip);
                getSingleton(GameData.class).saveData(dialog, 3500, zip);

                zip.putNextEntry(new ZipEntry("player"));
                zip.write(savePlayerData());

                zip.putNextEntry(new ZipEntry("game.config"));
                zip.write(config.toString(2).getBytes());
            } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        getString("dialog.problem_saving_project"),
                        getString("dialog.save_file_failed"),
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
        var filter = new FileNameExtensionFilter("Sunnix's Game File (.sgf, .aegf)", "sgf", "aegf");
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

    private void setupPlayer(){
        player = new GameObject(999, 0, 0, 0);
        player.setName("Player");
        loadDefaultPlayerComponents();
    }

    private void loadPlayerData(DataSaveObject dso, int[] version){
        player.load(dso, version);
        player.setName("Player");
        loadDefaultPlayerComponents();
    }

    private byte[] savePlayerData() {
        try (var stream = new ByteArrayOutputStream()) {
            player.save(new DataSaveObject()).save(stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDefaultPlayerComponents(){
        if(!player.hasComponent(RenderComponent.class))
            player.getComponents().add(new RenderComponent());
    }

    /**
     * Used to make a copy function in mapview from shortcut Ctrl+C or menu Copy
     * @return if copying was successfully
     */
    public ICopyObject onCopy() {
        if(mapView != null) {
            var copy = mapView.onCopy();
            if(copy != null)
                copyObject = copy;
            return copy;
        }
        return null;
    }

    public void onPaste() {
        if(copyObject != null)
            copyObject.paste();
    }

    public void setStart(int mapID, float x, int y, float z) {
        setStartMap(mapID);
        startMapPosition[0] = x;
        startMapPosition[1] = y;
        startMapPosition[2] = z;
        mapListView.repaint();
        setProjectChanged();
    }
}
