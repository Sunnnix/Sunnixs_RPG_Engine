package de.sunnix.srpge.editor.window;

import de.sunnix.srpge.editor.data.GameData;
import de.sunnix.srpge.editor.data.MapData;
import de.sunnix.srpge.editor.window.customswing.NumberPicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class PropertiesView extends JPanel {

    private final Window window;

    private final GridBagConstraints gbc;

    // Components
    private boolean loadingData;
    private NumberPicker wallDrawLayer;
    private NumberPicker floorY, wallHeight;
    private JCheckBox shiftWalls;

    private JComboBox<String> slopeDirection;

    public PropertiesView(Window window) {
        this.window = window;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder((String)null));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.insets.set(0,3,3,3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setup();

        onLoadMap(null);
    }

    private void setup(){
        setupHeights();
    }

    private void setupHeights() {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;

        wallDrawLayer = new NumberPicker(getString("view.properties.wall_layer"), 0, 10, 0, 0);
        wallDrawLayer.addChangeListener(this::wallDrawLayerChanged);
        add(wallDrawLayer, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        floorY = new NumberPicker(getString("view.properties.floor_y"), 0, 2, 0, 255);
        floorY.addChangeListener(this::floorHeightChanged);
        add(floorY, gbc);

        gbc.gridx++;

        wallHeight = new NumberPicker(getString("view.properties.wall_height"), 0, 2, 0, 255);
        wallHeight.addChangeListener(this::wallHeightChanged);
        add(wallHeight, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        shiftWalls = new JCheckBox("Shift walls");
        add(shiftWalls, gbc);
        gbc.gridy++;

        slopeDirection = new JComboBox<>(new String[]{ "None", "South", "East", "West", "North" });
        slopeDirection.addActionListener(this::slopeDirectionChanged);
        add(slopeDirection, gbc);
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
                tile.setWallHeight(height, shiftWalls.isSelected());
            }
        mapView.repaint();
        window.setProjectChanged();
    }

    private void slopeDirectionChanged(ActionEvent actionEvent) {
        if(loadingData)
            return;
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
                tile.setSlopeDirection(slopeDirection.getSelectedIndex());
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
        loadingData = true;
        var sTiles = map.getSelectedTiles();
        var tile = map.getTiles()[sTiles[0] + sTiles[1] * map.getWidth()];
        floorY.setValue(tile.getgroundY(), true);
        wallHeight.setValue(tile.getWallHeight(), true);
        slopeDirection.setSelectedIndex(tile.getSlopeDirection());
        loadingData = false;
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
