package de.sunnix.aje.editor.window.menubar.resource;

import de.sunnix.aje.editor.util.DialogUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import de.sunnix.aje.editor.window.resource.Tileset;
import de.sunnix.aje.editor.window.resource.TilesetPropertie;
import de.sunnix.aje.engine.util.Tuple;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static de.sunnix.aje.editor.lang.Language.getString;

public class TilesetView extends JPanel implements IResourceView{

    private final Window window;
    private final JPanel parent;
    private final List<JComponent> propertiesToggleComponents = new ArrayList<>(); // used to enable/disable components on tileset selection
    private final List<Tuple.Tuple2<JComponent, BiConsumer<JComponent, TilesetPropertie>>> propertiesComponents = new ArrayList<>();

    private DefaultListModel<String> model;
    private JList<String> list;
    private JPanel imageView;

    private final int[] selectedTiles = {0, 0, 1, 1};

    // ####################### Properties #######################
    // Tileset
    private JTextField lblTitle, lblWidth, lblHeight;
    // Tiles
    private JCheckBox blocking;

    public TilesetView(Window window, JPanel parent){
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout(5, 5));
        add(setupListView(), BorderLayout.WEST);
        add(setupImageView(), BorderLayout.CENTER);
        add(setupPropertiesView(), BorderLayout.EAST);
        list.setSelectedIndex(-1);
        propertiesToggleComponents.forEach(c -> enableComponent(c, list.getSelectedIndex() != -1));
        select(0, 0);
    }

    private JScrollPane setupListView(){
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3)
                    new JPopupMenu(){
                        {
                            var create = new JMenuItem(getString("name.create"));
                            create.addActionListener(this::createTileset);
                            add(create);
                            var selected = list.getSelectedValue();
                            if(selected != null){
                                var label = new JLabel(selected);
                                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                                add(label, 0);
                                add(new JSeparator(), 1);
                                var delete = new JMenuItem(getString("name.delete"));
                                delete.addActionListener(this::deleteTileset);
                                add(delete);
                            }
                        }

                        private void createTileset(ActionEvent e) {
                            var res = window.getSingleton(Resources.class);
                            var input = new JTextField(20);
                            var cat = new JComboBox<String>(res.image_getAllCategories().toArray(String[]::new));
                            cat.setSelectedIndex(-1);
                            var tex = new JComboBox<String>();
                            cat.addActionListener(a -> {
                                tex.removeAllItems();
                                res.image_getCategoryContent((String)cat.getSelectedItem()).forEach(tex::addItem);
                            });
                            while (DialogUtils.showMultiInputDialog(parent, getString("view.dialog_resources.tileset.create"), null, new String[]{getString("name.name"), getString("view.dialog_resources.tileset.ts_cat"), getString("name.texture")}, new JComponent[]{input, cat, tex})) {
                                if(cat.getSelectedIndex() == -1 || tex.getSelectedIndex() == -1)
                                    JOptionPane.showMessageDialog(parent, getString("view.dialog_resources.tileset.no_texture_selected"), getString("name.error"), JOptionPane.ERROR_MESSAGE);
                                else if (DialogUtils.validateInput(parent, input.getText(), res.tileset_getTilesetnames()))
                                    break;
                            }
                            Tileset ts;
                            try {
                                ts = new Tileset(input.getText(), String.format("%s/%s", cat.getSelectedItem(), tex.getSelectedItem()), res);
                            } catch (IndexOutOfBoundsException ex) {
                                JOptionPane.showMessageDialog(parent, ex.getMessage(), getString("name.error"), JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            res.tileset_add(input.getText(), ts);
                            updateList();
                            window.setProjectChanged();
                        }

                        private void deleteTileset(ActionEvent actionEvent) {
                            if(JOptionPane.showConfirmDialog(parent,
                                    getString("view.dialog_resources.tileset.delete_tileset.text"),
                                    getString("view.dialog_resources.tileset.delete_tileset.title"),
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION
                            )
                                return;
                            var res = window.getSingleton(Resources.class);
                            res.tileset_remove(getCurrentTileset().getName());
                            updateList();
                            window.reloadTilesetView();
                            window.reloadMap();
                            window.setProjectChanged();
                        }

                    }.show(list, e.getX(), e.getY());
            }
        });
        list.addListSelectionListener(l -> {
            propertiesToggleComponents.forEach(c -> enableComponent(c, list.getSelectedIndex() != -1));
            var ts = getCurrentTileset();
            lblTitle.setText(ts == null ? "" : ts.getName());
            lblWidth.setText(ts == null ? "" : Integer.toString(ts.getWidth()));
            lblHeight.setText(ts == null ? "" : Integer.toString(ts.getHeight()));
            recalculateImageSize();
            imageView.revalidate();
            imageView.repaint();
        });
        var scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createTitledBorder(getString("name.tilesets")));
        scroll.setPreferredSize(new Dimension(120, 0));
        return scroll;
    }

    private void recalculateImageSize() {
        if(list.getSelectedIndex() == -1) {
            imageView.setPreferredSize(new Dimension(0, 0));
            return;
        }
        var ts = getCurrentTileset();
        if(ts == null) {
            imageView.setPreferredSize(new Dimension(0, 0));
            return;
        }
        imageView.setPreferredSize(new Dimension(ts.getWidth() * 24, ts.getHeight() * 16));
    }

    private JScrollPane setupImageView(){
        imageView = new JPanel(null){

            {
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
                var ml = genMouseListener();
                addMouseListener(ml);
                addMouseMotionListener(ml);
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                drawTileset((Graphics2D) g);
            }
        };
        var scroll = new JScrollPane(imageView);
        return scroll;
    }

    private MouseAdapter genMouseListener() {
        return new MouseAdapter() {
            boolean primaryPress = false;
            int preX, preY, dragPreX, dragPreY;
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() != MouseEvent.BUTTON1)
                    return;
                var x = e.getX() / 24;
                var y = e.getY() / 16;
                var ts = getCurrentTileset();
                if(ts == null)
                    return;
                if(x >= ts.getWidth() || y >= ts.getHeight())
                    return;
                select(x, y);
                preX = x;
                preY = y;
                dragPreX = preX;
                dragPreY = preY;
                primaryPress = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() != MouseEvent.BUTTON1)
                    return;
                primaryPress = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(!primaryPress)
                    return;
                var tileset = getCurrentTileset();
                if(tileset == null)
                    return;
                int x, y;
                x = e.getX() / 24;
                y = e.getY() / 16;
                if(x == dragPreX && y == dragPreY)
                    return;
                dragPreX = x;
                dragPreY = y;
                if(x < 0)
                    x = 0;
                if(y < 0)
                    y = 0;
                if(x >= tileset.getWidth())
                    x = tileset.getWidth() - 1;
                if(y >= tileset.getHeight())
                    y = tileset.getHeight() - 1;
                select(Math.min(preX, x), Math.min(preY, y), Math.max(Math.abs(preX - x) + 1, 1), Math.max(Math.abs(preY - y) + 1, 1));
            }
        };
    }

    private static final Color COLOR_BLOCKING = new Color(1f, .1f, .1f, .4f);

    private void drawTileset(Graphics2D g) {
        var tileset = getCurrentTileset();
        if(tileset == null)
            return;
        var image = tileset.getImage(window);
        if(image == null)
            return;
        g.drawImage(image, 0, 0, null);
        for(var x = 0; x < tileset.getWidth(); x++)
            for (int y = 0; y < tileset.getHeight(); y++) {
                var prop = tileset.getPropertie(x, y);
                if(prop.isBlocking()){
                    g.setColor(COLOR_BLOCKING);
                    g.fillRect(x * 24, y * 16, 24, 16);
                }
            }
        g.setColor(Color.YELLOW);
        int x, y, w, h;
        x = selectedTiles[0];
        y = selectedTiles[1];
        w = selectedTiles[2];
        h = selectedTiles[3];
        g.drawRect(x * 24, y * 16, 24 * w, 16 * h);
    }

    private JScrollPane setupPropertiesView(){
        var panel = new JPanel(new BorderLayout());
        var top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder(getString("name.tileset")));
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 5, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 2;
        top.add(lblTitle = new JTextField(), gbc);
        lblTitle.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy++;

        gbc.gridwidth = 1;
        gbc.weightx = .5;
        top.add(new JLabel(getString("name.width")), gbc);
        gbc.gridx++;
        top.add(lblWidth = new JTextField(9), gbc);
        lblWidth.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy++;

        top.add(new JLabel(getString("name.height")), gbc);
        gbc.gridx++;
        top.add(lblHeight = new JTextField(), gbc);
        lblHeight.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy++;

        panel.add(top, BorderLayout.NORTH);

        var bottom = new JPanel(new GridBagLayout());
        bottom.setBorder(BorderFactory.createTitledBorder(getString("name.tile")));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 5, 10);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1;
        gbc.gridwidth = 2;
        bottom.add(blocking =
                createPropertieComponent(
                        new JCheckBox("Blocking"),
                        (cb, prop) -> cb.setSelected(prop.isBlocking()),
                        (cb, prop) -> prop.setBlocking(cb.isSelected())
                ), gbc
        );
        propertiesToggleComponents.add(blocking);
        gbc.gridy++;

        var dealDamage = new JCheckBox("Deal damage");
        dealDamage.setEnabled(false);
        bottom.add(dealDamage, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;


        bottom.add(new JLabel("Type:"), gbc);
        gbc.gridx++;

        var dmgType = new JComboBox<>(new String[]{ "Normal", "Water", "Fire", "Ice" });
        dmgType.setEnabled(false);
        bottom.add(dmgType, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        var tmpPanel = new JPanel(new BorderLayout());
        tmpPanel.add(bottom, BorderLayout.NORTH);

        panel.add(tmpPanel, BorderLayout.CENTER);

        var scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(200, 0));
        return scroll;
    }

    private <T extends JComponent> T createPropertieComponent(T comp, BiConsumer<T, TilesetPropertie> onLoad, BiConsumer<T, TilesetPropertie> onChange){
        if(onChange != null) {
            if (comp instanceof AbstractButton btn)
                btn.addActionListener(l -> {
                    var ts = getCurrentTileset();
                    if (ts == null)
                        return;
                    for (int x = selectedTiles[0]; x < selectedTiles[0] + selectedTiles[2]; x++)
                        for (int y = selectedTiles[1]; y < selectedTiles[1] + selectedTiles[3]; y++) {
                            var prop = ts.getPropertie(x, y);
                            if (prop == null)
                                continue;
                            onChange.accept(comp, prop);
                        }
                    imageView.repaint();
                    window.setProjectChanged();
                });
        }
        propertiesComponents.add(Tuple.create(comp, (BiConsumer<JComponent, TilesetPropertie>) onLoad));
        return comp;
    }

    private void updateList(){
        model.clear();
        window.getSingleton(Resources.class).tileset_getTilesetnames().forEach(model::addElement);
    }

    private Tileset getCurrentTileset(){
        var tsName = list.getSelectedValue();
        if(tsName == null)
            return null;
        var res = window.getSingleton(Resources.class);
        return res.tileset_get(tsName);
    }

    private static void enableComponent(JComponent comp, boolean enable){
        for(var child: comp.getComponents())
            if(child instanceof JComponent jChild)
                enableComponent(jChild, enable);
        comp.setEnabled(enable);
    }

    public void select(int x, int y, int w, int h){
        selectedTiles[0] = x;
        selectedTiles[1] = y;
        selectedTiles[2] = w;
        selectedTiles[3] = h;
        reloadProperties();
        imageView.repaint();
    }

    public void select(int x, int y){
        select(x, y, 1, 1);
    }

    public void reloadProperties(){
        var ts = getCurrentTileset();
        if(ts == null)
            return;
        var prop = ts.getPropertie(selectedTiles[0], selectedTiles[1]);
        if(prop == null)
            return;
        propertiesComponents.forEach(t -> {
            t.t2().accept(t.t1(), prop);
        });
    }

    @Override
    public void onViewAttached() {
        updateList();
    }

    @Override
    public void onViewClosed() {

    }

}
