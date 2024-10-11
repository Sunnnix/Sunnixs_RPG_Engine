package de.sunnix.srpge.editor.window.mapview;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.copyobjects.ICopyObject;
import de.sunnix.srpge.engine.Core;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.engine.util.FunctionUtils.bitcheck;

public class MapView extends JPanel {

    private final de.sunnix.srpge.editor.window.Window window;
    @Getter
    private final int mapID;
    @Getter
    private float zoom = 1;
    private float offsetX, offsetY;

    private Thread renderer;
    private long animTime;

    public MapView(Window window, int mapID) {
        super(new BorderLayout());
        this.window = window;
        this.mapID = mapID;
        var ml = genMouseListener();
        setFocusable(true);
        addMouseListener(ml);
        addMouseMotionListener(ml);
        addMouseWheelListener(ml);
        addKeyListener(genKeyListener());

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
                MapView.this.renderer = renderer;
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

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        var mapData = window.getSingleton(GameData.class).getMap(mapID);
        if(mapData == null) {
            g.setColor(Color.RED);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 24f));
            var text = getString("view.map.data_not_found");
            g.drawString(text, getWidth() / 2 - g.getFontMetrics().stringWidth(text) / 2, getHeight() / 2);
            return;
        }

        window.getCurrentMapModule().onDraw((Graphics2D) g, MapView.this, mapData, getWidth(), getHeight(), offsetX, offsetY, animTime);
    }

    public void setSelectedTilesetTile(int tileset, int index, int width, int height) {
        window.getSingleton(GameData.class).getMap(mapID).setSelectedTilesetTile(tileset, index, width, height);
    }

    public void setZoom(float zoom){
        var tmp = offsetX / this.zoom;
        tmp *= zoom;
        this.offsetX = tmp;
        tmp = offsetY / this.zoom;
        tmp *= zoom;
        this.offsetY = tmp;

        this.zoom = zoom;
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {
            int preX, preY;
            int preTileX, preTileY;
            int button;

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();
                preX = e.getX();
                preY = e.getY();
                var mod = e.getModifiersEx();
                if((mod & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
                    return;
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                preTileX = tilePos[0];
                preTileY = tilePos[1];

                button = e.getButton();

                if(window.getCurrentMapModule().onMousePresses(MapView.this, mapData, e, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint(0, 0, getWidth(), getHeight());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                preTileX = tilePos[0];
                preTileY = tilePos[1];

                button = e.getButton();

                if(window.getCurrentMapModule().onMouseReleased(MapView.this, mapData, button, e.getModifiersEx(), screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint(0, 0, getWidth(), getHeight());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                window.getCurrentMapModule().onMouseMoved(MapView.this, mapData, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                var x = e.getX();
                var y = e.getY();
                var pX = preX;
                var pY = preY;
                preX = e.getX();
                preY = e.getY();
                var mod = e.getModifiersEx();
                if((mod & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                    offsetX += x - pX;
                    offsetY += y - pY;
                    repaint(0, 0, getWidth(), getHeight());
                    return;
                }
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                var sameTile = preTileX == tilePos[0] && preTileY == tilePos[1];
                preTileX = tilePos[0];
                preTileY = tilePos[1];

                if(window.getCurrentMapModule().onMouseDragged(MapView.this, mapData, button, e.getModifiersEx(), screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1], sameTile))
                    repaint(0, 0, getWidth(), getHeight());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;

                if((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK){
                    var scrollAmount = 1.15f;
                    var zoom = getZoom();
                    if(e.getWheelRotation() > 0)
                        zoom = Math.round(zoom / scrollAmount * 100) / 100f;
                    else
                        zoom = Math.round(zoom * scrollAmount * 100) / 100f;
                    if(zoom < .25)
                        zoom = .25f;
                    else if(zoom > 15)
                        zoom = 15;
                    else if(zoom - .05 > 1 / scrollAmount && zoom + .05 < 1 * scrollAmount)
                        zoom = 1;
                    setZoom(zoom);
                    repaint(0, 0, getWidth(), getHeight());
                    return;
                }

                var screenX = e.getX();
                var screenY = e.getY();
                var mapPos = transScreenCoordToMapCoord(screenX, screenY);
                var tilePos = transMapCoordToTileCoord(mapPos[0], mapPos[1]);

                if(window.getCurrentMapModule().omMouseWheelMoved(MapView.this, mapData, e.getModifiersEx(), e.getWheelRotation() > 0, screenX, screenY, mapPos[0], mapPos[1], tilePos[0], tilePos[1]))
                    repaint(0, 0, getWidth(), getHeight());
            }

            private int[] transScreenCoordToMapCoord(int x, int y){
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return new int[2];
                var xStart = getWidth() / 2 - (int)(mapData.getWidth() * Window.TILE_WIDTH / 2 * zoom);
                var yStart = getHeight() / 2 - (int)(mapData.getHeight() * Window.TILE_HEIGHT / 2 * zoom);
                return new int[] {x - xStart - (int)offsetX, y - yStart - (int)offsetY};
            }

            private int[] transMapCoordToTileCoord(int x, int y){
                return new int[]{ (int)Math.floor((x / (float) Core.TILE_WIDTH + .5f) / zoom), (int)Math.floor((y / (float) Core.TILE_HEIGHT + .5f) / zoom)};
            }
        };
    }

    private KeyListener genKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(bitcheck(e.getModifiersEx(), KeyEvent.CTRL_DOWN_MASK) && e.getKeyCode() == KeyEvent.VK_SPACE){
                    offsetX = 0;
                    offsetY = 0;
                    repaint();
                    return;
                }
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                if(!window.getCurrentMapModule().onKeyPressed(MapView.this, mapData, e))
                    window.dispatchEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                var mapData = window.getSingleton(GameData.class).getMap(mapID);
                if(mapData == null)
                    return;
                if(!window.getCurrentMapModule().onKeyReleased(MapView.this, mapData, e))
                    window.dispatchEvent(e);
            }
        };
    }

    public ICopyObject onCopy() {
        var mapData = window.getSingleton(GameData.class).getMap(mapID);
        if(mapData == null)
            return null;
        return window.getCurrentMapModule().onCopy(this, mapData);
    }
}
