package de.sunnix.srpge.editor.window;

import de.sunnix.srpge.editor.Main;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.util.FunctionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.JarException;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class Toolbar extends JToolBar {

    private final Window window;

    private GameConsole console;

    private Process gameProcess;

    public Toolbar(Window window){
        setLayout(new FlowLayout(FlowLayout.LEFT));
        this.window = window;
        add(setPlay());
        add(new JSeparator(JSeparator.VERTICAL));
        add(setModes());
        add(new JSeparator(JSeparator.VERTICAL));
        add(setDrawTools());
        add(new JSeparator());
        add(setDrawViewProperties());
        console = new GameConsole();
        setupWindowGlassPane();
    }

    private JPanel setPlay(){
        var panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder((String) null));
        var playBtn = FunctionUtils.createButton(getString("toolbar.play.name"), "toolbar/play.png", this::startGameProcess);
        playBtn.setToolTipText(getString("toolbar.play.tooltip"));
        panel.add(window.menuBar.addProjectDependentComponent(playBtn));
        return panel;
    }

    private JPanel setModes(){
        var panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder((String) null));

        Arrays.stream(createButtonGroup(
                createToolbarButton(getString("toolbar.mode.select.tooltip"), "toolbar/propertieMode.png", "toolbar/propertieMode_s.png", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), l -> selectMode(0)),
                createToolbarButton(getString("toolbar.mode.draw_top.tooltip") ,"toolbar/drawTopMode.png", "toolbar/drawTopMode_s.png", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), l -> selectMode(1)),
                createToolbarButton(getString("toolbar.mode.wall.tooltip") ,"toolbar/addWallMode.png", "toolbar/addWallMode_s.png", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), l -> selectMode(2)),
                createToolbarButton(getString("toolbar.mode.object.tooltip") ,"toolbar/objectMode.png", "toolbar/objectMode_s.png", KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), l -> selectMode(3))
        )).forEach(panel::add);

        return panel;
    }

    private JPanel setDrawTools(){
        var panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder((String) null));

        Arrays.stream(createButtonGroup(
                createToolbarButton(getString("toolbar.tool.single_draw.tooltip") ,"toolbar/draw.png", "toolbar/draw_s.png", null, l -> selectDrawTool(Window.DRAW_TOOL_SINGLE)),
                createToolbarButton(getString("toolbar.tool.multi_draw_rect.tooltip") ,"toolbar/dragFill.png", "toolbar/dragFill_s.png", null, l -> selectDrawTool(Window.DRAW_TOOL_MULTI_RECT)),
                createToolbarButton(getString("toolbar.tool.fill.tooltip") ,"toolbar/fill.png", "toolbar/fill_s.png", null, l -> selectDrawTool(Window.DRAW_TOOL_FILL))
        )).forEach(panel::add);

        return panel;
    }

    private JPanel setDrawViewProperties(){
        var panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder((String) null));

        var showGrid = createToolbarButton(getString("toolbar.option.show_grid.tooltip"), "toolbar/showGrid.png", "toolbar/showGrid_s.png", null, l -> {
            var b = ((AbstractButton)l.getSource());
            b.setSelected(!b.isSelected());
            showGrid(b.isSelected());
        });
        showGrid.setSelected(true);
        panel.add(showGrid);

        return panel;
    }

    private JButton[] createButtonGroup(JButton... buttons){
        for(var button: buttons){
            button.addActionListener(l -> {
                for(var b: buttons){
                    b.setSelected(b.equals(button));
                }
            });
        }
        if(buttons.length > 0)
            buttons[0].setSelected(true);
        return buttons;
    }

    private JButton createToolbarButton(String tooltip, String unselectedIcon, String selectedIcon, KeyStroke shortcut, ActionListener al){
        var button = FunctionUtils.createButton(null, unselectedIcon, selectedIcon, al);
        if(shortcut != null) {
            var inputMap = window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            var actionMap = window.getRootPane().getActionMap();
            var actionName = Integer.toHexString(shortcut.hashCode());
            inputMap.put(shortcut, actionName);
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Arrays.stream(button.getActionListeners()).forEach(l -> l.actionPerformed(e));
                }
            });
        }
        if(tooltip != null)
            button.setToolTipText(shortcut != null ? String.format("%s (%s)", tooltip, KeyEvent.getKeyText(shortcut.getKeyCode())) : tooltip);
        return button;
    }

    private void selectMode(int mode){
        window.setMapModule(mode);
    }

    private void selectDrawTool(int tool){
        window.setDrawTool(tool);
    }

    private void showGrid(boolean show){
        window.setShowGrid(show);
    }

    private void setupWindowGlassPane(){
        var test = new JPanel(new GridBagLayout());
        test.setBackground(new Color(1f, 1f, 1f, .175f));
        var label = new JLabel(getString("name.game_is_running"));
        label.setPreferredSize(new Dimension(350, 125));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(32f));
        label.setBackground(Color.WHITE);
        var gbc = new GridBagConstraints();
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        test.add(label, gbc);
        window.setGlassPane(test);

        label.setOpaque(true);
        test.setOpaque(true);
    }

    private void startGameProcess(ActionEvent e) {
        if(gameProcess != null && gameProcess.isAlive() || !window.isProjectOpen())
            return;
        if(window.getSingleton(GameData.class).getMap(window.getStartMap()) == null) {
            JOptionPane.showMessageDialog(window, getString("toolbar.dialog.no_start_map.text"), getString("toolbar.dialog.no_start_map.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(window.isProjectChanged()) {
            if(JOptionPane.showConfirmDialog(window, getString("toolbar.dialog.project_must_be_saved.text"), getString("dialog.unsaved_changes.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION || !window.saveProject(false))
                return;
        }
        if(Main.isFROM_IDE())
            prepareProcessFromIDE();
        else
            prepareProcessFromJar();
    }

    private void prepareProcessFromIDE() {
        var args = new ArrayList<String>();
        args.add("java"); // command
        args.add("-cp");
        args.add(ManagementFactory.getRuntimeMXBean().getClassPath());
        args.add(Main.class.getName().replaceAll("\\.", "/"));
        args.add("startGame");
        args.add("gameFile=" + window.getProjectPath().toPath());

        var config = window.getSingleton(Config.class).getJSONObject("game");
        if(config.get("show_profiler", false))
            args.add("profiling");
        if(config.get("power_save_mode", false))
            args.add("psm");
        if(config.get("vsync", true))
            args.add("vsync");

        ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(String[]::new));

        startProcess(processBuilder);
    }

    private void prepareProcessFromJar() {
        try {
            var args = new ArrayList<String>();
            args.add("java"); // command
            args.add("-jar");
            args.add(getJarPath());
            args.add("startGame");
            args.add("gameFile=" + window.getProjectPath().toPath());

            var config = window.getSingleton(Config.class).getJSONObject("game");
            if(config.get("show_profiler", false))
                args.add("profiling");
            if(config.get("power_save_mode", false))
                args.add("psm");
            if(config.get("vsync", true))
                args.add("vsync");

            ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(String[]::new));

            startProcess(processBuilder);
        } catch (JarException e) {
            JOptionPane.showMessageDialog(window, getString("toolbar.dialog.start_game_error.jar.text", e.getMessage()), getString("toolbar.dialog.start_game_error.jar.title"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void startProcess(ProcessBuilder processBuilder){
        try {
            processBuilder.redirectErrorStream(true);

            gameProcess = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(gameProcess.getInputStream()));

            window.getGlassPane().setVisible(true);
            window.setEnabled(false);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    console.showConsole();
                    if(!console.isFocused()) {
                        console.requestFocus();
                        console.toFront();
                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        console.write(line);
                    }
                    int exitCode = gameProcess.waitFor();
                    console.write("Game exited with code " + exitCode);
                } catch (Exception e){
                    console.write(e);
                }

                window.setEnabled(true);
                window.getGlassPane().setVisible(false);
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, getString("toolbar.dialog.game_process_error.text", e.getMessage()), getString("toolbar.dialog.game_process_error.title"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static String getJarPath() throws JarException {
        String classPath = System.getProperty("java.class.path");
        Path jarPath = Paths.get(classPath.split(System.getProperty("path.separator"))[0]);
        if (Files.isRegularFile(jarPath)) {
            return jarPath.toString();
        } else {
            throw new JarException("Unable to determine path to JAR file.");
        }
    }

    public void closeProcess() {
        if(console.isVisible())
            console.dispose();
        if(gameProcess != null && gameProcess.isAlive())
            gameProcess.destroy();
    }

    private class GameConsole extends JDialog {

        private JTextArea textArea;
        private JScrollPane scroll;

        public GameConsole(){
            super((JFrame) null, getString("name.game_console"), false);
            setSize(800, 500);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            textArea = new JTextArea();
            textArea.setEditable(false);
            scroll = new JScrollPane(textArea);
            getContentPane().add(scroll, BorderLayout.CENTER);
        }

        public void showConsole(){
            if(isVisible())
                return;
            setLocationRelativeTo(window);
            setVisible(true);
        }

        public void write(String text){
            textArea.append(text + "\n");
            scrollToEnd();
        }

        public void write(Exception e){
            textArea.append(e.getMessage() + "\n");
            var stacks = e.getStackTrace();
            for(var stack: stacks)
                textArea.append(stack.toString() + "\n");
            scrollToEnd();
        }

        private void scrollToEnd(){
            var scrollBar = scroll.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        }

    }

}
