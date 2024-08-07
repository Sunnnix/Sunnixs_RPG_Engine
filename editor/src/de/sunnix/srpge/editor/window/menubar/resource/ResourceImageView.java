package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import de.sunnix.srpge.editor.window.resource.ImageResource;
import de.sunnix.srpge.editor.window.resource.Resources;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class ResourceImageView extends JPanel implements IResourceView {

    private static final String[] supportedFormats = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final String regexFilename = "[a-zA-Z0-9_]+";
    private final Window window;
    private final JPanel parent;
    private final List<JComponent> selectionDependency = new LinkedList<>();
    private JList<String> categoryList;
    private JList<String> imageList;

    private JLabel selectedText;
    private JPanel imageComponent;
    private JTextField atlasWidth, atlasHeight;
    private JCheckBox showComplete, animated, showGrid;
    private JSpinner animSpeed;
    private int animTimer;
    private float zoom = 1;

    private Timer timer;

    public ResourceImageView(Window window, JPanel parent){
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout());
        add(genImageView(), BorderLayout.CENTER);
        add(genResList(), BorderLayout.WEST);
        setTransferHandler(genTransferHandler());
        onImageSelected();
        timer = new Timer("Animator", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(0, 666);
                    onTimerLoop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }, 0, 16);
    }

    private JPanel genImageView() {
        var panel = new JPanel(new BorderLayout(5, 0));
        selectedText = new JLabel("", JLabel.CENTER);
        selectedText.setFont(selectedText.getFont().deriveFont(Font.BOLD, 20));
        panel.add(selectedText, BorderLayout.NORTH);
        panel.add(imageComponent = genImagePanel(), BorderLayout.CENTER);

        var bottomPanel = new JPanel(new BorderLayout(5, 5));

        var imageInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        showComplete = new JCheckBox(getString("view.dialog_resources.image.show_complete_image"), true);
        showComplete.addActionListener(a -> imageComponent.repaint());
        showComplete.setHorizontalTextPosition(JCheckBox.LEFT);
        selectionDependency.add(showComplete);
        atlasWidth = new JTextField(4);
        atlasWidth.setHorizontalAlignment(JTextField.RIGHT);
        atlasWidth.setEditable(false);
        atlasHeight = new JTextField(4);
        atlasHeight.setHorizontalAlignment(JTextField.RIGHT);
        atlasHeight.setEditable(false);
        animated = new JCheckBox(getString("view.dialog_resources.image.animate"));
        animated.setHorizontalTextPosition(JCheckBox.LEFT);
        selectionDependency.add(animated);
        animated.addActionListener(a -> imageComponent.repaint());
        animSpeed = new JSpinner(new SpinnerNumberModel(20, 1, 240, 1));
        selectionDependency.add(animSpeed);
        showGrid = new JCheckBox(getString("view.dialog_resources.image.show_grid"));
        showGrid.addActionListener(a -> imageComponent.repaint());
        showGrid.setHorizontalTextPosition(JCheckBox.LEFT);
        selectionDependency.add(showGrid);

        imageInfoPanel.add(showComplete);
        imageInfoPanel.add(new JLabel(getString("name.atlas_width")));
        imageInfoPanel.add(atlasWidth);
        imageInfoPanel.add(new JLabel(getString("name.atlas_height")));
        imageInfoPanel.add(atlasHeight);
        imageInfoPanel.add(animated);
        imageInfoPanel.add(new JLabel(getString("view.dialog_resources.image.animation_speed")));
        imageInfoPanel.add(animSpeed);
        imageInfoPanel.add(showGrid);

        bottomPanel.add(imageInfoPanel, BorderLayout.CENTER);

        var slider = new JSlider(JSlider.HORIZONTAL, 10, 500, 100);
        slider.setMajorTickSpacing(100);
        slider.setMinorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(l -> {
            zoom = slider.getValue() / 100f;
            imageComponent.repaint();
        });
        bottomPanel.add(slider, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);


        var json = window.getSingleton(Config.class).getJSONObject("image-res-view");
        showComplete.setSelected(json.get("show-complete-image", true));
        animated.setSelected(json.get("animate", false));
        animSpeed.setValue(json.get("anim-speed", 20));
        zoom = json.get("zoom", 1f);
        slider.setValue((int) (zoom * 100));
        showGrid.setSelected(json.get("show-grid", false));

        return panel;
    }

    private JPanel genResList(){
        var panel = new JPanel(new BorderLayout());

        // Categories
        var catModel = new DefaultListModel<String>();
        window.getSingleton(Resources.class).images.getCategoryNames().forEach(catModel::addElement);
        categoryList = new JList<>(catModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(e -> onCategorySelected());
        var scroll = new JScrollPane(categoryList);
        scroll.setBorder(BorderFactory.createTitledBorder(getString("name.category")));
        scroll.setMaximumSize(new Dimension(0, 200));
        scroll.setMinimumSize(scroll.getMaximumSize());
        panel.add(scroll, BorderLayout.NORTH);

        categoryList.addMouseListener(genCategoryListMouseListener(categoryList));

        // Images
        var jlModel = new DefaultListModel<String>();
        imageList = new JList<>(jlModel);
        imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageList.addListSelectionListener(e -> onImageSelected());
        scroll = new JScrollPane(imageList);
        scroll.setBorder(BorderFactory.createTitledBorder(getString("name.image")));
        scroll.setPreferredSize(new Dimension(120, 0));
        panel.add(scroll, BorderLayout.CENTER);

        imageList.addMouseListener(genImageListMouseListener(imageList));

        return panel;
    }

    private MouseListener genCategoryListMouseListener(JList<String> list) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() != MouseEvent.BUTTON3)
                    return;
                var popup = new JPopupMenu();
                var create = new JMenuItem(getString("name.create"));
                create.addActionListener(a -> createCategory(list));
                popup.add(create);

                var selected = list.getSelectedValue();
                if(selected != null) {
                    var remove = new JMenuItem(getString("name.remove"));
                    remove.addActionListener(a -> removeCategory(list, selected));
                    var change = new JMenuItem(getString("name.change"));
                    change.addActionListener(a -> changeCategory(list, selected));
                    if (selected.equals("default")) {
                        remove.setEnabled(false);
                        change.setEnabled(false);
                    }
                    popup.add(remove);
                    popup.add(change);
                }

                popup.show(list, e.getX(), e.getY());
            }
        };
    }

    private void createCategory(JList<String> list){
        var model = (DefaultListModel<String>) list.getModel();
        String input = null;
        do {
            input = (String) JOptionPane.showInputDialog(this, getString("view.dialog_resources.image.insert_category_name"), getString("view.dialog_resources.image.create_category"), JOptionPane.PLAIN_MESSAGE, null, null, input);
            if (input == null)
                return;
        } while (!DialogUtils.validateInput(parent, input, model.elements()));
        window.getSingleton(Resources.class).images.addCategory(input);
        model.addElement(input);
        list.setSelectedValue(input, true);
        window.setProjectChanged();
    }

    private void removeCategory(JList<String> list, String category) {
        var option = JOptionPane.showConfirmDialog(this,
                getString("view.dialog_resources.image.delete_category.text", category),
                getString("view.dialog_resources.image.delete_category.title"),
                JOptionPane.YES_NO_OPTION);
        if(option != JOptionPane.YES_OPTION)
            return;
        var index = list.getSelectedIndex();
        var model = (DefaultListModel<String>) list.getModel();
        window.getSingleton(Resources.class).images.removeCategory(category);
        model.remove(index);
        if(model.isEmpty())
            return;
        if(model.getSize() <= index)
            list.setSelectedIndex(index - 1);
        else
            list.setSelectedIndex(index);
        window.setProjectChanged();
    }

    private void changeCategory(JList<String> list, String category) {
        var option = JOptionPane.showConfirmDialog(this,
                getString("view.dialog_resources.image.change_category.text", category),
                getString("view.dialog_resources.image.change_category.title"),
                JOptionPane.YES_NO_OPTION);
        if(option != JOptionPane.YES_OPTION)
            return;

        var model = (DefaultListModel<String>) list.getModel();
        String input = category;
        do {
            input = (String) JOptionPane.showInputDialog(this, getString("view.dialog_resources.image.insert_category_name"), getString("view.dialog_resources.image.change_category.title"), JOptionPane.PLAIN_MESSAGE, null, null, input);
            if (input == null || input.equals(category))
                return;
        } while (!DialogUtils.validateInput(parent, input, model.elements()));

        var res = window.getSingleton(Resources.class);
        res.images.renameCategory(category, input);

        var index = list.getSelectedIndex();
        model.remove(index);
        model.add(index, input);
        list.setSelectedIndex(index);
        window.setProjectChanged();
    }

    private MouseListener genImageListMouseListener(JList<String> list) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() != MouseEvent.BUTTON3)
                    return;
                var popup = new JPopupMenu();
                var add = new JMenuItem(getString("name.add"));
                add.addActionListener(a -> addImage(null));
                popup.add(add);

                var selected = list.getSelectedValue();
                if(selected != null) {
                    var remove = new JMenuItem(getString("name.remove"));
                    remove.addActionListener(a -> removeImage(selected));
                    var change = new JMenuItem(getString("name.change"));
                    change.addActionListener(a -> changeImage(selected));
                    popup.add(remove);
                    popup.add(change);
                }

                popup.show(list, e.getX(), e.getY());
            }
        };
    }

    private void addImage(File file) {
        if(file == null) {
            var chooser = new JFileChooser(window.getSingleton(Config.class).get("chooser_image_resource", (String) null));
            var imageFilter = new FileNameExtensionFilter(getString("name.image_file"), supportedFormats);
            chooser.setFileFilter(imageFilter);
            var action = chooser.showOpenDialog(parent);
            if (action != JFileChooser.APPROVE_OPTION)
                return;
            file = chooser.getSelectedFile();
            window.getSingleton(Config.class).set("chooser_image_resource", file.getParent());
        }

        var res = window.getSingleton(Resources.class).images;
        var category = categoryList.getSelectedValue();
        if(!res.containsCategory(category)) {
            JOptionPane.showMessageDialog(this, getString("view.dialog_resources.image.add_no_category_selected.text"), getString("view.dialog_resources.image.no_category_selected.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        BufferedImage image;
        try(var stream = new FileInputStream(file)){
            image = ImageIO.read(stream);
        } catch (Exception e){
            JOptionPane.showMessageDialog(
                    this,
                    getString("view.dialog_resources.image.error_loading_image.text", file, e.getMessage()),
                    getString("view.dialog_resources.image.error_loading_image.title"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        var fName = file.getName();
        var data = new Object[] {
                fName.substring(0, fName.indexOf('.')),
                1,
                1
        };

        for(;;) {
            data = NewImageDialog.showDialog(parent, false, data);
            if (data == null)
                return;
            var name = (String) data[0];
            if(validateInput(name))
                break;
        }
        res.addData(category, (String)data[0], new ImageResource((String)data[0], (int)data[1], (int)data[2], image));
        window.setProjectChanged();
        var model = ((DefaultListModel<String>) imageList.getModel());
        model.addElement((String)data[0]);
        imageList.setSelectedIndex(model.getSize() - 1);
    }

    private void removeImage(String image) {
        if(image == null)
            return;
        var option = JOptionPane.showConfirmDialog(this,
                getString("view.dialog_resources.image.delete_image.text"),
                getString("view.dialog_resources.image.delete_image.title"),
                JOptionPane.YES_NO_OPTION
        );
        if(option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION)
            return;
        var model = ((DefaultListModel<String>) imageList.getModel());
        var index = model.indexOf(image);
        window.getSingleton(Resources.class).images.removeData(categoryList.getSelectedValue(), image);
        model.removeElementAt(index);
        if(model.getSize() > index)
            imageList.setSelectedIndex(index);
        else if(index - 1 >= 0)
            imageList.setSelectedIndex(index - 1);
        window.setProjectChanged();
        window.repaint();
    }

    private void changeImage(String selected) {
        var res = window.getSingleton(Resources.class);
        var image = res.images.getData(categoryList.getSelectedValue(), selected);
        if(image == null) {
            JOptionPane.showMessageDialog(this,
                    getString("view.dialog_resources.image.change_no_category_selected.text"),
                    getString("view.dialog_resources.image.no_category_selected.title"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        var data = new Object[] {
                image.getName(),
                image.getWidth(),
                image.getHeight()
        };
        for(;;) {
            data = NewImageDialog.showDialog(parent, true, data);
            if (data == null)
                return;
            var name = (String) data[0];
            if(name.equals(image.getName()) || validateInput(name))
                break;
        }
        var newName = (String)data[0];
        if(!newName.equals(image.getName()) &&
                JOptionPane.showConfirmDialog(
                        this,
                        getString("view.dialog_resources.image.change_image.text"),
                        getString("view.dialog_resources.image.change_image.title"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.OK_OPTION)
            return;
        image.setName(newName);
        image.setWidth((int)data[1]);
        image.setHeight((int)data[2]);
        res.images.renameData(categoryList.getSelectedValue(), selected, newName);

        var selectedIndex = imageList.getSelectedIndex();
        var model = (DefaultListModel<String>) imageList.getModel();
        model.removeElementAt(selectedIndex);
        model.add(selectedIndex, (String)data[0]);
        imageList.setSelectedIndex(selectedIndex);

        window.setProjectChanged();
        window.repaint();
    }

    private boolean validateInput(String name) {
        var res = window.getSingleton(Resources.class);
        var category = categoryList.getSelectedValue();
        if (!res.images.containsCategory(category)) {
            JOptionPane.showMessageDialog(this,
                    getString("view.dialog_resources.image.select_cat_first"),
                    getString("view.dialog_resources.image.no_cat_selected"),
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return DialogUtils.validateInput(parent, name, res.images.getDataNames(category));
    }

    private void onCategorySelected(){
        var model = (DefaultListModel<String>) imageList.getModel();
        model.clear();
        var selectedCat = categoryList.getSelectedValue();
        if(selectedCat != null) {
            window.getSingleton(Resources.class).images.getDataNames(selectedCat).forEach(model::addElement);
            if(model.getSize() > 0)
                imageList.setSelectedIndex(0);
        }
        imageList.revalidate();
    }

    private void onImageSelected(){
        var enable = imageList.getSelectedIndex() >= 0;
        selectionDependency.forEach(c -> c.setEnabled(enable));
        var imageID = imageList.getSelectedValue();
        selectedText.setText(imageID);
        ImageResource imageRes = null;
        if(enable)
            imageRes = window.getSingleton(Resources.class).images.getData(categoryList.getSelectedValue(), imageID);
        atlasWidth.setText(imageRes == null ? "" : Integer.toString(imageRes.getWidth()));
        atlasHeight.setText(imageRes == null ? "" : Integer.toString(imageRes.getHeight()));
        imageComponent.repaint();
    }

    private JPanel genImagePanel(){
        return new JPanel(){

            {
                setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                var id = imageList.getSelectedValue();
                if(id == null)
                    return;
                var imageRes = window.getSingleton(Resources.class).images.getData(categoryList.getSelectedValue(), id);
                if(imageRes == null)
                    return;
                var image = imageRes.getImage();
                int x, y, width, height, atlasWidth, atlasHeight, srcX = 0, srcY = 0;
                width = image.getWidth();
                height = image.getHeight();
                atlasWidth = imageRes.getWidth();
                atlasHeight = imageRes.getHeight();
                if(!showComplete.isSelected() && (atlasWidth > 1 || atlasHeight > 1)) {
                    width /= atlasWidth;
                    height /= atlasHeight;
                    var aS = (int) animSpeed.getValue();
                    srcX = width * (animTimer / aS % atlasWidth);
                    srcY = height * (animTimer / aS / atlasWidth % atlasHeight);
                }
                x = getWidth() / 2 - (int)(width * zoom / 2);
                y = getHeight() / 2 - (int)(height * zoom / 2);

                g.drawImage(image, x, y, x + (int)(width * zoom), y + (int)(height * zoom), srcX, srcY, srcX + width, srcY + height, null);

                if(showGrid.isSelected()){
                    var g2 = (Graphics2D) g;
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(3));
                    if(!showComplete.isSelected())
                        g.drawRect(x, y, (int)(width * zoom), (int) (height * zoom));
                    else {
                        var iWidth = width * zoom / atlasWidth;
                        var iHeight = height * zoom / atlasHeight;
                        for (int i = 0; i < atlasWidth; i++)
                            for (int j = 0; j < atlasHeight; j++)
                                g.drawRect((int)(x + iWidth * i), (int)(y + iHeight * j), (int)iWidth, (int)iHeight);
                    }
                }
            }
        };
    }

    private TransferHandler genTransferHandler(){
        return new TransferHandler(){

            @Override
            public boolean canImport(TransferSupport support) {
                return categoryList.getSelectedIndex() >= 0 && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if(!canImport(support))
                    return false;

                var transferable = support.getTransferable();
                try{
                    @SuppressWarnings("unchecked")
                    var files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for(var file: files) {
                        if (!formatSupported(file)) {
                            JOptionPane.showMessageDialog(
                                    ResourceImageView.this,
                                    getString("view.dialog_resources.image.unsupported_format.text"),
                                    getString("view.dialog_resources.image.unsupported_format.title"),
                                    JOptionPane.ERROR_MESSAGE
                            );
                            continue;
                        }
                        addImage(file);
                    }
                } catch (IOException | UnsupportedFlavorException e) {
                    JOptionPane.showMessageDialog(
                            ResourceImageView.this,
                            getString("view.dialog_resources.image.error_dragging.text", e.getMessage()),
                            getString("view.dialog_resources.image.error_dragging.title"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                return true;
            }

            private boolean formatSupported(File file) {
                var fileName = file.getName().toLowerCase();
                for(var format : supportedFormats)
                    if(fileName.endsWith("." + format))
                        return true;
                return false;
            }
        };
    }

    private void onTimerLoop() {
        if(!animated.isSelected() || imageList.getSelectedIndex() < 0)
            return;
        animTimer++;
        if(animTimer % ((int)animSpeed.getValue()) == 0)
            imageComponent.repaint();
    }

    @Override
    public void onViewAttached() {}

    @Override
    public void onViewClosed() {
        var json = new BetterJSONObject();
        json.put("show-complete-image", showComplete.isSelected());
        json.put("animate", animated.isSelected());
        json.put("anim-speed", animSpeed.getValue());
        json.put("zoom", zoom);
        window.getSingleton(Config.class).set("image-res-view", json);
        timer.cancel();
    }

    private static class NewImageDialog extends JDialog {

        private final JTextField name;
        private final JSpinner atlasWidth, atlasHeight;
        private boolean canceled = true;

        private NewImageDialog(Component parent, boolean change, Object[] data){
            super(getWindowForComponent(parent), getString("view.dialog_resources.image.new_image_dialog.title", (change ? getString("name.change") : getString("name.create"))), true);
            var content = new JPanel(new BorderLayout(10, 10));
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            var fieldsPanel = new JPanel(new GridBagLayout());
            var gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.ipadx = 5;
            gbc.ipady = 5;
            gbc.fill = GridBagConstraints.BOTH;

            name = new JTextField(15);
            atlasWidth = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
            atlasHeight = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));

            if(data != null && data.length == 3){
                name.setText((String) data[0]);
                atlasWidth.setValue(data[1]);
                atlasHeight.setValue(data[2]);
            }

            gbc.insets.right=7;

            fieldsPanel.add(new JLabel(getString("name.id_name")), gbc);
            gbc.gridx++;
            fieldsPanel.add(name, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            fieldsPanel.add(new JLabel(getString("name.atlas_width")), gbc);
            gbc.gridx++;
            fieldsPanel.add(atlasWidth, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            fieldsPanel.add(new JLabel(getString("name.atlas_height")), gbc);
            gbc.gridx++;
            fieldsPanel.add(atlasHeight, gbc);

            content.add(fieldsPanel, BorderLayout.CENTER);

            var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            var apply = new JButton(change ? getString("button.apply") : getString("name.create"));
            apply.addActionListener(a -> {
                canceled = false;
                dispose();
            });
            buttonsPanel.add(apply);
            var cancel = new JButton(getString("button.cancel"));
            cancel.addActionListener(a -> dispose());
            buttonsPanel.add(cancel);
            content.add(buttonsPanel, BorderLayout.SOUTH);

            setContentPane(content);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(parent);
        }

        private static JDialog getWindowForComponent(Component comp){
            if(comp instanceof JDialog dialog)
                return dialog;
            return getWindowForComponent(comp.getParent());
        }

        public static Object[] showDialog(Component parent, boolean change, Object[] data){
            var dialog = new NewImageDialog(parent, change, data);
            dialog.show();
            dialog.dispose();

            if(dialog.canceled)
                return null;
            else
                return new Object[] {
                        dialog.name.getText(),
                        dialog.atlasWidth.getValue(),
                        dialog.atlasHeight.getValue()
                };
        }

    }

}
