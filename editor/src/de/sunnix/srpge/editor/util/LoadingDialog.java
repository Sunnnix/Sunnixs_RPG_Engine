package de.sunnix.srpge.editor.util;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class LoadingDialog extends JDialog {

    private boolean requestClosing;
    private JProgressBar progressBar;
    @Setter
    @Getter
    private Object result;

    @Getter
    private int maxProgress = 100;

    LoadingDialog(Frame owner, String title, Object process){
        super(owner, title, true);
        setUp(owner, process);
    }

    LoadingDialog(Dialog owner, String title, Object process){
        super(owner, title, true);
        setUp(owner, process);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setUp(Component owner, Object process){
        setUpView();

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(genWindowListener());
        new Thread(() -> {
            if(process instanceof Consumer consumer)
                consumer.accept(LoadingDialog.this);
            else if(process instanceof Function func)
                result = func.apply(LoadingDialog.this);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            if(LoadingDialog.this.isVisible())
                LoadingDialog.this.dispose();
        }).start();
        setVisible(true);
    }

    private void setUpView(){
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, maxProgress);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200 ,progressBar.getPreferredSize().height));
        add(progressBar, gbc);
    }

    public void setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
        progressBar.setMaximum(maxProgress);
    }

    public void setProgress(int progress){
        progressBar.setValue(progress);
    }

    public void addProgress(int progress){
        progressBar.setValue(progressBar.getValue() + progress);
    }

    private WindowAdapter genWindowListener(){
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                requestClosing = true;
            }
        };
    }

}
