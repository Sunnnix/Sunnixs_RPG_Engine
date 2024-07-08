package de.sunnix.srpge.editor.window.customswing;

import lombok.Getter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class NumberPicker extends JPanel {

    private final JTextField text;
    private final JButton dec, inc;

    @Getter
    private int min, max, value;

    private final java.util.List<ChangeListener> changeListeners = new ArrayList<>();

    public NumberPicker(String title, int initialValue, int columns, int min, int max){
        this.setLayout(new BorderLayout());
        this.text = new JTextField(Integer.toString(initialValue), columns);
        this.dec = new JButton("-");
        this.inc = new JButton("+");
        this.min = min;
        this.max = max;
        if(title != null)
            add(new JLabel(title), BorderLayout.NORTH);

        dec.addMouseListener(genHoldingFunction(-1));
        add(dec, BorderLayout.WEST);

        text.setHorizontalAlignment(JTextField.CENTER);
        text.setEditable(false);
        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(text.getText().isBlank())
                    return;
                value = Integer.parseInt(text.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {}

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        add(text, BorderLayout.CENTER);

        inc.addMouseListener(genHoldingFunction(1));
        add(inc, BorderLayout.EAST);
    }

    public void setValue(int value, boolean ignoreListeners){
        var oldValue = Integer.parseInt(text.getText());
        text.setText(Integer.toString(value));
        if(!ignoreListeners)
            changeListeners.forEach(l -> l.onChange(this, oldValue, value));
    }

    public void setValue(int value){
        setValue(value, false);
    }

    public void changeValue(int change){
        var oldValue = Integer.parseInt(text.getText());
        var value = oldValue;
        value += change;
        if(value < min)
            value = min;
        else if(value > max)
            value = max;
        text.setText(Integer.toString(value));
        int finalValue = value;
        changeListeners.forEach(l -> l.onChange(this, oldValue, finalValue));
    }

    private MouseListener genHoldingFunction(int i) {
        return new MouseAdapter() {

            private Thread thread;

            @Override
            public void mousePressed(MouseEvent e) {
                if(!isEnabled())
                    return;
                if(e.getButton() != MouseEvent.BUTTON1)
                    return;
                if(thread != null && thread.isAlive())
                    thread.interrupt();
                changeValue(i);
                thread = new Thread(() -> {
                    float mult = .6f;
                    float nextValue = 0;
                    while (true){
                        if(Thread.currentThread().isInterrupted())
                            return;
                        try {
                            nextValue += mult;
                            if(nextValue >= 1){
                                changeValue((int)nextValue * i);
                                nextValue = nextValue - (int)nextValue;
                            }
                            mult *= 1.015f;
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() != MouseEvent.BUTTON1)
                    return;
                if(thread != null && thread.isAlive())
                    thread.interrupt();
            }
        };
    }

    public void setMin(int min){
        this.min = min;
        if(Integer.parseInt(text.getText()) < min)
            text.setText(Integer.toString(min));
    }

    public void setMax(int max){
        this.max = max;
        if(Integer.parseInt(text.getText()) > max)
            text.setText(Integer.toString(max));
    }

    public void addChangeListener(ChangeListener l){
        changeListeners.add(l);
    }

    @Override
    public void setEnabled(boolean enabled) {
        inc.setEnabled(enabled);
        dec.setEnabled(enabled);
    }

    public ChangeListener[] getChangeListeners(){
        return changeListeners.toArray(ChangeListener[]::new);
    }

    public void removeChangeListener(ChangeListener l){
        changeListeners.remove(l);
    }

    public interface ChangeListener {
        void onChange(NumberPicker src, int preValue, int postValue);
    }

}
