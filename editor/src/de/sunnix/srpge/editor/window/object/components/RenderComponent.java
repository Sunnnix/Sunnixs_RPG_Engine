package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.States;
import de.sunnix.srpge.editor.window.resource.Resources;
import org.lwjglx.debug.joptsimple.internal.Strings;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;
import static de.sunnix.srpge.engine.Core.TILE_HEIGHT;

public class RenderComponent extends Component{

    private String defaultSprite;

    private HashMap<String, String> stateSprites = new HashMap<>();

    public RenderComponent() {
        super("render");
    }

    @Override
    public String genName() {
        return getString("component.render");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        defaultSprite = dso.getString("sprite", null);
        dso.<DataSaveObject>getList("state-sprites")
                .forEach(x -> stateSprites.put(x.getString("state", null), x.getString("sprite", null)));
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putString("sprite", defaultSprite);
        dso.putList("state-sprites", stateSprites.entrySet().stream().map(x -> {
            var sDSO = new DataSaveObject();
            sDSO.putString("state", x.getKey());
            sDSO.putString("sprite", x.getValue());
            return sDSO;
        }).toList());
        return dso;
    }

    @Override
    public Runnable createView(Window window, GameObject object, JPanel parent) {
        var setSpriteBtn = addView(parent, new JButton("Set Sprite"));
        var spriteView = addView(parent, new JPanel(){

            long timer;
            int currentSpriteIndex = -1;

            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(currentSpriteIndex == -1 || defaultSprite == null || defaultSprite.isBlank())
                    return;

                var sprite = window.getSingleton(Resources.class).sprites.getData(RenderComponent.this.defaultSprite);
                if(sprite == null)
                    return;

                var texture = sprite.getImage(window);
                if(texture == null)
                    return;
                var image = texture.getImage();
                if(image == null)
                    return;
                var aWidth = texture.getWidth();
                var aHeight = texture.getHeight();

                currentSpriteIndex %= aWidth * aHeight;

                var spriteWidth = image.getWidth() / aWidth;
                var spriteHeight = image.getHeight() / aHeight;

                var x = getWidth() / 2 - spriteWidth / 2;
                var y = getHeight() / 2 - spriteHeight / 2;

                var srcX1 = (currentSpriteIndex % aWidth) * spriteWidth;
                var srcY1 = (currentSpriteIndex / aWidth % aHeight) * spriteHeight;
                var srcX2 = srcX1 + spriteWidth;
                var srcY2 = srcY1 + spriteHeight;

                g.drawImage(image, x, y, x + spriteWidth, y + spriteHeight, srcX1, srcY1, srcX2, srcY2, null);
            }
        }, 250);

        setSpriteBtn.addActionListener(a -> {
            var newSprite = window.getSingleton(Resources.class).sprites.showSelectDialogSinglePath(parent, getString("component.render.set_sprite"), null, getString("name.sprite"), defaultSprite);
            if(newSprite == null)
                return;
            defaultSprite = newSprite;
            spriteView.timer = 0;
            parent.repaint();
        });

        var setStateSpritesBtn = addView(parent, new JButton(getString("component.render.set_state_sprite")));
        setStateSpritesBtn.addActionListener(l -> new StateSpriteEditDialog(window, parent, stateSprites));

        return () -> {
            if(RenderComponent.this.defaultSprite == null || RenderComponent.this.defaultSprite.isBlank())
                return;
            var sprite = window.getSingleton(Resources.class).sprites.getData(RenderComponent.this.defaultSprite);
            if(sprite == null)
                return;
            spriteView.timer++;
            var index = sprite.getTextureIndexForAnimation(spriteView.timer, 0);
            if(index != spriteView.currentSpriteIndex){
                spriteView.currentSpriteIndex = index;
                parent.repaint();
            }
        };
    }

    @Override
    public boolean onDraw(Window window, GameObject parent, Graphics2D g, float zoom, float offsetX, float offsetY, boolean selected) {
        var sprite = window.getSingleton(Resources.class).sprites.getData(this.defaultSprite);
        if(sprite == null)
            return true;
        var imageRes = sprite.getImage(window);
        if(imageRes == null)
            return true;
        var image = imageRes.getImage();
        if(image == null)
            return true;

        var TW = TILE_WIDTH * zoom;
        var TH = TILE_HEIGHT * zoom;
        var x = parent.getX() * TW + offsetX;
        var y = (parent.getZ() - parent.getY()) * TH + offsetY;
        var sW = image.getWidth() / imageRes.getWidth();
        var sH = image.getHeight() / imageRes.getHeight();
        var w = sW * zoom;
        var h = sH * zoom;
        x -= w / 2;
        y -= h - ((parent.hasComponent(PhysicComponent.class) ? parent.getComponent(PhysicComponent.class).getWidth() / 2 : parent.getWidth() / 2) * TH);

        g.drawImage(image, (int)x, (int)y, (int)(x + w), (int)(y + h), 0, 0, sW, sH, null);

        return true;
    }

    @Override
    public int getRenderPriority() {
        return 1;
    }

    private static class StateSpriteEditDialog extends JDialog {

        private final Window window;
        private final JPanel parent;
        private final Map<String, String> stateSprites;

        public StateSpriteEditDialog(Window window, JPanel parent, HashMap<String, String> stateSprites){
            super(DialogUtils.getWindowForComponent(parent), getString("component.render.state_dialog"), ModalityType.APPLICATION_MODAL);
            this.window = window;
            this.parent = parent;
            var content = new JPanel(new BorderLayout(5, 5));
            content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            setContentPane(content);

            this.stateSprites = new HashMap<>(stateSprites);

            add(createList(), BorderLayout.CENTER);
            add(createButtons(stateSprites), BorderLayout.SOUTH);

            pack();
            setResizable(false);

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(parent);
            setVisible(true);
        }

        private JScrollPane createList(){
            var model = new DefaultListModel<Map.Entry<String, String>>();
            var list = new JList<>(model);
            reloadList(model);
            list.setCellRenderer(this::genCellrenderer);
            list.addMouseListener(genMouseListener(model, list));
            var scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(695, 400));
            return scroll;
        }

        private java.awt.Component genCellrenderer(JList<? extends Map.Entry<String, String>> jList, Map.Entry<String, String> entry, int index, boolean selected, boolean focus) {
            var panel = new JPanel(new BorderLayout());
            var state = States.getState(entry.getKey());
            var prio = new JTextField(state == null ? null : String.valueOf(state.priority()) ,3);
            prio.setHorizontalAlignment(SwingConstants.CENTER);
            prio.setOpaque(false);
            var id = new JTextField(state == null ? null : state.id(), 16);
            id.setOpaque(false);
            var sprite = new JTextField(entry.getValue(), 40);
            sprite.setOpaque(false);
            panel.add(prio, BorderLayout.WEST);
            panel.add(id, BorderLayout.CENTER);
            panel.add(sprite, BorderLayout.EAST);
            if(selected){
                var c = UIManager.getDefaults().getColor("List.selectionBackground");
                prio.setBackground(c);
                id.setBackground(c);
                sprite.setBackground(c);
            }
            else if(index % 2 == 0) {
                var c = panel.getBackground().brighter();
                prio.setBackground(c);
                id.setBackground(c);
                sprite.setBackground(c);
            }
            return panel;
        }

        private MouseAdapter genMouseListener(DefaultListModel<Map.Entry<String, String>> model, JList<Map.Entry<String, String>> list){

            var popup = new JPopupMenu();
            var addMenu = new JMenuItem(getString("component.render.state_dialog.add"));
            var removeMenu = new JMenuItem(getString("component.render.state_dialog.remove"));

            addMenu.addActionListener(a -> {
                var states = States.getStates().stream().filter(x -> !stateSprites.containsKey(x.id())).toList();
                if(states.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, getString("component.render.state_dialog.no_state"), getString("component.render.state_dialog.title"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                var statestrings = states.stream().map(x -> String.format("(%s) %s", x.priority(), x.id())).toArray(String[]::new);
                var selection = (String) JOptionPane.showInputDialog(parent, getString("component.render.state_dialog.select"), getString("component.render.state_dialog.title"), JOptionPane.PLAIN_MESSAGE, null, statestrings, statestrings[0]);
                if(selection == null)
                    return;
                var state = states.get(Arrays.stream(statestrings).toList().indexOf(selection));
                if(state == null)
                    return;
                stateSprites.put(state.id(), Strings.EMPTY);
                reloadList(model);
            });
            removeMenu.addActionListener(a -> {
                stateSprites.remove(list.getSelectedValue().getKey());
                reloadList(model);
            });

            popup.add(addMenu);
            popup.add(removeMenu);

            return new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON1){
                        if(e.getClickCount() != 2)
                            return;
                        var loc = list.locationToIndex(e.getPoint());
                        list.setSelectedIndex(loc);
                        if(loc == -1)
                            return;
                        var selection = window.getSingleton(Resources.class).sprites
                                .showSelectDialog(
                                        StateSpriteEditDialog.this.rootPane,
                                        getString("component.render.state_dialog.select_sprite.title"),
                                        getString("component.render.state_dialog.select_sprite.text"),
                                        getString("name.sprite"),
                                        stateSprites.get(list.getSelectedValue().getValue()
                                        )
                                );
                        if(selection == null)
                            return;
                        stateSprites.put(list.getSelectedValue().getKey(), selection[0] + "/" + selection[1]);
                    } else if(e.getButton() == MouseEvent.BUTTON3){
                        var loc = list.locationToIndex(e.getPoint());
                        list.setSelectedIndex(loc);
                        removeMenu.setEnabled(loc != -1);
                        popup.show(list, e.getX(), e.getY());
                    }
                }
            };
        }

        private void reloadList(DefaultListModel<Map.Entry<String, String>> model){
            model.clear();
            model.addAll(stateSprites.entrySet());
        }

        private JPanel createButtons(HashMap<String, String> originalMap) {
            var panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

            var applyBtn = new JButton(getString("button.apply"));
            var cancelBtn = new JButton(getString("button.cancel"));

            applyBtn.addActionListener(a -> {
                originalMap.clear();
                originalMap.putAll(this.stateSprites);
                dispose();
            });
            cancelBtn.addActionListener(a -> dispose());

            panel.add(applyBtn);
            panel.add(cancelBtn);

            return panel;
        }

    }

    @Override
    public Component clone() {
        var comp = (RenderComponent) super.clone();
        comp.stateSprites = new HashMap<>(comp.stateSprites);
        return comp;
    }

}
