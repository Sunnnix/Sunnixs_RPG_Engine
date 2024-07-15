package de.sunnix.srpge.editor.window.menubar;

import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.components.RenderComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.lang.Language.removeLanguagePack;

public class PlayerSpriteManager extends JDialog {

    private boolean loopShouldStop;

    public PlayerSpriteManager(Window window){
        super(window, "Edit player sprites", true);
        var mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setContentPane(mainPanel);

        var player = window.getPlayer();
        var component = player.getComponent(RenderComponent.class);
        var centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setPreferredSize(new Dimension(180, 250));
        var loopFunction = component.createView(window, player, centerPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        var loop = new Thread(() -> {
            while(!loopShouldStop){
                try {
                    loopFunction.run();
                    Thread.sleep(16, 666666);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Player sprite animation loop");
        loop.setDaemon(true);
        loop.start();

        addWindowListener(creatwWindowListener());
        
        setResizable(false);
        pack();
        setLocationRelativeTo(window);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private WindowListener creatwWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loopShouldStop = true;
            }
        };
    }

}
