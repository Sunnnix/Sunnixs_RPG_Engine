package de.sunnix.aje.editor.window.tileset;

import de.sunnix.aje.editor.util.FunctionUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static de.sunnix.aje.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.aje.editor.window.Window.TILE_WIDTH;

public class TilesetView extends JPanel {

    private final Window window;
    private final TilesetTabView parent;
    private final String tileset;

    @Setter
    private int[] selected = new int[] { -1, 1, 1};

    public TilesetView(Window window, TilesetTabView parent, String tileset) {
        this.window = window;
        this.parent = parent;
        this.tileset = tileset;

        addMouseListener(genMouseListener());
    }

    private MouseListener genMouseListener() {
        return new MouseAdapter() {


            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    var image = window.getSingleton(Resources.class).tileset_getRaw(tileset);
                    if (image == null)
                        return;
                    var width = image.getWidth() / TILE_WIDTH;
                    var height = image.getHeight() / TILE_HEIGHT;
                    var x = e.getX();
                    var y = e.getY();
                    if (x < 0 || x > image.getWidth() || y < 0 || y > image.getHeight())
                        return;
                    var index = x / TILE_WIDTH + (y / TILE_HEIGHT) * width;
                    window.setSelectedTile(parent.getSelectedIndex(), index, 1, 1);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {

            }
        };
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var image = window.getSingleton(Resources.class).tileset_getRaw(tileset);
        if(image == null)
            return;
        setPreferredSize(new Dimension(image.getWidth() / TILE_WIDTH * TILE_WIDTH, image.getHeight() / TILE_HEIGHT * TILE_HEIGHT));
        revalidate();
        g.drawImage(image, 0, 0, null);
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
