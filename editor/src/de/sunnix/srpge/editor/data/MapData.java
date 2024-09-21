package de.sunnix.srpge.editor.data;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.sunnix.srpge.editor.lang.Language.getString;

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
    private String backgroundMusic;
    @Getter
    @Setter
    private int selectedTileset;
    @Getter
    private int[] selectedTilesetTile = {0, 0, 1, 1};

    @Getter
    private int[] selectedTiles = {0, 0, 1, 1};

    private List<GameObject> objects = new ArrayList<>();

    @Getter
    @Setter
    private int selectedObject = -1;

    public MapData(int id, int width, int height){
        this.ID = id;
        this.width = Math.max(width, MINIMUM_WIDTH);
        this.height = Math.max(height, MINIMUM_HEIGHT);
        this.tiles = new Tile[width * height];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile();
    }

    public MapData(DataSaveObject dso, int[] version) {
        loadMap(dso, version);
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

    public void setSelectedTilesetTile(int tileset, int index, int width, int height) {
        selectedTilesetTile[0] = tileset;
        selectedTilesetTile[1] = index;
        selectedTilesetTile[2] = width;
        selectedTilesetTile[3] = height;
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
        dso.putString("bgm", backgroundMusic);
        dso.putList("objects", objects.stream().map(go -> go.save(new DataSaveObject())).toList());
    }

    private void loadMap(DataSaveObject dso, int[] version) {
        ID = dso.getInt("id", -1);
        name = dso.getString("name", null);
        width = dso.getInt("width", MINIMUM_WIDTH);
        height = dso.getInt("height", MINIMUM_HEIGHT);
        tilesets = dso.getArray("tilesets", String[]::new);
        tiles = new Tile[width * height];
        var tileList = dso.<DataSaveObject>getList("tiles");
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile(tileList.get(i), version);
        }
        backgroundMusic = dso.getString("bgm", null);
        objects.addAll(dso.<DataSaveObject>getList("objects").stream().map(o -> new GameObject(o, version)).toList());
    }

    public void drawObjects(Window window, Graphics2D g, float zoom, int offsetX, int offsetY){
        objects.forEach(o -> o.draw(window, g, zoom, offsetX, offsetY, o.ID == selectedObject));
    }

    public GameObject createNewObject(float x, float y) {
        var obj = new GameObject(genNextID(), x, 0, y);
        objects.add(obj);
        return obj;
    }

    public int genNextID(){
        var it = objects.stream().mapToInt(GameObject::getID).sorted().iterator();
        var nextID = 0;
        while(it.hasNext()){
            var oID = it.next();
            if(oID <= nextID)
                nextID = oID + 1;
            else
                break;
        }
        if(nextID < 0)
            throw new RuntimeException("no free id's!");
        else
            return nextID;
    }

    public GameObject getObjectAt(float x, float y) {
        for(var o : objects){
            if(o.intersects(x, y))
                return o;
        }
        return null;
    }

    public GameObject getObject(int id) {
        if(id < 0)
            return null;
        for(var obj : objects)
            if(obj.ID == id)
                return obj;
        return null;
    }

    public GameObject removeObject(GameObject obj) {
        objects.remove(obj);
        return obj;
    }

    public GameObject removeObject(int id){
        return removeObject(getObject(id));
    }

    public List<GameObject> getObjects() {
        return new ArrayList<>(objects);
    }
}
