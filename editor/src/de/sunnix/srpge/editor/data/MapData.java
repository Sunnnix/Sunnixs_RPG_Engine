package de.sunnix.srpge.editor.data;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

    @Getter
    @Setter
    private Parallax parallax;

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
        dso.putObject("parallax", parallax == null ? null : parallax.save(new DataSaveObject()));
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
        var pDSO = dso.getObject("parallax");
        if(pDSO != null) {
            parallax = new Parallax();
            parallax.load(pDSO);
        }
    }

    public void drawObjects(Window window, Graphics2D g, float zoom, float offsetX, float offsetY){
        objects.stream().sorted(Comparator.comparing(GameObject::getZ)).forEach(o -> o.draw(window, g, zoom, offsetX, offsetY, o.ID == selectedObject));
        if(window.getStartMap() == ID)
            window.getPlayer().draw(window, g, zoom, offsetX, offsetY, false);
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

    /**
     * Returns a {@link GameObject} that intersects the specified (x, y) coordinates.<br>
     * <br>
     * This method can either return the currently selected object if it intersects
     * the coordinates, or it can return the next object in the list that intersects
     * the coordinates, depending on the {@code goNext} parameter.
     *
     * @param x the x-coordinate to check for intersections
     * @param y the y-coordinate to check for intersections
     * @param selected the currently selected {@link GameObject}, used to determine
     *                 whether to return it or search for the next object or null
     * @param goNext if {@code true}, the method will return the next object that
     *               intersects the coordinates after the selected object is found.
     *               If {@code false}, the method will return the selected object if
     *               it intersects the coordinates and if not another object that intersects.
     *
     * @return the {@link GameObject} that intersects the (x, y) coordinates, or
     *         the next one if {@code goNext} is true. Returns {@code null} if no
     *         intersecting object is found.
     */
    public GameObject getObjectAt(float x, float y, GameObject selected, boolean goNext) {
        if(!goNext && selected != null && selected.intersects(x, y))
            return selected;
        GameObject first = null;
        var foundSelected = false;
        for(var o : objects.stream().sorted(Comparator.comparing(GameObject::getZ).reversed()).toList()){
            if(o.intersects(x, y)) {
                if(!goNext || foundSelected)
                    return o;
                if(first == null)
                    first = o;
                if(o.equals(selected))
                    foundSelected = true;
            }
        }
        return first;
    }

    public GameObject getObjectAt(float x, float y, GameObject selected) {
        return getObjectAt(x, y, selected, false);
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

    /**
     * Returns a copy of the objects list
     */
    public List<GameObject> getObjects() {
        return new ArrayList<>(objects);
    }

    public Tile getTile(int x, int y){
        if(x < 0 || x >= width || y < 0 || y >= height)
            return null;
        return tiles[x + y * width];
    }

    public void replaceTiles(int x, int y, int width, Tile[] tiles) {
        var height = tiles.length / width;
        for(var iX = 0; iX < width && iX + x < this.width; iX++)
            for(var iY = 0; iY < height && iY + y < this.height; iY++)
                this.tiles[(iX + x) + (iY + y) * this.width] = tiles[iX + iY * width].clone();
    }
}
