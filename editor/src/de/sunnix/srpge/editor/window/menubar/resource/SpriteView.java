package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.customswing.NumberPicker;
import de.sunnix.srpge.editor.window.resource.Resources;
import de.sunnix.srpge.editor.window.resource.Sprite;
import de.sunnix.srpge.engine.util.Tuple;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class SpriteView extends JPanel implements IResourceView{

    private final Window window;
    private final JPanel parent;

    private Thread spriteRenderThread;
    private boolean shouldStopRender;
    private long animTimer = 0;
    private long texPos = -1;

    private JList<String> categories, sprites;
    private JPanel texturePanel, previewPanel;

    /*
     *  Properties
     */
    private List<Tuple.Tuple2<JComponent, BiConsumer<? super JComponent, Sprite>>> propertieComps = new ArrayList<>();
    private boolean spriteSavingDisabled;
    private JComboBox<String> imageCategory, imageResource;
    private JComboBox<String> direction;
    private JCheckBox showIndex;
    private NumberPicker animSpeed;

    public SpriteView(Window window, JPanel parent) {
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout());

        add(genLists(), BorderLayout.WEST);
        add(texturePanel = genTexturePanel(), BorderLayout.CENTER);
        add(genProperties(), BorderLayout.EAST);
    }

    private JPanel genLists(){
        var panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(120, 0));

        var lists = window.getSingleton(Resources.class).sprites.getJListBuilder(Sprite::new)
                .setParent(this)
                .setDataName("Sprite")
                .setOnChange(window::setProjectChanged)
                .build();
        categories = lists[0];
        sprites = lists[1];

        sprites.addListSelectionListener(l -> {
            texturePanel.repaint();
            reloadSprite();
        });

        var scroll = new JScrollPane(categories);
        scroll.setBorder(BorderFactory.createTitledBorder(getString("name.category")));
        panel.add(scroll, BorderLayout.NORTH);

        scroll = new JScrollPane(sprites);
        scroll.setBorder(BorderFactory.createTitledBorder(getString("name.sprite")));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel genTexturePanel(){
        return new JPanel(null){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        var sprite = getCurrentSprite();
                        if(sprite == null)
                            return;
                        if(imageResource.getSelectedIndex() == -1)
                            return;
                        var texture = window.getSingleton(Resources.class).images.getData((String) imageCategory.getSelectedItem(), (String) imageResource.getSelectedItem());
                        if(texture == null)
                            return;
                        var image = texture.getImage();
                        if(image == null)
                            return;

                        var x = getWidth() / 2 - image.getWidth() / 2;
                        var y = getHeight() / 2 - image.getHeight() / 2;

                        var mX = e.getX();
                        var mY = e.getY();
                        if(mX < x || mX > x + image.getWidth() || mY < y || mY > y + image.getHeight())
                            return;

                        mX -= x;
                        mY -= y;

                        var spriteWidth = image.getWidth() / texture.getWidth();
                        var spriteHeight = image.getHeight() / texture.getHeight();

                        mX /= spriteWidth;
                        mY /= spriteHeight;

                        sprite.getPattern(direction.getSelectedIndex()).add(mX + mY * texture.getWidth());
                        window.setProjectChanged();
                        direction.setSelectedIndex(direction.getSelectedIndex()); // reload list
                    }
                });
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(imageResource.getSelectedIndex() == -1)
                    return;
                var texture = window.getSingleton(Resources.class).images.getData((String) imageCategory.getSelectedItem(), (String) imageResource.getSelectedItem());
                if(texture == null)
                    return;
                var image = texture.getImage();
                if(image == null)
                    return;
                var x = getWidth() / 2 - image.getWidth() / 2;
                var y = getHeight() / 2 - image.getHeight() / 2;
                g.drawImage(image, x, y, null);

                var spriteWidth = image.getWidth() / texture.getWidth();
                var spriteHeight = image.getHeight() / texture.getHeight();
                var font = new Font("Arial", Font.BOLD, 16);
                g.setFont(font);
                for(var aX = 0; aX < texture.getWidth(); aX++)
                    for(var aY = 0; aY < texture.getHeight(); aY++){
                        g.setColor(Color.BLACK);
                        g.drawRect(x + aX * spriteWidth, y + aY * spriteHeight, spriteWidth, spriteHeight);
                        if(showIndex.isSelected()) {
                            var text = String.valueOf(aX + aY * texture.getWidth());
                            g.setColor(Color.BLACK);
                            g.drawString(text, x + aX * spriteWidth + 5 + 2, y + aY * spriteHeight + g.getFontMetrics().getHeight() + 2);
                            g.setColor(Color.RED);
                            g.drawString(text, x + aX * spriteWidth + 5, y + aY * spriteHeight + g.getFontMetrics().getHeight());
                        }
                    }
            }
        };
    }

    private JPanel genProperties(){
        var mainPanel = new JPanel(new BorderLayout());

        var pPanel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 3, 0, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        pPanel.add(new JLabel(getString("view.dialog_resources.sprite.texture")), gbc);
        gbc.gridy++;

        var boxes = window.getSingleton(Resources.class).images.getSelectionBoxes();
        imageCategory = createPropertieComp(boxes[0], (c, s) -> {
            if(s == null) {
                c.setSelectedIndex(0);
                return;
            }
            var name = s.getImageName();
            if(name == null) {
                c.setSelectedIndex(0);
                return;
            }
            var split = name.split("/");
            c.setSelectedItem(split[0]);
        });
        addAL(imageCategory, l -> window.setProjectChanged());
        pPanel.add(imageCategory, gbc);
        gbc.gridy++;
        imageResource = createPropertieComp(boxes[1], (c, s) -> {
            if(s == null) // will be set automaticly to index -1 from imageCategory
                return;
            var name = s.getImageName();
            if(name == null) {
                c.setSelectedIndex(-1);
                return;
            }
            var split = name.split("/");
            if(split.length < 2) {
                c.setSelectedIndex(-1);
                return;
            }
            c.setSelectedItem(split[1]);
        });
        addAL(imageResource, l -> {
            var sprite = getCurrentSprite();
            if(sprite != null) {
                sprite.setImage(imageResource.getSelectedIndex() == -1 ? null : imageCategory.getSelectedItem() + "/" + imageResource.getSelectedItem());
                window.setProjectChanged();
            }
            texturePanel.repaint();
            previewPanel.repaint();
        });
        pPanel.add(imageResource, gbc);
        gbc.gridy++;

        {
            var animPanel = new JPanel(new GridBagLayout());
            animPanel.setBorder(new TitledBorder(getString("name.animation")));
            var gbc2 = new GridBagConstraints();
            gbc2.gridx = 0;
            gbc2.gridy = 0;
            gbc2.gridwidth = 1;
            gbc2.gridheight = 1;
            gbc2.weightx = 1;
            gbc2.insets = new Insets(5, 3, 0, 3);
            gbc2.fill = GridBagConstraints.HORIZONTAL;

            animPanel.add(new JLabel(getString("view.dialog_resources.sprite.direction_type")), gbc2);
            gbc2.gridy++;
            var directionType = createPropertieComp(new JComboBox<>(Sprite.DirectionType.values()), (c, s) -> {
                if (s == null)
                    c.setSelectedIndex(0);
                else
                    c.setSelectedItem(s.getDirectionType());
            });
            addAL(directionType, l -> {
                var sprite = getCurrentSprite();
                if (sprite == null)
                    return;
                if(!sprite.getDirectionType().equals(directionType.getSelectedItem())) {
                    sprite.setDirectionType((Sprite.DirectionType) directionType.getSelectedItem());
                    window.setProjectChanged();
                }
                reloadSprite();
            });
            animPanel.add(directionType, gbc2);
            gbc2.gridy++;

            animPanel.add(new JLabel(getString("view.dialog_resources.sprite.animation_type")), gbc2);
            gbc2.gridy++;
            var animType = createPropertieComp(new JComboBox<>(Sprite.AnimationType.values()), (c, s) -> {
                if (s == null)
                    c.setSelectedIndex(0);
                else
                    c.setSelectedItem(s.getAnimationType());
            });
            addAL(animType, l -> {
                var sprite = getCurrentSprite();
                if (sprite == null)
                    return;
                if(!sprite.getAnimationType().equals(animType.getSelectedItem())) {
                    sprite.setAnimationType((Sprite.AnimationType) animType.getSelectedItem());
                    window.setProjectChanged();
                }
                reloadSprite();
            });
            animPanel.add(animType, gbc2);
            gbc2.gridy++;

            animPanel.add(new JLabel(getString("view.dialog_resources.sprite.direction")), gbc2);
            gbc2.gridy++;
            direction = createPropertieComp(new JComboBox<>(), (c, s) -> {
                c.removeAllItems();
                if(s == null)
                    return;
                ((DefaultComboBoxModel<String>) c.getModel()).addAll(s.getDirectionsFromType());
                c.setSelectedIndex(0);
            });
            animPanel.add(direction, gbc2);
            gbc2.gridy++;

            var spriteListModel = new DefaultListModel<Integer>();
            var spriteList = createPropertieComp(new JList<>(spriteListModel), (c, s) -> {
                spriteListModel.clear();
                if(s == null){
                    return;
                }
                spriteListModel.addAll(s.getPattern(direction.getSelectedIndex()));
            });
            spriteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            spriteList.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    var sprite = getCurrentSprite();
                    if(sprite == null)
                        return;
                    switch (e.getKeyCode()){
                        case KeyEvent.VK_DELETE -> {
                            var index = spriteList.getSelectedIndex();
                            if(index < 0)
                                return;
                            sprite.getPattern(direction.getSelectedIndex()).remove(index);
                            spriteListModel.remove(index);
                            window.setProjectChanged();
                            spriteList.setSelectedIndex(index - (spriteListModel.getSize() > index ? 0 : 1));
                        }
                        case KeyEvent.VK_UP -> {
                            if(!e.isControlDown())
                                return;
                            var index = spriteList.getSelectedIndex();
                            if(index == 0)
                                return;
                            var pattern = sprite.getPattern(direction.getSelectedIndex());
                            var tex = pattern.remove(index);
                            pattern.add(index - 1, tex);
                            spriteListModel.remove(index);
                            spriteListModel.add(index - 1, tex);
                            window.setProjectChanged();
                            spriteList.setSelectedIndex(index - 1);
                            e.consume();
                        }
                        case KeyEvent.VK_DOWN -> {
                            if(!e.isControlDown())
                                return;
                            var index = spriteList.getSelectedIndex();
                            if(index == spriteListModel.getSize() - 1)
                                return;
                            var pattern = sprite.getPattern(direction.getSelectedIndex());
                            var tex = pattern.remove(index);
                            pattern.add(index + 1, tex);
                            spriteListModel.remove(index);
                            spriteListModel.add(index + 1, tex);
                            window.setProjectChanged();
                            spriteList.setSelectedIndex(index + 1);
                            e.consume();
                        }
                        default -> spriteList.getParent().dispatchEvent(e);
                    }
                }
            });
            var scroll = new JScrollPane(spriteList);
            scroll.setPreferredSize(new Dimension(0, 120));
            animPanel.add(scroll, gbc2);
            gbc2.gridy++;

            addAL(direction, l -> {
                spriteListModel.clear();
                var sprite = getCurrentSprite();
                if(sprite == null){
                    return;
                }
                spriteListModel.addAll(sprite.getPattern(direction.getSelectedIndex()));
                animTimer = 0;
            });

            pPanel.add(animPanel, gbc);
            gbc.gridy++;
        }

        showIndex = new JCheckBox(getString("view.dialog_resources.sprite.show_indices"));
        showIndex.addActionListener(l -> texturePanel.repaint());
        pPanel.add(showIndex, gbc);
        gbc.gridy++;

        animSpeed = createPropertieComp(new NumberPicker(getString("view.dialog_resources.sprite.animation_speed"), 1, 0, 1, Integer.MAX_VALUE), (c, s) -> {
            if(s == null) {
                animSpeed.setValue(1, true);
                return;
            }
            animSpeed.setValue(s.getAnimationSpeed(), true);
        });
        addCL(animSpeed, (src, oldV, newV) -> {
            var sprite = getCurrentSprite();
            if(sprite == null)
                return;
            sprite.setAnimationSpeed(animSpeed.getValue());
            window.setProjectChanged();
        });
        pPanel.add(animSpeed, gbc);
        gbc.gridy++;

        var mPanel = new JPanel(new FlowLayout());
        mPanel.setPreferredSize(new Dimension(200, 0));
        mPanel.add(pPanel);
        var scroll = new JScrollPane(mPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(scroll, BorderLayout.CENTER);


        previewPanel = new JPanel(null){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                var sprite = getCurrentSprite();
                if(sprite == null)
                    return;

                var index = sprite.getTextureIndexForAnimation(animTimer, direction.getSelectedIndex());
                if(index == -1)
                    return;
                var texture = sprite.getImage(window);
                if(texture == null)
                    return;
                var image = texture.getImage();
                if(image == null)
                    return;
                var aWidth = texture.getWidth();
                var aHeight = texture.getHeight();

                index %= aWidth * aHeight;

                var spriteWidth = image.getWidth() / aWidth;
                var spriteHeight = image.getHeight() / aHeight;

                var x = getWidth() / 2 - spriteWidth / 2;
                var y = getHeight() / 2 - spriteHeight / 2;

                var srcX1 = (index % aWidth) * spriteWidth;
                var srcY1 = (index / aWidth % aHeight) * spriteHeight;
                var srcX2 = srcX1 + spriteWidth;
                var srcY2 = srcY1 + spriteHeight;

                g.drawImage(image, x, y, x + spriteWidth, y + spriteHeight, srcX1, srcY1, srcX2, srcY2, null);
            }
        };
        previewPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        previewPanel.setPreferredSize(new Dimension(0, 300));

        return mainPanel;
    }

    @SuppressWarnings("unchecked")
    private <T extends JComponent> T createPropertieComp(T comp, BiConsumer<? super T, Sprite> onSpriteLoad){
        propertieComps.add(Tuple.create(comp, (BiConsumer<? super JComponent, Sprite>) onSpriteLoad));
        return comp;
    }

    /**
     * Add a ActionListener that don't work if spriteSavingDisabled is disabled
     */
    private void addAL(JComboBox<?> comp, ActionListener a){
        comp.addActionListener(l -> {
            if(spriteSavingDisabled)
                return;
            a.actionPerformed(l);
        });
    }

    private void addCL(NumberPicker comp, NumberPicker.ChangeListener a){
        comp.addChangeListener((src, oldV, newV) -> {
            if(spriteSavingDisabled)
                return;
            a.onChange(src, oldV, newV);
        });
    }

    private Sprite getCurrentSprite(){
        return window.getSingleton(Resources.class).sprites.getData(categories.getSelectedValue(), sprites.getSelectedValue());
    }

    private void reloadSprite(){
        texPos = -1;
        animTimer = 0;
        spriteSavingDisabled = true;
        propertieComps.forEach(x -> {
            x.t1().setEnabled(sprites.getSelectedIndex() != -1);
            x.t2().accept(x.t1(), getCurrentSprite());
        });
        spriteSavingDisabled = false;
        previewPanel.repaint();
    }

    @Override
    public void onViewAttached() {
        reloadSprite();
        spriteRenderThread = new Thread(() -> {
            while (!shouldStopRender) {
                try {
                    Thread.sleep(16, 666666);
                    var sprite = getCurrentSprite();
                    if(sprite != null){
                        var nTexPos = sprite.getTextureIndexForAnimation(animTimer, direction.getSelectedIndex());
                        if(nTexPos != texPos){
                            texPos = nTexPos;
                            previewPanel.repaint();
                        }
                    }
                    animTimer++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        spriteRenderThread.setDaemon(true);
        spriteRenderThread.start();
    }

    @Override
    public void onViewClosed() {
        shouldStopRender = true;
    }
}
