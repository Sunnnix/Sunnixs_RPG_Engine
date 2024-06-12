package de.sunnix.srpge.editor.window.menubar.resource;

import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Function;

import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.util.Texts.WINDOW_NAME;

public class ResourceDialog extends JDialog {

    private JPanel contentPanel;

    public ResourceDialog(Window parent) {
        super(parent, WINDOW_NAME + " - " + getString("dialog_resources.title"), true);
        setContentPane(createContent(parent));
        addWindowListener(genWindowListener());
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JPanel createContent(Window parent){
        var panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        var treeScroll = new JScrollPane(createTree(parent));
        treeScroll.setPreferredSize(new Dimension(200, 800));
        panel.add(treeScroll, BorderLayout.WEST);
        panel.add(contentPanel = new JPanel(new BorderLayout()), BorderLayout.CENTER);
        contentPanel.setPreferredSize(new Dimension(900, 0));

        return panel;
    }

    private JTree createTree(Window parent){
        var root = createNode("Root",
                createNode(getString("dialog_resources.node.video"),
                        createNode(getString("dialog_resources.node.video.images"), p -> new ResourceImageView(parent, p)),
                        createNode(getString("dialog_resources.node.video.tilesets"), p -> new TilesetView(parent, p))
                ),
                createNode(getString("dialog_resources.node.audio"),
                        createNode(getString("dialog_resources.node.audio.raw"), p -> new ResourceAudioView(parent, p))
                )
        );

        var tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(l -> {
            var node = (TreeNode)tree.getLastSelectedPathComponent();
            if (node instanceof InteractiveTreeNode interactiveNode) {
                if (interactiveNode.isLeaf())
                    changeContentView(interactiveNode.contentViewCreator);
            }
        });

        return tree;
    }

    private DefaultMutableTreeNode createNode(String name, DefaultMutableTreeNode... childs){
        var node = new DefaultMutableTreeNode(name);
        for(var child : childs)
            node.add(child);
        return node;
    }

    private DefaultMutableTreeNode createNode(String name, Function<JPanel, JComponent> contentViewCreator){
        return new InteractiveTreeNode(name, contentViewCreator);
    }

    private void changeContentView(Function<JPanel, JComponent> contentViewCreator) {
        for(var comp : contentPanel.getComponents())
            if(comp instanceof IResourceView resView)
                resView.onViewClosed();
        contentPanel.removeAll();
        var newComp = contentViewCreator.apply(contentPanel);
        contentPanel.add(newComp, BorderLayout.CENTER);
        if(newComp instanceof IResourceView resView)
            resView.onViewAttached();
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private WindowListener genWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for(var comp : contentPanel.getComponents())
                    if(comp instanceof IResourceView resView)
                        resView.onViewClosed();
            }
        };
    }

    private static class InteractiveTreeNode extends DefaultMutableTreeNode{

        public final Function<JPanel, JComponent> contentViewCreator;

        public InteractiveTreeNode(String name, Function<JPanel, JComponent> contentViewCreator){
            super(name);
            this.contentViewCreator = contentViewCreator;
        }

    }

}
