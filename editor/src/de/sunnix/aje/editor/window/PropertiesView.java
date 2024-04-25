package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.data.GameData;
import de.sunnix.aje.editor.data.MapData;
import de.sunnix.aje.editor.window.customswing.NumberPicker;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class PropertiesView extends JPanel {

    private final Window window;

    private final GridBagConstraints gbc;

    // Components
    private NumberPicker wallDrawLayer;
    private NumberPicker floorY, wallHeight;

    public PropertiesView(Window window) {
        this.window = window;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder((String)null));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;

        setup();

        onLoadMap(null);
    }

    @Override
    public Component add(Component comp) {
        add(comp, gbc);
        gbc.gridy++;
        return comp;
    }

    private void setup(){
        setupHeights();
    }

    private void setupHeights() {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.ipadx = 12;

        wallDrawLayer = new NumberPicker("Wall Layer:", 0, 10, 0, 0);
        wallDrawLayer.addChangeListener(this::wallDrawLayerChanged);
        panel.add(wallDrawLayer, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        floorY = new NumberPicker("Floor Y:", 0, 2, 0, 255);
        floorY.addChangeListener(this::floorHeightChanged);
        panel.add(floorY, gbc);

        gbc.gridx++;

        wallHeight = new NumberPicker("Wall height:", 0, 2, 0, 255);
        wallHeight.addChangeListener(this::wallHeightChanged);
        panel.add(wallHeight, gbc);

        add(panel);
    }

    private void wallDrawLayerChanged(NumberPicker numberPicker, int oldValue, int value) {
        var mapView = window.getMapView();
        if(mapView == null)
            return;
        mapView.repaint();
    }

    private void floorHeightChanged(NumberPicker numberPicker, int oldValue, int value) {
        var mapView = window.getMapView();
        if(mapView == null)
            return;
        var map = window.getSingleton(GameData.class).getMap(mapView.getMapID());
        if(map == null)
            return;
        var sTiles = map.getSelectedTiles();
        var tiles = map.getTiles();
        for (int x = sTiles[0]; x < sTiles[0] + sTiles[2]; x++)
            for (int y = sTiles[1]; y < sTiles[1] + sTiles[3]; y++){
                var tile = tiles[x + y * map.getWidth()];
                var ground = tile.getgroundY() + (value - oldValue);
                if(ground < 0)
                    ground = 0;
                if(ground > 255)
                    ground = 255;
                tile.setGroundY(ground);
            }
        mapView.repaint();
        window.setProjectChanged();
    }

    private void wallHeightChanged(NumberPicker numberPicker, int oldValue, int value) {
        var mapView = window.getMapView();
        if(mapView == null)
            return;
        var map = window.getSingleton(GameData.class).getMap(mapView.getMapID());
        if(map == null)
            return;
        var sTiles = map.getSelectedTiles();
        var tiles = map.getTiles();
        for (int x = sTiles[0]; x < sTiles[0] + sTiles[2]; x++)
            for (int y = sTiles[1]; y < sTiles[1] + sTiles[3]; y++){
                var tile = tiles[x + y * map.getWidth()];
                var height = tile.getWallHeight() + (value - oldValue);
                if(height < 0)
                    height = 0;
                if(height > 255)
                    height = 255;
                tile.setWallHeight(height);
            }
        mapView.repaint();
        window.setProjectChanged();
    }

    private void changeTfComp(JTextField textField, int i) {
        var value = Integer.parseInt(textField.getText());
        value += i;
        if(value < 0)
            value = 0;
        textField.setText(Integer.toString(value));
    }

    public void loadSelectedTileData() {
        var mapView = window.getMapView();
        if(mapView == null)
            return;
        var map = window.getSingleton(GameData.class).getMap(mapView.getMapID());
        if(map == null)
            return;
        var sTiles = map.getSelectedTiles();
        var tile = map.getTiles()[sTiles[0] + sTiles[1] * map.getWidth()];
        floorY.setValue(tile.getgroundY(), true);
        wallHeight.setValue(tile.getWallHeight(), true);
    }

    public void onLoadMap(MapData map) {
        if(map == null) {
            enableChilds(this, false);
            return;
        }
        enableChilds(this, true);
        wallDrawLayer.setMax(map.getHeight() - 1);
    }

    private void enableChilds(JComponent comp, boolean enabled){
        for(var c: comp.getComponents())
            if(c instanceof JComponent jc)
                enableChilds(jc, enabled);
        comp.setEnabled(enabled);
    }

    public int getWallDrawLayer(){
        return wallDrawLayer.getValue();
    }

}
