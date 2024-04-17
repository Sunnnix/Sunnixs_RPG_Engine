package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.Main;
import de.sunnix.aje.editor.util.FunctionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarException;

public class Toolbar extends JToolBar {

    private final Window window;

    private GameConsole console;

    private Process gameProcess;

    public Toolbar(Window window){
        this.window = window;
        add(window.menuBar.addProjectDependentComponent(FunctionUtils.createButton("Play", this::startGameProcess)));
        console = new GameConsole();
        setupWindowGlassPane();
    }

    private void setupWindowGlassPane(){
        var test = new JPanel(new GridBagLayout());
        test.setBackground(new Color(1f, 1f, 1f, .175f));
        var label = new JLabel("GAME IS RUNNING");
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
        if(window.isProjectChanged()) {
            if(JOptionPane.showConfirmDialog(window, "All changes must be saved.\nWould you like to save the current changes?", "Unsafed chnages", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION || !window.saveProject(false))
                return;
        }
        if(Main.isFROM_IDE())
            prepareProcessFromIDE();
        else
            prepareProcessFromJar();
    }

    private void prepareProcessFromIDE() {
        String command = "java";
        String arg1 = "-cp";
        String arg2 = ManagementFactory.getRuntimeMXBean().getClassPath();
        String arg3 = Main.class.getName().replaceAll("\\.", "/");
        String arg4 = "startGame";
        String arg5 = "gameFile=" + window.getProjectPath().toPath();

        ProcessBuilder processBuilder = new ProcessBuilder(command, arg1, arg2, arg3, arg4, arg5);

        startProcess(processBuilder);
    }

    private void prepareProcessFromJar() {
        try {
            String command = "java";
            String arg1 = "-jar";
            String arg2 = getJarPath();
            String arg3 = "startGame";
            String arg4 = "gameFile=" + window.getProjectPath().toPath();

            ProcessBuilder processBuilder = new ProcessBuilder(command, arg1, arg2, arg3, arg4);

            startProcess(processBuilder);
        } catch (JarException e){
            JOptionPane.showMessageDialog(window, "The process could not start because the Jar was not found.\n" +
                    "If you are in an IDE, start the editor with the argument \"fromIDE\"", "Couldn't start game process", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(window, "There was a problem with the game process", "Process error", JOptionPane.ERROR_MESSAGE);
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
            super((JFrame) null, "Game Console", false);
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
