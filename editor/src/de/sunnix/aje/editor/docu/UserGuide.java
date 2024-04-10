package de.sunnix.aje.editor.docu;

import de.sunnix.aje.editor.window.Texts;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class UserGuide extends JFrame {

    private JTree tree;
    private JTextPane content;
    private JScrollPane contentScroll;

    public UserGuide(JFrame parent) {
        super(Texts.WINDOW_NAME + " - User Guide");
        setLayout(new BorderLayout(5, 5));

        tree = genTree();
        var scroll = new JScrollPane(tree);
        add(scroll, BorderLayout.WEST);

        add(contentScroll = new JScrollPane(content = genContentPane()), BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JTree genTree(){
        var root = new DefaultMutableTreeNode("root");

        root.add(
                genNode("Getting started",
                        genNode("Test 1", "Main.html"),
                        genNode("Test 2", "site 2")
                )
        );

        var tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setPreferredSize(new Dimension(200, 0));
        tree.addTreeSelectionListener(genTreeSelectionListener());
        return tree;
    }

    private MutableTreeNode genNode(String name, MutableTreeNode... nodes){
        var node = new DefaultMutableTreeNode(name);
        for(var n: nodes)
            node.add(n);
        return node;
    }

    private MutableTreeNode genNode(String name, String pagePath){
        return new HTMLTreeNode(name, pagePath);
    }

    private TreeSelectionListener genTreeSelectionListener(){
        return e -> {
            var node = (TreeNode)tree.getLastSelectedPathComponent();
            if(node instanceof HTMLTreeNode htmlNode)
                loadHTMLPage(htmlNode.pagePath);
        };
    }

    private JTextPane genContentPane(){
        var pane = new JTextPane();
        pane.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            UserGuide.this,
                            "An exception was thrown!\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        return pane;
    }

    private void loadHTMLPage(String path){
        try(var stream = getClass().getResourceAsStream("/de/sunnix/aje/editor/docu/html/" + path)){
            var text = new String(stream.readAllBytes());
            content.setText(text);
            content.setCaretPosition(0);
        } catch (IOException e) {
            content.setText(e.toString());
            JOptionPane.showMessageDialog(
                    UserGuide.this,
                    "An exception was thrown!\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (NullPointerException e){
            content.setText(e.toString());
            JOptionPane.showMessageDialog(
                    UserGuide.this,
                    "Html page " + path + " not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static class HTMLTreeNode extends DefaultMutableTreeNode {

        public final String pagePath;

        public HTMLTreeNode(String name, String pagePath){
            super(name);
            this.pagePath = pagePath;
        }

    }

}
