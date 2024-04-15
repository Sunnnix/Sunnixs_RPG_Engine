package de.sunnix.aje.editor.window.menubar.resource;

import de.sunnix.aje.editor.window.Window;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Function;

import static de.sunnix.aje.editor.util.Texts.WINDOW_NAME;

public class ResourceDialog extends JDialog {

    private JPanel contentPanel;

    public ResourceDialog(Window parent) {
        super(parent, WINDOW_NAME + " - Resources", true);
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
                createNode("Video",
                        createNode("Images", p -> new ResourceImageView(parent, p))
                ),
                createNode("Audio",
                        createNode("Raw", p -> new JPanel())
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
