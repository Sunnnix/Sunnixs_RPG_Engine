package de.sunnix.srpge.editor.window.tileset;

import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.TilesetPropertie;
import de.sunnix.srpge.engine.Core;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;

public class TilesetView extends JPanel {

    private final Window window;
    private final TilesetTabView parent;
    private final String tileset;

    @Setter
    private int[] selected = new int[] { -1, 1, 1};

    private Thread renderer;
    private long animTime;

    public TilesetView(Window window, TilesetTabView parent, String tileset) {
        this.window = window;
        this.parent = parent;
        this.tileset = tileset;

        var ml = genMouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                var renderer = new Thread(() -> {
                    var config = window.getSingleton(Config.class);
                    var animate = config.get("animate_tiles", false);
                    var animateCheck = System.currentTimeMillis();
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            Thread.sleep(16,666666);
                            if(animateCheck + 1000 < System.currentTimeMillis()){
                                animateCheck = System.currentTimeMillis();
                                animate = config.get("animate_tiles", false);
                            }
                            if(!animate)
                                continue;
                            animTime++;
                            repaint();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                });
                renderer.setDaemon(true);
                renderer.start();
                TilesetView.this.renderer = renderer;
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                renderer.interrupt();
                animTime = 0;
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {

            private int preX, preY;

            private boolean primPress;

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    var tileset = window.getSingleton(Resources.class).tileset_get(TilesetView.this.tileset);
                    if(tileset == null)
                        return;
                    var width = tileset.getWidth();
                    var height = tileset.getHeight();
                    var x = preX = Math.max(Math.min(e.getX() / TILE_WIDTH, width - 1), 0);
                    var y = preY = Math.max(Math.min(e.getY() / TILE_HEIGHT, height - 1), 0);
                    var index = x + y * width;
                    window.setSelectedTile(parent.getSelectedIndex(), index, 1, 1);
                    primPress = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    primPress = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(!primPress)
                    return;
                var tileset = window.getSingleton(Resources.class).tileset_get(TilesetView.this.tileset);
                if(tileset == null)
                    return;
                var width = tileset.getWidth();
                var height = tileset.getHeight();
                var x = Math.max(Math.min(e.getX() / TILE_WIDTH, width - 1), 0);
                var y = Math.max(Math.min(e.getY() / TILE_HEIGHT, height - 1), 0);

                var index = Math.min(preX, x) + Math.min(preY, y) * width;
                var sW = Math.max(Math.abs(preX - x) + 1, 1);
                var sH = Math.max(Math.abs(preY - y) + 1, 1);
                window.setSelectedTile(parent.getSelectedIndex(), index, sW, sH);
            }
        };
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var tileset = window.getSingleton(Resources.class).tileset_get(this.tileset);
        if(tileset == null)
            return;
        var image = tileset.getImage(window);
        if(image == null)
            return;
        setPreferredSize(new Dimension(image.getWidth() / TILE_WIDTH * TILE_WIDTH, image.getHeight() / TILE_HEIGHT * TILE_HEIGHT));
        revalidate();

        for(var x = 0; x < tileset.getWidth(); x++)
            for (int y = 0; y < tileset.getHeight(); y++) {
                var prop = tileset.getProperty(x, y);

                var iX = x;
                var iY = y;

                if(prop.getAnimationParent() != -1 || prop.getAnimation() != null){
                    TilesetPropertie parent;
                    if(prop.getAnimationParent() != -1) {
                        var parentI = prop.getAnimationParent();
                        parent = tileset.getProperty(parentI % tileset.getWidth(), parentI / tileset.getWidth());
                    } else
                        parent = prop;
                    var animation = parent.getAnimation();
                    var animSpeed = parent.getAnimationTempo();
                    var offset = animation.indexOf((short)(x + y * tileset.getWidth()));
                    var index = (int) (((animTime / animSpeed) + offset) % animation.size());
                    var tex = animation.get(index);
                    iX = tex % tileset.getWidth();
                    iY = tex / tileset.getWidth();
                }

                g.drawImage(image, x * Core.TILE_WIDTH, y * Core.TILE_HEIGHT, x * Core.TILE_WIDTH + Core.TILE_WIDTH, y * Core.TILE_HEIGHT + Core.TILE_HEIGHT, iX * Core.TILE_WIDTH, iY * Core.TILE_HEIGHT, iX * Core.TILE_WIDTH + Core.TILE_WIDTH, iY * Core.TILE_HEIGHT + Core.TILE_HEIGHT, null);
            }

        if(selected[0] < 0)
            return;
        var width = image.getWidth() / TILE_WIDTH;
        var height = image.getHeight() / TILE_HEIGHT;
        int x, y;
        x = selected[0] % width;
        y = selected[0] / width;
        g.setColor(Color.YELLOW);
        g.drawRect(x * TILE_WIDTH, y * TILE_HEIGHT, TILE_WIDTH * selected[1], TILE_HEIGHT * selected[2]);
    }

    public void setSelected(int index, int width, int height){
        selected[0] = index;
        selected[1] = width;
        selected[2] = height;
    }

}
