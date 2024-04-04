package de.sunnix.aje.editor.window.menubar.resource;

import de.sunnix.aje.editor.window.Config;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ResourceImageView extends JPanel {

    private static final String[] supportedFormats = {"jpg", "jpeg", "png", "gif", "bmp"};
    private final Window window;
    private final JPanel parent;
    private final List<JComponent> selectionDependency = new LinkedList<>();
    private JList<String> categoryList;
    private JList<String> imageList;

    private JLabel selectedText;
    private JPanel imageComponent;
    private float zoom = 1;

    public ResourceImageView(Window window, JPanel parent){
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout());
        add(genImageView(), BorderLayout.CENTER);
        add(genResList(), BorderLayout.WEST);
        setTransferHandler(genTransferHandler());
        onListSelectionChanged();
    }

    private JPanel genImageView() {
        var panel = new JPanel(new BorderLayout(5, 0));
        selectedText = new JLabel("", JLabel.CENTER);
        selectedText.setFont(selectedText.getFont().deriveFont(Font.BOLD, 20));
        panel.add(selectedText, BorderLayout.NORTH);
        panel.add(imageComponent = genImagePanel(), BorderLayout.CENTER);
        var slider = new JSlider(JSlider.HORIZONTAL, 10, 500, 100);
        slider.setMajorTickSpacing(100);
        slider.setMinorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(l -> {
            zoom = slider.getValue() / 100f;
            imageComponent.repaint();
        });
        panel.add(slider, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel genResList(){
        var panel = new JPanel(new BorderLayout());

        var res = window.getSingleton(Resources.class).imageResources;
        var catModel = new DefaultListModel<String>();
        res.keySet().forEach(catModel::addElement);
        categoryList = new JList<>(catModel);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(e -> onCategorySelected());
        var scroll = new JScrollPane(categoryList);
        scroll.setBorder(BorderFactory.createTitledBorder("Category"));
        scroll.setMaximumSize(new Dimension(0, 200));
        scroll.setMinimumSize(scroll.getMaximumSize());
        panel.add(scroll, BorderLayout.NORTH);

        var jlModel = new DefaultListModel<String>();
        imageList = new JList<>(jlModel);
        imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageList.addListSelectionListener(e -> onListSelectionChanged());
        scroll = new JScrollPane(imageList);
        scroll.setBorder(BorderFactory.createTitledBorder("Image"));
        scroll.setPreferredSize(new Dimension(120, 0));
        panel.add(scroll, BorderLayout.CENTER);

        var btnPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        var btnPanel2 = new JPanel(new GridLayout(1, 0, 5, 0));
        var btnAdd = new JButton("Add");
        btnAdd.addActionListener(a -> addImage());
        var btnChange = new JButton("Change");
        btnChange.addActionListener(a -> changeImage());
        selectionDependency.add(btnChange);
        var btnRemove = new JButton("Remove");
        var cbAskRemove = new JCheckBox("Ask on Remove");
        btnRemove.addActionListener(a -> removeImage(cbAskRemove.isSelected()));
        cbAskRemove.setSelected(true);
        selectionDependency.add(btnRemove);
        selectionDependency.add(cbAskRemove);
        btnPanel.add(btnAdd);
        btnPanel2.add(btnChange);
        btnPanel2.add(btnRemove);
        btnPanel.add(btnPanel2);
        btnPanel.add(cbAskRemove);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addImage() {
        var chooser = new JFileChooser(window.getSingleton(Config.class).get("chooser_image_resource", (String)null));
        var imageFilter = new FileNameExtensionFilter("Image file", supportedFormats);
        chooser.setFileFilter(imageFilter);
        var action = chooser.showOpenDialog(parent);
        if(action != JFileChooser.APPROVE_OPTION)
            return;
        var file = chooser.getSelectedFile();
        window.getSingleton(Config.class).set("chooser_image_resource", file.getParent());
        loadNewImage(file);
    }

    private void loadNewImage(File file){
        var model = ((DefaultListModel<String>) imageList.getModel());
        try(var stream = new FileInputStream(file)){
            var image = ImageIO.read(stream);
            var fName = file.getName();
            fName = fName.substring(0, fName.indexOf('.'));
            var input = fName;
            for(;;) {
                input = (String) JOptionPane.showInputDialog(this, "Write a name for the id of the image:", "Create new Image Resource", JOptionPane.PLAIN_MESSAGE, null, null, input);
                if (input == null)
                    return;
                if(input.isEmpty())
                    JOptionPane.showMessageDialog(this, "The name can't be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                else if(window.getSingleton(Resources.class).imageResources.get("default").containsKey(input))
                    JOptionPane.showMessageDialog(this, "The name " + input + " is already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                    break;
            }
            window.getSingleton(Resources.class).imageResources.get("default").put(input, image);
            window.setProjectChanged();
            model.addElement(input);
            imageList.setSelectedIndex(model.getSize() - 1);
        } catch (Exception e){
            JOptionPane.showMessageDialog(
                    this,
                    String.format("Error loading image %s.\n%s", file, e.getMessage()),
                    "Error loading image!",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void changeImage() {
        // TODO add change image
        if(imageList.getSelectedIndex() < 0)
            return;
    }

    private void removeImage(boolean showDeletionDialog) {
        if(imageList.getSelectedIndex() < 0)
            return;
        if(showDeletionDialog){
            var option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the Image Resource?\n" +
                            "All objects that use this resource will then display an error image!",
                    "Delete Image Resource",
                    JOptionPane.YES_NO_OPTION);
            if(option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION)
                return;
        }
        var model = ((DefaultListModel<String>) imageList.getModel());
        var index = imageList.getSelectedIndex();
        model.removeElementAt(index);
        if(model.getSize() > index)
            imageList.setSelectedIndex(index);
        else if(index - 1 >= 0)
            imageList.setSelectedIndex(index - 1);
    }

    private void onCategorySelected(){
        var model = (DefaultListModel<String>) imageList.getModel();
        model.clear();
        var selectedCat = categoryList.getSelectedValue();
        if(selectedCat != null) {
            window.getSingleton(Resources.class).imageResources.get(selectedCat).keySet().forEach(model::addElement);
            if(model.getSize() > 0)
                imageList.setSelectedIndex(0);
        }
        imageList.revalidate();
    }

    private void onListSelectionChanged(){
        var enable = imageList.getSelectedIndex() >= 0;
        selectionDependency.forEach(c -> c.setEnabled(enable));
        var imageID = imageList.getSelectedValue();
        selectedText.setText(imageID);
        imageComponent.repaint();
    }

    private JPanel genImagePanel(){
        return new JPanel(){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                var id = imageList.getSelectedValue();
                if(id == null)
                    return;
                var image = window.getSingleton(Resources.class).imageResources.get("default").get(id);
                if(image == null)
                    return;
                int x, y, width, height;
                width = (int)(image.getWidth() * zoom);
                height = (int)(image.getHeight() * zoom);
                x = getWidth() / 2 - width / 2;
                y = getHeight() / 2 - height / 2;
                g.drawImage(image, x, y, width, height, null);
            }
        };
    }

    private TransferHandler genTransferHandler(){
        return new TransferHandler(){

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if(!canImport(support))
                    return false;

                var transferable = support.getTransferable();
                try{
                    @SuppressWarnings("unchecked")
                    var files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if(files.isEmpty())
                        return false;
                    var file = files.get(0);
                    if(!formatSupported(file)){
                        JOptionPane.showMessageDialog(
                                ResourceImageView.this,
                                "The image format is not supported.",
                                "Unsupported format!",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return false;
                    }
                    loadNewImage(files.get(0));
                } catch (IOException | UnsupportedFlavorException e) {
                    JOptionPane.showMessageDialog(
                            ResourceImageView.this,
                            String.format("Error dragging image.\n%s", e.getMessage()),
                            "Error dragging image!",
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

}
