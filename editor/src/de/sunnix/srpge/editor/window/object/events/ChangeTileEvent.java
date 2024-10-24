package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.data.Tile;
import de.sunnix.srpge.editor.util.FunctionUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Tileset;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.util.Tuple.Tuple3;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import static de.sunnix.srpge.engine.util.FunctionUtils.firstOrNull;

/**
 * @see de.sunnix.srpge.engine.ecs.event.ChangeTileEvent event -> ChangeTileEvent
 */
public class ChangeTileEvent extends de.sunnix.srpge.engine.ecs.event.ChangeTileEvent implements IEvent {

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putInt("x", x);
        dso.putInt("y", y);
        dso.putList("changes", changedTextures.stream()
                .map(c -> new DataSaveObject().putByte("w", c.t1()).putBool("fl", c.t2()).putInt("i", c.t3()))
                .toList()
        );
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return varText(String.format("(%s, %s)", x, y)) +
                " changes: " +
                varText(changedTextures.size());
    }

    @Override
    public String getMainColor() {
        return "#ff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Change Tile";
    }

    private Tile tile;

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject currentObject, JPanel content) {
        Tileset ts;
        if(map.getTilesets().length > 0) {
            var name = map.getTilesets()[0];
            ts = window.getSingleton(Resources.class).tilesets.getData(name == null ? null : "default/" + name);
        } else
            ts = null;
        if(x == -1) {
            x = (int) currentObject.getX();
            y = (int) currentObject.getZ();
        }
        var changedTextures = new ArrayList<Tuple3<Byte, Boolean, Integer>>();
        content.setLayout(new GridBagLayout());
        var gbc = FunctionUtils.genDefaultGBC();

        var warning = new JLabel("Changing the Tile will remove the change list!", SwingConstants.CENTER);
        warning.setForeground(Color.RED);
        warning.setFont(warning.getFont().deriveFont(Font.BOLD, 18));
        gbc.gridwidth = 4;
        content.add(warning, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        gbc.weightx = .1;

        content.add(new JLabel("X:"), gbc);
        gbc.gridx++;
        gbc.weightx = .9;
        var xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, map.getWidth() - 1, 1));
        content.add(xSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = .1;
        content.add(new JLabel("Y:"), gbc);
        gbc.gridx++;
        gbc.weightx = .9;
        var ySpinner = new JSpinner(new SpinnerNumberModel(Math.max(0, Math.min(map.getHeight() - 1, x)), 0, map.getHeight() - 1, 1));
        content.add(ySpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.weightx = 1;
        gbc.gridwidth = 2;
        var tilesetSelect = new JPanel(){

            int sX = 0;
            int sY = 0;
            int sH = 1;

            {
                if(ts != null) {
                    setPreferredSize(new Dimension(ts.getWidth() * Core.TILE_WIDTH, ts.getHeight() * Core.TILE_HEIGHT));
                    var mL = new MouseAdapter() {
                        boolean pressed;
                        int startY;
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if(e.getButton() != MouseEvent.BUTTON1)
                                return;
                            pressed = true;
                            sX = e.getX() / Core.TILE_WIDTH;
                            sY = e.getY() / Core.TILE_HEIGHT;
                            sH = 1;
                            startY = sY;
                            repaint();
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON1)
                                pressed = false;
                        }

                        @Override
                        public void mouseDragged(MouseEvent e) {
                            var nextY = Math.max(0, Math.min(ts.getHeight() - 1, e.getY() / Core.TILE_HEIGHT));
                            if(nextY < startY){
                                sY = nextY;
                                sH = startY - nextY + 1;
                            } else {
                                sY = startY;
                                sH = Math.min(ts.getHeight() - sY, Math.max(1, nextY - startY + 1));
                            }
                            repaint();
                        }
                    };
                    addMouseListener(mL);
                    addMouseMotionListener(mL);
                }
            }

            int getSelectedIndex(int height){
                if(ts == null)
                    return 0;
                return sX + (sY + height) * ts.getWidth();
            }

            int getSelectedIndex(){
                return getSelectedIndex(0);
            }

            void setSelectedToIndex(int index){
                if(ts == null || index == -1 || index >= ts.getWidth() * ts.getHeight())
                    return;
                sX = index % ts.getWidth();
                sY = index / ts.getWidth();
                sH = 1;
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(ts == null)
                    return;
                var image = ts.getImage(window);
                if(image == null)
                    return;
                g.drawImage(image, 0, 0, null);
                var TW = Core.TILE_WIDTH;
                var TH = Core.TILE_HEIGHT;
                g.setColor(Color.YELLOW);
                g.drawRect(sX * TW, sY * TH, TW, sH * TH);
            }
        };
        var scroll = new JScrollPane(tilesetSelect);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(260, 367));
        content.add(scroll, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 3;
        gbc.gridy = 1;

        var tilePreviewPanel = new JPanel(new GridBagLayout());
        var tPGBC = FunctionUtils.genDefaultGBC();
        tPGBC.fill = GridBagConstraints.NONE;
        tPGBC.anchor = GridBagConstraints.NORTHWEST;
        var groundPreview = new JPanel(){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                if(ts != null) {
                    var ml = new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (e.getButton() != MouseEvent.BUTTON2)
                                return;
                            tilesetSelect.setSelectedToIndex(tile.getGroundTex()[e.isShiftDown() ? 3 : 1]);
                        }
                    };
                    addMouseListener(ml);
                }
            }
            void reload(){
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(ts == null)
                    return;
                var image = ts.getImage(window);
                if(image == null)
                    return;
                var gI = tile.getGroundTex();
                int TW = Core.TILE_WIDTH;
                int TH = Core.TILE_HEIGHT;
                int DW = TW * 2;
                int DH = TH * 2;
                int x, y;
                int tex = gI[1];
                if(tex >= 0) {
                    x = tex % ts.getWidth() * TW;
                    y = tex / ts.getWidth() * TH;
                    g.drawImage(image, 0, 0, DW, DH, x, y, x + TW, y + TH, null);
                }
                tex = gI[3];
                if(tex >= 0) {
                    x = tex % ts.getWidth() * TW;
                    y = tex / ts.getWidth() * TH;
                    g.drawImage(image, 0, 0, DW, DH, x, y, x + TW, y + TH, null);
                }
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, DW, DH);
            }
        };
        groundPreview.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2));
        tilePreviewPanel.add(groundPreview, tPGBC);
        tPGBC.gridx++;

        var groundEdit = new JPanel(){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                if(ts != null) {
                    var ml = new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            switch (e.getButton()){
                                case MouseEvent.BUTTON2 -> {
                                    var change = firstOrNull(changedTextures, t -> t.t1() == -1 && t.t2() == (!e.isShiftDown()));
                                    if(change != null)
                                        tilesetSelect.setSelectedToIndex(change.t3());
                                    else
                                        tilesetSelect.setSelectedToIndex(tile.getGroundTex()[e.isShiftDown() ? 3 : 1]);
                                }
                                case MouseEvent.BUTTON1 -> {
                                    var change = firstOrNull(changedTextures, t -> t.t1() == -1 && t.t2() == (!e.isShiftDown()));
                                    changedTextures.remove(change);
                                    changedTextures.add(new Tuple3<>((byte) -1, !e.isShiftDown(), tilesetSelect.getSelectedIndex()));
                                    repaint();
                                }
                                case MouseEvent.BUTTON3 -> {
                                    var change = firstOrNull(changedTextures, t -> t.t1() == -1 && t.t2() == (!e.isShiftDown()));
                                    changedTextures.remove(change);
                                    repaint();
                                }
                            }
                        }
                    };
                    addMouseListener(ml);
                }
            }
            void reload(){
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(ts == null)
                    return;
                var image = ts.getImage(window);
                if(image == null)
                    return;
                var gI = tile.getGroundTex();
                int TW = Core.TILE_WIDTH;
                int TH = Core.TILE_HEIGHT;
                int DW = TW * 2;
                int DH = TH * 2;
                int x, y;
                var change = firstOrNull(changedTextures, t -> t.t1() == -1 && t.t2());
                int tex = change == null ? gI[1] : change.t3();
                if(tex >= 0) {
                    x = tex % ts.getWidth() * TW;
                    y = tex / ts.getWidth() * TH;
                    g.drawImage(image, 0, 0, DW, DH, x, y, x + TW, y + TH, null);
                }
                change = firstOrNull(changedTextures, t -> t.t1() == -1 && !t.t2());
                tex = change == null ? gI[3] : change.t3();
                if(tex >= 0) {
                    x = tex % ts.getWidth() * TW;
                    y = tex / ts.getWidth() * TH;
                    g.drawImage(image, 0, 0, DW, DH, x, y, x + TW, y + TH, null);
                }
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, DW, DH);
            }
        };
        groundEdit.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2));
        tilePreviewPanel.add(groundEdit, tPGBC);
        tPGBC.gridx = 0;
        tPGBC.gridy++;

        var wallPreview = new JPanel(){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                if(ts != null) {
                    var ml = new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            var y = tile.getWallHeight() - 1 - (e.getY() / Core.TILE_HEIGHT / 2);
                            if(e.getButton() != MouseEvent.BUTTON2 || y < 0 || y >= tile.getWallHeight())
                                return;
                            tilesetSelect.setSelectedToIndex(tile.getWallTex(y)[e.isShiftDown() ? 3 : 1]);
                        }
                    };
                    addMouseListener(ml);
                }
            }
            void reload() {
                setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2 * (tile.getWallHeight() - 1)));
                revalidate();
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(ts == null)
                    return;
                var image = ts.getImage(window);
                if(image == null)
                    return;
                var height = tile.getWallHeight();
                g.setColor(Color.BLACK);
                for(var i = 0; i < height; i++) {
                    var gI = tile.getWallTex(i);
                    int TW = Core.TILE_WIDTH;
                    int TH = Core.TILE_HEIGHT;
                    int DW = TW * 2;
                    int DH = TH * 2;
                    int x, y;
                    int tex = gI[1];
                    if (tex >= 0) {
                        x = tex % ts.getWidth() * TW;
                        y = tex / ts.getWidth() * TH;
                        g.drawImage(image, 0, (height - i - 1) * DH, DW, (height - i) * DH, x, y, x + TW, y + TH, null);
                    }
                    tex = gI[3];
                    if (tex >= 0) {
                        x = tex % ts.getWidth() * TW;
                        y = tex / ts.getWidth() * TH;
                        g.drawImage(image, 0, (height - i - 1) * DH, DW, (height - i) * DH, x, y, x + TW, y + TH, null);
                    }
                    g.drawRect(0, (height - i - 1) * DH, DW, DH);
                }
            }
        };
        wallPreview.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2 * 100));
        scroll = new JScrollPane(wallPreview);
        var vScrollbar = scroll.getVerticalScrollBar();
        vScrollbar.setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2 + 2, 380));
        tilePreviewPanel.add(scroll, tPGBC);
        tPGBC.gridx++;

        var wallEdit = new JPanel(){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                if(ts != null) {
                    var ml = new MouseAdapter() {
                        boolean pressPri, pressSec;
                        @Override
                        public void mousePressed(MouseEvent e) {
                            var y = tile.getWallHeight() - 1 - (e.getY() / Core.TILE_HEIGHT / 2);
                            if(y < 0 || y >= tile.getWallHeight())
                                return;
                            switch (e.getButton()){
                                case MouseEvent.BUTTON2 -> {
                                    var change = firstOrNull(changedTextures, t -> t.t1() == y && t.t2() == (!e.isShiftDown()));
                                    if(change != null)
                                        tilesetSelect.setSelectedToIndex(change.t3());
                                    else
                                        tilesetSelect.setSelectedToIndex(tile.getWallTex(y)[e.isShiftDown() ? 3 : 1]);
                                }
                                case MouseEvent.BUTTON1 -> {
                                    pressPri = true;
                                    pressSec = false;
                                    for(var i = 0; i < tilesetSelect.sH && y - i > 0; i++) {
                                        var nY = y - i;
                                        var change = firstOrNull(changedTextures, t -> t.t1() == nY && t.t2() == (!e.isShiftDown()));
                                        changedTextures.remove(change);
                                        changedTextures.add(new Tuple3<>((byte) nY, !e.isShiftDown(), tilesetSelect.getSelectedIndex(i)));
                                    }
                                    repaint();
                                }
                                case MouseEvent.BUTTON3 -> {
                                    pressPri = false;
                                    pressSec = true;
                                    var change = firstOrNull(changedTextures, t -> t.t1() == y && t.t2() == (!e.isShiftDown()));
                                    changedTextures.remove(change);
                                    repaint();
                                }
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if(e.getButton() == MouseEvent.BUTTON1)
                                pressPri = false;
                            else if(e.getButton() == MouseEvent.BUTTON3)
                                pressSec = false;
                        }

                        @Override
                        public void mouseDragged(MouseEvent e) {
                            var y = tile.getWallHeight() - 1 - (e.getY() / Core.TILE_HEIGHT / 2);
                            if(y < 0 || y >= tile.getWallHeight())
                                return;
                            if(pressPri){
                                for(var i = 0; i < tilesetSelect.sH && y - i > 0; i++) {
                                    var nY = y - i;
                                    var change = firstOrNull(changedTextures, t -> t.t1() == nY && t.t2() == (!e.isShiftDown()));
                                    changedTextures.remove(change);
                                    changedTextures.add(new Tuple3<>((byte) nY, !e.isShiftDown(), tilesetSelect.getSelectedIndex(i)));
                                }
                                repaint();
                            } else if(pressSec){
                                var change = firstOrNull(changedTextures, t -> t.t1() == y && t.t2() == (!e.isShiftDown()));
                                changedTextures.remove(change);
                                repaint();
                            }
                        }
                    };
                    addMouseListener(ml);
                    addMouseMotionListener(ml);
                }
            }
            void reload() {
                setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2 * (tile.getWallHeight() - 1)));
                revalidate();
                repaint();
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(ts == null)
                    return;
                var image = ts.getImage(window);
                if(image == null)
                    return;
                var height = tile.getWallHeight();
                g.setColor(Color.BLACK);
                for(var i = 0; i < height; i++) {
                    var gI = tile.getWallTex(i);
                    int TW = Core.TILE_WIDTH;
                    int TH = Core.TILE_HEIGHT;
                    int DW = TW * 2;
                    int DH = TH * 2;
                    int x, y;
                    int currentHeight = i;
                    var change = firstOrNull(changedTextures, t -> t.t1() == currentHeight && t.t2());
                    int tex = change == null ? gI[1] : change.t3();
                    if (tex >= 0) {
                        x = tex % ts.getWidth() * TW;
                        y = tex / ts.getWidth() * TH;
                        g.drawImage(image, 0, (height - i - 1) * DH, DW, (height - i) * DH, x, y, x + TW, y + TH, null);
                    }
                    change = firstOrNull(changedTextures, t -> t.t1() == currentHeight && !t.t2());
                    tex = change == null ? gI[3] : change.t3();
                    if (tex >= 0) {
                        x = tex % ts.getWidth() * TW;
                        y = tex / ts.getWidth() * TH;
                        g.drawImage(image, 0, (height - i - 1) * DH, DW, (height - i) * DH, x, y, x + TW, y + TH, null);
                    }
                    g.drawRect(0, (height - i - 1) * DH, DW, DH);
                }
            }
        };
        wallEdit.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2, Core.TILE_HEIGHT * 2 * 100));
        scroll = new JScrollPane(wallEdit);
        scroll.setVerticalScrollBar(vScrollbar);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(Core.TILE_WIDTH * 2 + 15, 380));
        tilePreviewPanel.add(scroll, tPGBC);

        gbc.gridheight = 3;
        content.add(tilePreviewPanel, gbc);

        // Listeners
        var cL = (ChangeListener) l -> {
            changedTextures.clear();
            tile = map.getTile(((Number)xSpinner.getValue()).intValue(), ((Number)ySpinner.getValue()).intValue());
            groundPreview.reload();
            groundEdit.reload();
            wallPreview.reload();
            wallEdit.reload();
        };
        xSpinner.addChangeListener(cL);
        ySpinner.addChangeListener(cL);

        // Set values
        xSpinner.setValue(Math.max(0, Math.min(map.getWidth() - 1, x)));
        ySpinner.setValue(Math.max(0, Math.min(map.getWidth() - 1, y)));
        changedTextures.addAll(this.changedTextures.stream().map(t -> new Tuple3<>(t.t1(), t.t2(), t.t3())).toList());

        return () -> {
            this.x = ((Number)xSpinner.getValue()).intValue();
            this.y = ((Number)ySpinner.getValue()).intValue();
            this.changedTextures = changedTextures;
        };
    }

}
