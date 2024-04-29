package de.sunnix.aje.editor.window.menubar.resource;

import de.sunnix.aje.editor.window.Window;

import javax.swing.*;
import java.awt.*;

public class TilesetView extends JPanel implements IResourceView{

    private final Window window;

    private DefaultListModel<String> model;
    private JList<String> list;

    public TilesetView(Window window, JPanel parent){
        this.window = window;
        setLayout(new BorderLayout(5, 5));
        add(setupListView(), BorderLayout.WEST);
        add(setupImageView(), BorderLayout.CENTER);
        add(setupPropertiesView(), BorderLayout.EAST);
    }

    private JScrollPane setupListView(){
        model = new DefaultListModel<>();
        list = new JList<>(model);
        var scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createTitledBorder("Tilesets:"));
        scroll.setPreferredSize(new Dimension(120, 0));
        return scroll;
    }

    private JScrollPane setupImageView(){
        var panel = new JPanel(null){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
            }
        };

        return new JScrollPane(panel);
    }

    private JScrollPane setupPropertiesView(){
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();


        var scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(200, 0));
        return scroll;
    }

    private void updateList(){

    }

    @Override
    public void onViewAttached() {
        updateList();
    }

    @Override
    public void onViewClosed() {

    }

}
