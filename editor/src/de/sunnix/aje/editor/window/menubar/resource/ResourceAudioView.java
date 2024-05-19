package de.sunnix.aje.editor.window.menubar.resource;

import de.sunnix.aje.editor.util.DialogUtils;
import de.sunnix.aje.editor.window.Window;
import de.sunnix.aje.editor.window.resource.Resources;
import de.sunnix.aje.editor.window.resource.audio.AudioResource;
import de.sunnix.aje.editor.window.resource.audio.AudioSpeaker;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import static de.sunnix.aje.editor.util.FunctionUtils.createButton;
import static de.sunnix.aje.editor.util.FunctionUtils.createMenuItem;

public class ResourceAudioView extends JPanel implements IResourceView{

    private final Window window;
    private final JPanel parent;
    private JList<String> categories;
    private JPanel listPanel;

    public ResourceAudioView(Window window, JPanel parent){
        this.window = window;
        this.parent = parent;
        setLayout(new BorderLayout());
        create();
        setTransferHandler(genTransferHandler());
    }

    private TransferHandler genTransferHandler(){
        return new TransferHandler(){

            @Override
            public boolean canImport(TransferSupport support) {
                return categories.getSelectedIndex() != -1 && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support))
                    return false;

                var transferable = support.getTransferable();
                try {
                    @SuppressWarnings("unchecked")
                    var files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    DialogUtils.showLoadingDialog(parent, "Loading audio files", pb -> {
                        try {
                            var changed = false;
                            var percent = 1f / files.size();
                            var res = window.getSingleton(Resources.class);
                            for (var file : files) {
                                pb.addProgress((int) (percent * 10000));
                                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                                do {
                                    name = (String) JOptionPane.showInputDialog(parent, "Input audio name:", "Rename audio", JOptionPane.PLAIN_MESSAGE, null, null, name);
                                    if (name == null)
                                        break;
                                } while (!DialogUtils.validateInput(parent, name, res.audio_getAllNames(categories.getSelectedValue())));
                                if(name == null)
                                    continue;
                                var audio = new AudioResource(name, file.getPath());
                                window.getSingleton(Resources.class).audio_add(categories.getSelectedValue(), audio);
                                listPanel.add(new AudioView(new AudioSpeaker(audio), audio.getName()));
                                changed = true;
                            }
                            listPanel.revalidate();
                            listPanel.repaint();
                            if(changed)
                                window.setProjectChanged();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(
                                    parent,
                                    String.format("Error dragging audio.\n%s", e.getMessage()),
                                    "Error dragging audio!",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            e.printStackTrace();
                        }
                    });
                    return true;
                } catch (Exception e){
                    JOptionPane.showMessageDialog(
                            parent,
                            String.format("Error dragging audio.\n%s", e.getMessage()),
                            "Error dragging audio!",
                            JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    private void create(){
        var model = new DefaultListModel<String>();
        categories = new JList<>(model);

        window.getSingleton(Resources.class).audio_getCategories().forEach(model::addElement);

        categories.setSelectedIndex(-1);
        categories.addListSelectionListener(l -> {
            unloadAudioViews();
            listPanel.removeAll();
            if(categories.getSelectedIndex() != -1)
                window.getSingleton(Resources.class).audio_getAll(categories.getSelectedValue())
                        .forEach(audio -> listPanel.add(new AudioView(new AudioSpeaker(audio), audio.getName())));
            listPanel.revalidate();
            listPanel.repaint();
        });

        categories.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3)
                    showCategoryPopupMenu(categories, e.getX(), e.getY());
            }
        });

        var scroll = new JScrollPane(categories);
        scroll.setBorder(BorderFactory.createTitledBorder("Categories:"));
        scroll.setPreferredSize(new Dimension(120, 0));
        add(scroll, BorderLayout.WEST);

        var mPanel = new JPanel(new BorderLayout());
        listPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        mPanel.add(listPanel, BorderLayout.NORTH);

        scroll = new JScrollPane(mPanel);
        add(scroll, BorderLayout.CENTER);
    }

    private void showCategoryPopupMenu(JList<String> categories, int x, int y) {
        var sCat = categories.getSelectedValue();
        new JPopupMenu(sCat){
            {
                add(createMenuItem("Create", this::createCategory));
                if(sCat != null){
                    var label = new JLabel(sCat);
                    label.setBorder(BorderFactory.createEmptyBorder(3, 5,5,3));
                    add(label, 0);
                    add(new JSeparator(JSeparator.HORIZONTAL), 1);
                    add(createMenuItem("Rename", this::renameCategory));
                    add(createMenuItem("Delete", this::deleteCategory));
                }
            }

            private void createCategory(ActionEvent e) {
                var res = window.getSingleton(Resources.class);
                String name = null;
                do {
                    name = (String) JOptionPane.showInputDialog(parent, "Input category name:", "Create new category", JOptionPane.PLAIN_MESSAGE, null, null, name);
                    if (name == null)
                        return;
                } while (!DialogUtils.validateInput(parent, name, res.audio_getCategories()));
                res.audio_addCategory(name);
                ((DefaultListModel<String>)categories.getModel()).addElement(name);
                categories.setSelectedValue(name, true);
                window.setProjectChanged();
            }

            private void renameCategory(ActionEvent e) {
                if(JOptionPane.showConfirmDialog(
                        parent,
                        "Do you want to rename the category?\n" +
                                "All components that use sounds in this category could have errors.",
                        "Rename category",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.YES_OPTION)
                    return;
                var res = window.getSingleton(Resources.class);
                var preName = categories.getSelectedValue();
                String name = preName;
                do {
                    name = (String) JOptionPane.showInputDialog(parent, "Input category name:", "Create new category", JOptionPane.PLAIN_MESSAGE, null, null, name);
                    if (name == null || name.equals(preName))
                        return;
                } while (!DialogUtils.validateInput(parent, name, res.audio_getCategories()));
                var cat = res.audio_removeCategory(preName);
                res.audio_addCategory(name, cat);
                var model = (DefaultListModel<String>) categories.getModel();
                var index = model.indexOf(preName);
                model.removeElement(preName);
                model.add(index, name);
                categories.setSelectedIndex(index);
                window.setProjectChanged();
            }

            private void deleteCategory(ActionEvent e) {
                if(JOptionPane.showConfirmDialog(
                        parent,
                        "Do you really want to delete the category?\n" +
                        "All components that use sounds in this category could have errors.",
                        "Delete category",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.YES_OPTION)
                    return;
                window.getSingleton(Resources.class).audio_removeCategory(categories.getSelectedValue(), true);
                ((DefaultListModel<String>)categories.getModel()).remove(categories.getSelectedIndex());
                window.setProjectChanged();
            }

        }.show(categories, x, y);
    }

    private void unloadAudioViews(){
        for(var comp: listPanel.getComponents())
            if(comp instanceof AudioView av)
                av.destroy();
    }

    @Override
    public void onViewAttached() {}

    @Override
    public void onViewClosed() {
        unloadAudioViews();
    }

    private class AudioView extends JPanel{

        private final AudioSpeaker speaker;

        private String name;

        private JLabel label;
        private JSlider slider;
        private JButton play;
        private JLabel timestamp;
        private boolean ignoreUpdate;

        private String maxTime;

        private Thread thread;

        public AudioView(AudioSpeaker speaker, String name){
            this.speaker = speaker;
            this.name = name;
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createTitledBorder((String) null));
            create();
        }

        private void create(){
            var panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            var gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 8;
            gbc.gridheight = 1;
            gbc.weightx = .8;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 5, 0, 0);

            label = new JLabel();
            label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
            if("wav".equals(speaker.getAudio().getExtension()))
                label.setForeground(Color.YELLOW.brighter());
            genLabel();
            panel.add(label, gbc);
            gbc.gridwidth = 1;
            gbc.gridx = 8;
            gbc.weightx = .1;

            var type = new JLabel(speaker.getAudio().channels == 1 ? " Mono " : " Stereo ", JLabel.CENTER);
            type.setBorder(BorderFactory.createLineBorder(type.getForeground().darker(), 2, true));
            panel.add(type);
            gbc.gridx++;

            var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            JButton rename, delete;
            rename = createButton("R", "audio/rename.png", this::renameAudio);
            rename.setPreferredSize(new Dimension(24, 24));
            delete = createButton("T", "audio/trash.png", this::deleteAudio);
            delete.setPreferredSize(new Dimension(24, 24));

            buttons.add(rename);
            buttons.add(delete);
            panel.add(buttons, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 10;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            var l = speaker.getSize();

            slider = new JSlider(JSlider.HORIZONTAL, 0, l, 0);
            slider.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON1)
                        ignoreUpdate = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if(e.getButton() == MouseEvent.BUTTON1) {
                        ignoreUpdate = false;
                    }
                }
            });

            slider.addChangeListener(cl -> {
                if(!ignoreUpdate)
                    return;
                speaker.setPosition(slider.getValue());
                reloadTimestamp();
            });

            panel.add(slider, gbc);
            gbc.gridy++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;

            play = new JButton("Play");
            play.addActionListener(this::pressPlay);
            var stop = new JButton("Stop");
            stop.addActionListener(this::stop);

            gbc.gridwidth = 1;
            panel.add(play, gbc);
            gbc.gridx++;
            panel.add(stop, gbc);
            gbc.gridx++;

            panel.add(new JLabel("Default Volume:"), gbc);
            gbc.gridx++;

            var gain = new JSlider(JSlider.HORIZONTAL, 0, 200, (int)(speaker.getGain() * 100));
            gain.setMajorTickSpacing(50);
            gain.setMinorTickSpacing(10);
            gain.setPaintTrack(true);
            gain.setPaintLabels(true);
            gain.setPaintTicks(true);
            gain.addChangeListener(cl -> {
                var g = gain.getValue() / 100f;
                speaker.getAudio().setDefaultGain(g);
                speaker.setGain(g);
                window.setProjectChanged();
            });

            gbc.gridwidth = 2;

            panel.add(gain, gbc);

            gbc.gridwidth = 2;
            gbc.gridx = 8;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            l = speaker.getLengthInMS();

            maxTime = String.format("%02d:%02d.%01d", l / 1000 / 60, l / 1000 % 60, l % 1000 / 100);

            timestamp = new JLabel();
            reloadTimestamp();
            timestamp.setHorizontalAlignment(JLabel.RIGHT);
            gbc.insets.right = 15;
            panel.add(timestamp, gbc);

            add(panel, BorderLayout.CENTER);
        }

        private void genLabel(){
            label.setText("[" + speaker.getAudio().getRawSizeInText() + "] " + name);
        }

        private void renameAudio(ActionEvent e) {
            if(JOptionPane.showConfirmDialog(
                    parent,
                    "Do you want to rename the audio?\n" +
                            "All components that use this audio could have errors.",
                    "Rename audio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            ) != JOptionPane.YES_OPTION)
                return;
            var res = window.getSingleton(Resources.class);
            String name = this.name;
            do {
                name = (String) JOptionPane.showInputDialog(parent, "Input audio name:", "Rename audio", JOptionPane.PLAIN_MESSAGE, null, null, name);
                if (name == null || name.equals(this.name))
                    return;
            } while (!DialogUtils.validateInput(parent, name, res.audio_getAllNames(categories.getSelectedValue())));
            var audio = res.audio_remove(categories.getSelectedValue(), this.name);
            audio.setName(name);
            this.name = name;
            genLabel();
            res.audio_add(categories.getSelectedValue(), audio);
            revalidate();
            repaint();
            window.setProjectChanged();
        }

        private void deleteAudio(ActionEvent e) {
            if(JOptionPane.showConfirmDialog(
                    parent,
                    "Do you want to rename the audio?\n" +
                            "All components that use this audio could have errors.",
                    "Rename audio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            ) != JOptionPane.YES_OPTION)
                return;
            window.getSingleton(Resources.class).audio_remove(categories.getSelectedValue(), name, true);
            destroy();
            listPanel.remove(this);
            listPanel.revalidate();
            listPanel.repaint();
            window.setProjectChanged();
        }

        private void reloadTimestamp(){
            var l = speaker.getPositionInMS();
            timestamp.setText(String.format("%02d:%02d.%01d / %s", l / 1000 / 60, l / 1000 % 60, l % 1000 / 100, maxTime));
        }

        private void pressPlay(ActionEvent e) {
            if(!speaker.isPlaying()) {
                play.setText("Pause");
                speaker.play();
                if (thread != null)
                    thread.interrupt();
                thread = new Thread(this::updateView, String.format("Audio Tracker (%s)", name));
                thread.setDaemon(true);
                thread.start();
            } else {
                play.setText("Play");
                speaker.pause();
            }
        }

        private void stop(ActionEvent e) {
            speaker.stop();
            if(thread != null) {
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            slider.setValue(0);
            reloadTimestamp();
        }

        private void updateView() {
            var waiter = 0;
            while(!Thread.interrupted() && speaker.isPlaying()){
                if(ignoreUpdate)
                    waiter = 3;
                else if(waiter > 0)
                    waiter--;
                else
                    slider.setValue(speaker.getPosition());
                reloadTimestamp();
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    break;
                }
            }
            play.setText("Play");
            thread = null;
        }

        public void destroy(){
            if(thread != null && thread.isAlive())
                thread.interrupt();
            speaker.cleanup();
        }

    }

}
