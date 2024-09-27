package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.Window;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.joml.Math.lerp;

public class GlobalColorTintEvent extends de.sunnix.srpge.engine.ecs.event.GlobalColorTintEvent implements IEvent {

    public GlobalColorTintEvent(){
        color = new float[4];
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putArray("color", color);
        dso.putInt("delay", maxDelay);
        if(parallel)
            dso.putBool("parallel", true);
        return dso;
    }

    @Override
    public String getGUIText(Window window, MapData map) {
        return String.format("color /cv00 /b (%.2f, %.2f, %.2f, %.2f) /n /cx in /cv00 /b %s /n /cx frames", color[0], color[1], color[2], color[3], maxDelay);
    }

    @Override
    public String getMainColor() {
        return "/cff8";
    }

    @Override
    public String getEventDisplayName() {
        return "Global Tint";
    }

    @Override
    public Runnable createEventEditDialog(Window window, GameData gameData, MapData map, GameObject go, JPanel content) {
        content.setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.insets.set(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        final JSpinner[] spinners = new JSpinner[4];
        var panel = setupPreview(content, gbc, spinners);
        gbc.gridy++;
        System.arraycopy(setupSpinners(content, gbc), 0, spinners, 0, spinners.length);
        var panelRepaintChangeListener = (ChangeListener)(l -> panel.repaint());
        for(var spinner: spinners)
            spinner.addChangeListener(panelRepaintChangeListener);
        var sliders = setupSliders(content, gbc);
        var delay = setupDelaySpinner(content, gbc);

        for(var i = 0; i < spinners.length; i++)
            bindSpinnerToSlider(spinners[i], sliders[i]);

        var runParallelCheck = new JCheckBox("Run parallel", parallel);
        content.add(runParallelCheck, gbc);

        return () -> {
            color[0] = ((Number)spinners[0].getValue()).floatValue();
            color[1] = ((Number)spinners[1].getValue()).floatValue();
            color[2] = ((Number)spinners[2].getValue()).floatValue();
            color[3] = ((Number)spinners[3].getValue()).floatValue();
            maxDelay = ((Number)delay.getValue()).intValue();
            parallel = runParallelCheck.isSelected();
        };
    }

    private JSpinner[] setupSpinners(JPanel content, GridBagConstraints gbc) {
        gbc.weightx = .25;
        var r = createColorSpinner(content, gbc, "Red", color[0], -1, Color.RED);
        var g = createColorSpinner(content, gbc, "Green", color[1], -1, Color.GREEN);
        var b = createColorSpinner(content, gbc, "Blue", color[2], -1, Color.BLUE);
        var s = createColorSpinner(content, gbc, "Strength", color[3], 0, null);
        gbc.gridx = 0;
        gbc.gridy += 2;
        return new JSpinner[] { r, g, b, s };
    }

    private JSpinner createColorSpinner(JPanel content, GridBagConstraints gbc, String text, float value, float min, Color color){
        var label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.CENTER);
        content.add(label, gbc);
        gbc.gridy++;
        var spinner = new JSpinner(new SpinnerNumberModel(value, min, 1, .05));
        if(color != null)
            spinner.setForeground(color);
        spinner.setFont(spinner.getFont().deriveFont(Font.BOLD));
        content.add(spinner, gbc);
        gbc.gridy--;
        gbc.gridx++;
        return spinner;
    }

    private JSlider[] setupSliders(JPanel content, GridBagConstraints gbc){
        var r = createColorSlider(content, gbc, "Red", color[0], -100, Color.RED);
        var g = createColorSlider(content, gbc, "Green", color[1], -100, Color.GREEN);
        var b = createColorSlider(content, gbc, "Blue", color[2], -100, Color.BLUE);
        var s = createColorSlider(content, gbc, "Strength", color[3], 0, null);
        return new JSlider[] { r, g, b, s };
    }

    private JSlider createColorSlider(JPanel content, GridBagConstraints gbc, String text, float value, int min, Color color){
        content.add(new JLabel(text), gbc);
        gbc.gridx++;
        gbc.gridwidth = 3;
        var slider = new JSlider(JSlider.HORIZONTAL, min, 100, (int)(value * 100));
        slider.setForeground(color);
        content.add(slider, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        return slider;
    }

    private JSpinner setupDelaySpinner(JPanel content, GridBagConstraints gbc) {
        content.add(new JLabel("Delay"), gbc);
        gbc.gridx++;
        gbc.gridwidth = 3;
        var spinner = new JSpinner(new SpinnerNumberModel(maxDelay, 0, Integer.MAX_VALUE, 1));
        content.add(spinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        return spinner;
    }

    private void bindSpinnerToSlider(JSpinner spinner, JSlider slider){
        spinner.addChangeListener(l -> {
            var listener = slider.getChangeListeners()[0];
            slider.removeChangeListener(listener);
            slider.setValue((int)(((Number)spinner.getValue()).doubleValue() * 100));
            slider.addChangeListener(listener);
        });
        slider.addChangeListener(l -> {
            var listener = spinner.getChangeListeners()[1];
            spinner.removeChangeListener(listener);
            spinner.setValue(slider.getValue() / 100d);
            spinner.addChangeListener(listener);
        });
    }

    private JPanel setupPreview(JPanel content, GridBagConstraints gbc, JSpinner[] spinners) {
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        var panel = new JPanel(){

            BufferedImage image;

            {
                try (var stream = getClass().getResourceAsStream("/de/sunnix/srpge/editor/window/images/color_example.png")) {
                    image = ImageIO.read(stream);
                } catch (Exception e){
                    e.printStackTrace();
                }
                getInsets().set(5, 0, 10, 0);
                if(image != null)
                    setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            }

            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                if(image != null){
                    var newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    for (int x = 0; x < image.getWidth(); x++) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            int pixel = image.getRGB(x, y);

                            var red = ((pixel >> 16) & 0xff) / 255f;
                            var green = ((pixel >> 8) & 0xff) / 255f;
                            var blue = (pixel & 0xff) / 255f;

                            var strength = ((Number)spinners[3].getValue()).floatValue();
                            red = lerp(red, ((Number)spinners[0].getValue()).floatValue(), strength);
                            green = lerp(green, ((Number)spinners[1].getValue()).floatValue(), strength);
                            blue = lerp(blue, ((Number)spinners[2].getValue()).floatValue(), strength);

                            red = Math.min(1, Math.max(0, red));
                            green = Math.min(1, Math.max(0, green));
                            blue = Math.min(1, Math.max(0, blue));

                            int newPixel = (255 << 24) | ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | (int)(blue * 255);
                            newImage.setRGB(x, y, newPixel);
                        }
                    }

                    graphics.drawImage(newImage, getWidth() / 2 - newImage.getWidth() / 2, getHeight() / 2 - newImage.getHeight() / 2, null);
                }
            }
        };
        content.add(panel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        return panel;
    }

}
