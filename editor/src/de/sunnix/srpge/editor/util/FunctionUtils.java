package de.sunnix.srpge.editor.util;

import de.sunnix.srpge.editor.window.customswing.DefaultValueComboboxModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public class FunctionUtils {

    public static <T> T firstOrNull(Collection<T> collection, Function<T, Boolean> expression){
        for(var item: collection){
            if(expression.apply(item))
                return item;
        }
        return null;
    }

    public static <T> T first(Collection<T> collection, Function<T, Boolean> expression){
        var value = firstOrNull(collection, expression);
        if(value == null)
            throw new NullPointerException("No matching value with this expression");
        return value;
    }

    public static <T> T firstOrElse(Collection<T> collection, Function<T, Boolean> expression, T elseValue){
        var value = firstOrNull(collection, expression);
        return value == null ? elseValue : value;
    }

    public static JMenuItem createMenuItem(String name, ActionListener al){
        var mi = new JMenuItem(name);
        mi.addActionListener(al);
        return mi;
    }

    public static JButton createButton(String name, String unselectedIcon, String selectedIcon, ActionListener al){
        var mi = new JButton(name);
        if(unselectedIcon != null){
            try(var stream = FunctionUtils.class.getResourceAsStream("/de/sunnix/srpge/editor/window/icons/" + unselectedIcon)){
                var icon = new ImageIcon(ImageIO.read(stream));
                mi.setIcon(icon);
                mi.setText(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(selectedIcon != null){
            try(var stream = FunctionUtils.class.getResourceAsStream("/de/sunnix/srpge/editor/window/icons/" + selectedIcon)){
                var icon = new ImageIcon(ImageIO.read(stream));
                mi.setSelectedIcon(icon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mi.addActionListener(al);
        return mi;
    }

    public static JButton createButton(String name, String iconLocation, ActionListener al){
        return createButton(name, iconLocation, null, al);
    }

    public static JButton createButton(String name, ActionListener al){
        return createButton(name, null, al);
    }

    public static <T> JComboBox<T> createComboBox(T defaultValue, T[] content){
        return new JComboBox<>(new DefaultValueComboboxModel<>(defaultValue, content));
    }

}
