package de.sunnix.srpge.editor.docu;

import de.sunnix.srpge.editor.util.Texts;
import de.sunnix.srpge.editor.window.Config;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

public class UserGuide extends JFrame {

    private final Window parent;
    private JTree tree;
    private DefaultMutableTreeNode root;
    private JTextPane content;
    private JScrollPane contentScroll;

    private String userGuidePath;

    public UserGuide(Window parent) {
        super(Texts.WINDOW_NAME + " - User Guide");
        this.parent = parent;
        setLayout(new BorderLayout(5, 5));

        var sPanel = new JPanel(new BorderLayout());

        tree = genTree();
        var scroll = new JScrollPane(tree);
        sPanel.add(scroll, BorderLayout.CENTER);

        userGuidePath = parent.getSingleton(Config.class).get("user_guide_path", (String) null);
        if(userGuidePath == null) {
            if (!changeUserGuidePath(null)) {
                return;
            }
        } else {
            setUpTree(new File(userGuidePath));
        }

        var chooseBtn = new JButton("Select UserGuide folder");
        chooseBtn.addActionListener(l -> changeUserGuidePath(parent.getSingleton(Config.class).get("user_guide_path", (String)null)));
        sPanel.add(chooseBtn, BorderLayout.SOUTH);

        add(sPanel, BorderLayout.WEST);

        add(contentScroll = new JScrollPane(content = genContentPane()), BorderLayout.CENTER);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JTree genTree(){
        root = new DefaultMutableTreeNode("root");

        var tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setPreferredSize(new Dimension(200, 0));
        tree.addTreeSelectionListener(genTreeSelectionListener());

        return tree;
    }

    private TreeSelectionListener genTreeSelectionListener(){
        return e -> {
            var node = (TreeNode)tree.getLastSelectedPathComponent();
            if(node instanceof HTMLTreeNode htmlNode)
                loadHTMLPage(htmlNode.pagePath);
        };
    }

    private boolean changeUserGuidePath(String path){
        var fc = new JFileChooser(path);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return false;
        var directory = fc.getSelectedFile();
        var config = new File(directory, "UserGuide.json");
        if(!config.exists()){
            JOptionPane.showMessageDialog(
                    this,
                    "The folder doesn't have a UserGuide.json!",
                    "Invalid folder!",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        var done = setUpTree(directory);
        if(done) {
            parent.getSingleton(Config.class).set("user_guide_path", directory.toString());
            userGuidePath = directory.toString();
        }
        return done;
    }

    private boolean setUpTree(File path){
        BetterJSONObject json;
        try (var stream = new FileInputStream(new File(path, "UserGuide.json"))){
            var src = new String(stream.readAllBytes());
            json = new BetterJSONObject(src);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    parent,
                    "Error reading UserGuide.json:\n" + e.getMessage(),
                    "Error!",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        root.removeAllChildren();
        var keys = json.keys();
        while(keys.hasNext()){
            var key = keys.next();
            var jsonNode = json.get(key);
            if(jsonNode instanceof JSONObject jObj)
                root.add(jsonToNode(key, jObj));
            else if(jsonNode instanceof String str)
                root.add(new HTMLTreeNode(key, str));
        }
        tree.setModel(new DefaultTreeModel(root));
        return true;
    }

    private MutableTreeNode jsonToNode(String key, JSONObject json){
        var node = new DefaultMutableTreeNode(key);
        var keys = json.keys();
        while(keys.hasNext()) {
            var k = keys.next();
            var jsonNode = json.get(k);
            if (jsonNode instanceof JSONObject jObj)
                node.add(jsonToNode(k, jObj));
            else if (jsonNode instanceof String str)
                node.add(new HTMLTreeNode(k, str));
        }
        return node;
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
        var rawFile = new File(userGuidePath, path);
        try(var stream = new FileInputStream(rawFile)){
            var text = new String(stream.readAllBytes());
            text = text.replace("src=\"file:///", "src=\"file:///" + (rawFile.getParentFile().toString().replace('\\', '/')) + "/");
            var document = ((HTMLDocument)content.getDocument());
            var styles = document.getStyleNames().asIterator();
            while(styles.hasNext()) {
                var style = styles.next();
                if(style instanceof String sStyle)
                    document.removeStyle(sStyle);
            }
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
