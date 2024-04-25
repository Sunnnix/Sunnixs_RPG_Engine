package de.sunnix.aje.editor.data;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class MapData {

    public static final int MINIMUM_WIDTH = 12;
    public static final int MINIMUM_HEIGHT = 7;
    @Getter
    private int ID;

    @Getter
    @Setter
    private String name;

    @Getter
    private int width, height;

    @Getter
    private Tile[] tiles;

    @Getter
    @Setter
    private String[] tilesets = new String[0];

    @Getter
    @Setter
    private int selectedTileset;
    @Getter
    private int[] selectedTilesetTile = {-1, -1};

    @Getter
    private int[] selectedTiles = {0, 0, 1, 1};

    public MapData(int id, int width, int height){
        this.ID = id;
        this.width = Math.max(width, MINIMUM_WIDTH);
        this.height = Math.max(height, MINIMUM_HEIGHT);
        this.tiles = new Tile[width * height];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile();
    }

    public MapData(DataSaveObject dso) {
        loadMap(dso);
    }

    public void setTileset(String tileset){
        this.tilesets = new String[] { tileset };
    }

    public void setSize(int width, int height) {
        var tilesOLD = tiles;
        var tilesNEW = new Tile[width * height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                if(x >= this.width || y >= this.height)
                    tilesNEW[x + y * width] = new Tile();
                else
                    tilesNEW[x + y * width] = tilesOLD[x + y * this.width];
            }
        this.tiles = tilesNEW;
        this.width = width;
        this.height = height;
    }

    public void setSelectedTilesetTile(int tileset, int index) {
        selectedTilesetTile[0] = tileset;
        selectedTilesetTile[1] = index;
    }

    @Override
    public String toString() {
        return String.format("%04d - %s (%s, %s)", getID(), getName() == null || getName().isEmpty() ? "Map" : getName(), getWidth(), getHeight());
    }

    public void saveMap(DataSaveObject dso) {
        dso.putInt("id", ID);
        dso.putString("name", name);
        dso.putInt("width", width);
        dso.putInt("height", height);
        dso.putArray("tilesets", tilesets);
        dso.putList("tiles", Arrays.stream(tiles).map(x -> {
            var tileDSO = new DataSaveObject();
            x.saveTile(tileDSO);
            return tileDSO;
        }).toList());
    }

    private void loadMap(DataSaveObject dso) {
        ID = dso.getInt("id", -1);
        name = dso.getString("name", null);
        width = dso.getInt("width", MINIMUM_WIDTH);
        height = dso.getInt("height", MINIMUM_HEIGHT);
        tilesets = dso.getArray("tilesets", String[]::new);
        tiles = new Tile[width * height];
        var tileList = dso.<DataSaveObject>getList("tiles");
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile(tileList.get(i));
        }
    }
}
