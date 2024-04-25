package de.sunnix.aje.editor.window.customswing;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class NumberPicker extends JPanel {

    private final JTextField text;
    private final JButton dec, inc;

    @Getter
    private int min, max;

    private final java.util.List<ChangeListener> changeListeners = new ArrayList<>();

    public NumberPicker(String title, int initialValue, int columns, int min, int max){
        this.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        this.text = new JTextField(Integer.toString(initialValue), columns);
        this.dec = new JButton("-");
        this.inc = new JButton("+");
        this.min = min;
        this.max = max;
        if(title != null){
            gbc.gridwidth = 3;
            add(new JLabel(title), gbc);
            gbc.gridy++;
        }
        gbc.gridwidth = 1;

//        dec.addActionListener(l -> changeValue(-1));
        dec.addMouseListener(genHoldingFunction(-1));
        add(dec, gbc);
        gbc.gridx++;

        text.setHorizontalAlignment(JTextField.CENTER);
        text.setEditable(false);
        add(text, gbc);
        gbc.gridx++;

//        inc.addActionListener(l -> changeValue(1));
        inc.addMouseListener(genHoldingFunction(1));
        add(inc, gbc);
    }

    public int getValue(){
        return Integer.parseInt(text.getText());
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
