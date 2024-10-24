package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.util.DialogUtils;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class EventListTreeView extends JTree{

    private static final Color b1 = UIManager.getColor("List.background");
    private static final Color b2 = b1.darker();

    /** used by Ctrl + C and Ctrl + V on Edit Object Dialog */
    private static IEvent copy;

    private final Window window;
    private final MapData map;
    private final GameObject object;

    private final Node root;

    public static JScrollPane create(Window window, EventList eventList, MapData map, GameObject object){
        var tree = new EventListTreeView(window, eventList, map, object);
        UIManager.put("Tree.expandedIcon", new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
        SwingUtilities.updateComponentTreeUI(tree);
        UIManager.put("Tree.expandedIcon", null);
        return new JScrollPane(tree);
    }

    private EventListTreeView(Window window, EventList eventList, MapData map, GameObject object){
        this.window = window;
        this.map = map;
        this.object = object;

        setOpaque(false);
        root = eventList.genListNode("root");

        setToggleClickCount(0);

        setModel(new DefaultTreeModel(root));
        reload();
        setRootVisible(false);
        setCellRenderer(genCellRenderer());

        var ml = genMouseListener(this);
        addMouseListener(ml);
        addMouseMotionListener(ml);

        addKeyListener(genKeyListener(this));
    }

    @Override
    protected void paintComponent(Graphics g) {
        var h = 1;
        var count = getWholeCountOf(root);
        for (int i = 0; i < count; i++) {
            var cH = getRowBounds(i).height;
            g.setColor(i % 2 == 0 ? b1 : b2);
            g.fillRect(0, h, getWidth(), cH);
            h += cH;
        }
        super.paintComponent(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1.5f));
        drawNodeLines(g2, this, root);
    }

    private int getWholeCountOf(TreeNode node){
        var count = node.getChildCount();
        var it = node.children();
        while (it.hasMoreElements())
            count += getWholeCountOf(it.nextElement());
        return count;
    }

    @Override
    public void collapsePath(TreePath path) {}

    private void reload(){
        ((DefaultTreeModel)getModel()).reload();
        for(int i = 0; i < getRowCount(); i++)
            expandRow(i);
    }

    private MouseAdapter genMouseListener(JTree parent){
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if(!isEnabled())
                    return;
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    showEventSelection();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if(!isEnabled() || !e.isPopupTrigger())
                    return;

                var path = getClosestPathForLocation(e.getX(), e.getY());
                setSelectionPath(path);

                var popupMenu = new JPopupMenu();

                var menuAdd = new JMenuItem(getString("dialog_object.add_event"));
                menuAdd.addActionListener(l -> showEventSelection());
                popupMenu.add(menuAdd);

                if(path != null){
                    var menuEditEvent = new JMenuItem(getString("dialog_object.edit_event"));
                    menuEditEvent.addActionListener(l -> showEditEventDialog(((Node)getLastSelectedPathComponent())));
                    popupMenu.add(menuEditEvent);
                    var menuRemoveEvent = new JMenuItem(getString("dialog_object.remove_event"));
                    menuRemoveEvent.addActionListener(l -> {
                        ((Node)getLastSelectedPathComponent()).removeEvent();
                        reload();
                    });
                    popupMenu.add(menuRemoveEvent);
                }

                popupMenu.show(parent, e.getX(), e.getY());
            }

            private void showEventSelection() {
                var event = EventSelectionDialog.show(window, DialogUtils.getWindowForComponent(parent), map, object);
                if(event == null)
                    return;
                var selectedNode = (Node) getLastSelectedPathComponent();
                if(selectedNode == null)
                    selectedNode = root;
                var newNode = selectedNode.addEvent(0, event);
                event.onChangeEvent(newNode);
                reload();
                setSelectionPath(new TreePath(newNode.getPath()));
            }

            private void showEditEventDialog(Node node){
                EventNode eNode = null;
                do {
                    if (node instanceof EventNode n)
                        eNode = n;
                } while (eNode == null && (node = (Node) node.getParent()) != null);
                setSelectionPath(new TreePath(eNode.getPath()));
                if(eNode.event.openDialog(window, DialogUtils.getWindowForComponent(parent), window.getSingleton(GameData.class), map, object)) {
                    if(eNode.event.onChangeEvent(eNode)) {
                        reload();
                        setSelectionPath(new TreePath(eNode.getPath()));
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(!isEnabled())
                    return;
                var path = getClosestPathForLocation(e.getX(), e.getY());
                setSelectionPath(path);
            }

        };
    }

    private KeyListener genKeyListener(JTree tree) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                var sNode = (Node) tree.getLastSelectedPathComponent();
                if(!e.isControlDown()) {
                    if (sNode != null && e.getKeyCode() == KeyEvent.VK_DELETE) {
                        if(JOptionPane.showConfirmDialog(tree, "Are you sure you want to delete this event?", "Delete event", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                        var index = tree.getRowForPath(new TreePath(sNode.getPath()));
                        sNode.removeEvent();
                        reload();
                        if(tree.getRowCount() > 0)
                            tree.setSelectionRow(index >= tree.getRowCount() ? index - 1 : index);
                    }
                    return;
                }
                if(e.getKeyCode() == KeyEvent.VK_V){
                    if(copy == null)
                        return;
                    var event = (IEvent) copy.clone();
                    if(sNode == null)
                        sNode = ((Node) tree.getModel().getRoot());
                    var newNode = sNode.addEvent(0, event);
                    reload();
                    tree.setSelectionRow(tree.getRowForPath(new TreePath(newNode.getPath())));
                    return;
                }
                if(sNode == null)
                    return;
                switch (e.getKeyCode()){
                    case KeyEvent.VK_UP -> sNode.moveEvent(true);
                    case KeyEvent.VK_DOWN -> sNode.moveEvent(false);
                    case KeyEvent.VK_C -> {
                        copy = (IEvent) sNode.getEvent().clone();
                        return;
                    }
                    case KeyEvent.VK_DELETE -> {
                        var index = tree.getRowForPath(new TreePath(sNode.getPath()));
                        sNode.removeEvent();
                        reload();
                        if(tree.getRowCount() > 0)
                            tree.setSelectionRow(index >= tree.getRowCount() ? index - 1 : index);
                        return;
                    }
                    default -> {
                        return;
                    }
                }
                reload();
                tree.setSelectionRow(tree.getRowForPath(new TreePath(sNode.getPath())));
            }
        };
    }

    private TreeCellRenderer genCellRenderer(){
        return (JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) -> {
            var label = new JLabel();
            if(value instanceof Node node){
                if(node instanceof EventNode eNode)
                    label.setText(eNode.event.getDisplayText(window, map));
                else
                    label.setText(node.name);
            }
            return label;
        };
    }

    private static void drawNodeLines(Graphics2D g, JTree tree, Node node){
        var count = node.getChildCount();
        if(count == 0)
            return;
        if(node.getParent() != null) {
            g.setColor(node.getLineColor());
            var pBounds = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
            for (int i = 0; i < count; i++) {
                var cBounds = tree.getRowBounds(tree.getRowForPath(new TreePath(((Node) node.getChildAt(i)).getPath())));
                g.drawLine(pBounds.x + 5, pBounds.y + pBounds.height, pBounds.x + 5, cBounds.y + cBounds.height / 2);
                g.drawLine(pBounds.x + 5, cBounds.y + cBounds.height / 2, cBounds.x - 5, cBounds.y + cBounds.height / 2);
            }
        }
        for(int i = 0; i < count; i++)
            drawNodeLines(g, tree, (Node)node.getChildAt(i));
    }

    public static abstract class Node extends DefaultMutableTreeNode {
        String name;

        public Node(String name){
            this.name = name;
        }

        public abstract IEvent getEvent();

        public abstract EventNode addEvent(int index, IEvent event);

        public abstract void removeEvent();

        public abstract void moveEvent(boolean up);
        public abstract Color getLineColor();

        public int getLocalIndex(){
            return getParent().getIndex(this);
        }

    }

    public static class ListNode extends Node {
        EventList list;

        public ListNode(EventList list, String name) {
            super(name);
            this.list = list;
        }

        @Override
        public IEvent getEvent() {
            return ((Node)getParent()).getEvent();
        }

        @Override
        public EventNode addEvent(int index, IEvent event) {
            list.getEvents().add(index, event);
            var node = new EventNode(event);
            insert(node, index);
            return node;
        }

        @Override
        public void removeEvent() {
            var parent = ((EventNode)getParent());
            ((ListNode)parent.getParent()).removeEvent(parent);
        }

        @Override
        public void moveEvent(boolean up) {
            ((Node)getParent()).moveEvent(up);
        }

        private void removeEvent(EventNode toRemove){
            list.getEvents().remove(toRemove.event);
            remove(toRemove);
        }

        @Override
        public Color getLineColor() {
            var parent = (Node) getParent();
            return parent == null ? null : parent.getLineColor();
        }

    }

    public static class EmptyNode extends Node {

        public EmptyNode(String name) {
            super(name);
        }

        @Override
        public IEvent getEvent() {
            return ((Node)getParent()).getEvent();
        }

        @Override
        public EventNode addEvent(int index, IEvent event) {
            return ((Node)getParent()).addEvent(index, event);
        }

        @Override
        public void removeEvent() {
            var parent = ((EventNode)getParent());
            ((ListNode)parent.getParent()).removeEvent(parent);
        }

        @Override
        public void moveEvent(boolean up) {
            ((Node)getParent()).moveEvent(up);
        }

        @Override
        public Color getLineColor() {
            var parent = (Node) getParent();
            return parent == null ? null : parent.getLineColor();
        }

    }

    public static class EventNode extends Node {

        IEvent event;

        public EventNode(IEvent event) {
            super(null);
            this.event = event;
        }

        @Override
        public IEvent getEvent() {
            return event;
        }

        @Override
        public EventNode addEvent(int index, IEvent event) {
            return ((Node)getParent()).addEvent(getLocalIndex() + 1, event);
        }

        @Override
        public void removeEvent() {
            ((ListNode)getParent()).removeEvent(this);
        }

        @Override
        public void moveEvent(boolean up) {
            var parent = (ListNode) getParent();
            var index = parent.getIndex(this);
            var list = parent.list;
            var events = list.getEvents();
            if(up) {
                if(index == 0)
                    return;
                parent.remove(this);
                parent.insert(this, index - 1);
                events.remove(index);
                events.add(index - 1, event);
            } else {
                if(index >= parent.getChildCount() - 1)
                    return;
                parent.remove(this);
                parent.insert(this, index + 1);
                events.remove(index);
                events.add(index + 1, event);
            }
        }

        @Override
        public Color getLineColor() {
            var c = event.getColor();
            c = c == null ? Color.LIGHT_GRAY : c;
            return c;
        }
    }

}
