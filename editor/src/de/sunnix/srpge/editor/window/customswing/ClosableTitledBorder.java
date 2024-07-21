package de.sunnix.srpge.editor.window.customswing;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A custom TitledBorder with a close button in the top right corner.
 */
public class ClosableTitledBorder extends TitledBorder {

    private boolean hovered;
    private boolean pressed;
    private int closeX, closeY, size;

    /**
     * Constructs a ClosableTitledBorder with the specified title.
     *
     * @param title the title to be displayed in the border.
     */
    public ClosableTitledBorder(String title) {
        super(title);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);

        // Set color based on the state of the close button
        if (pressed) {
            g.setColor(c.getBackground().brighter());
        } else if (hovered) {
            g.setColor(c.getBackground().darker());
        } else {
            g.setColor(c.getBackground());
        }

        // Draw the close button
        size = 14;
        closeX = x - 4 + width - size;
        closeY = y + 1;
        g.fillOval(closeX, closeY, size, size);

        // Set color for the 'X' mark based on hover state
        g.setColor(hovered ? c.getForeground() : c.getForeground().darker());
        Graphics2D g2d = (Graphics2D) g;
        Stroke defaultStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g.drawLine(closeX + 4, closeY + 4, closeX + size - 4, closeY + size - 4);
        g.drawLine(closeX + size - 4, closeY + 4, closeX + 4, closeY + size - 4);
        g2d.setStroke(defaultStroke);
    }

    /**
     * Checks if the given coordinates intersect with the close button.
     *
     * @param x the x coordinate to check.
     * @param y the y coordinate to check.
     * @return true if the coordinates intersect with the close button, false otherwise.
     */
    public boolean intersectsClose(int x, int y) {
        return x >= closeX && x <= closeX + size && y >= closeY && y <= closeY + size;
    }

    /**
     * Updates the hover state of the close button.
     *
     * @param x the x coordinate of the mouse.
     * @param y the y coordinate of the mouse.
     * @return true if the hover state changed, false otherwise.
     */
    public boolean refreshHover(int x, int y) {
        boolean isHovered = intersectsClose(x, y);
        boolean changed = this.hovered != isHovered;
        this.hovered = isHovered;
        return changed;
    }

    /**
     * Updates the press state of the close button.
     *
     * @param x the x coordinate of the mouse.
     * @param y the y coordinate of the mouse.
     * @param startPress true if the mouse is pressed, false if released.
     * @return true if the close button was pressed, false otherwise.
     */
    public boolean refreshPress(int x, int y, boolean startPress) {
        boolean isHovered = intersectsClose(x, y);
        boolean wasPressed = !startPress && isHovered && this.pressed;
        this.pressed = startPress && isHovered;
        return wasPressed;
    }

    /**
     * Creates a JPanel with a ClosableTitledBorder and attaches the necessary mouse listeners.
     *
     * @param title the title of the border.
     * @param onClosing a Consumer to handle the closing action.
     * @return the created JPanel.
     */
    public static JPanel createClosableTitledPanel(String title, Consumer<JPanel> onClosing) {
        JPanel panel = new JPanel();
        ClosableTitledBorder border = new ClosableTitledBorder(title);
        panel.setBorder(border);

        MouseAdapter ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                border.refreshPress(e.getX(), e.getY(), true);
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (border.refreshPress(e.getX(), e.getY(), false))
                    onClosing.accept(panel);
                panel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (border.refreshHover(e.getX(), e.getY()))
                    panel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (border.refreshHover(e.getX(), e.getY())) {
                    panel.repaint();
                }
            }
        };

        panel.addMouseListener(ml);
        panel.addMouseMotionListener(ml);
        return panel;
    }
}
