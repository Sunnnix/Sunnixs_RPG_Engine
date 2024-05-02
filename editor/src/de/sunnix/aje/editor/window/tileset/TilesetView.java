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

public class TilesetView extends JPanel {

    private final Window window;
    private final TilesetTabView parent;
    private final String tileset;

    @Setter
    private int selected;

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
                var image = window.getSingleton(Resources.class).tileset_getRaw(tileset);
                if(image == null)
                    return;
                var width = image.getWidth() / 24;
                var height = image.getHeight() / 16;
                var x = e.getX();
                var y = e.getY();
                if(x < 0 || x > image.getWidth() || y < 0 || y > image.getHeight())
                    return;
                var index = x / 24 + (y / 16) * width;
                if(index == selected)
                    return;
                window.setSelectedTile(parent.getSelectedIndex(), index);
            }

        };
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var image = window.getSingleton(Resources.class).tileset_getRaw(tileset);
        if(image == null)
            return;
        setPreferredSize(new Dimension(image.getWidth() / 24 * 24, image.getHeight() / 16 * 16));
        revalidate();
        g.drawImage(image, 0, 0, null);
        if(selected < 0)
            return;
        var width = image.getWidth() / 24;
        var height = image.getHeight() / 16;
        int x, y;
        x = selected % width;
        y = selected / width;
        g.setColor(Color.YELLOW);
        g.drawRect(x * 24, y * 16, 24, 16);
    }

}
