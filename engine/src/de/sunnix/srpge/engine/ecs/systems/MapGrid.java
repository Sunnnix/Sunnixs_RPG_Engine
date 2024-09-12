package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.ecs.GameObject;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapGrid {

    private static final int MAP_GRID_SIZE = 5;
    private int mapWidth, mapHeight;
    private List<GameObject>[] mapGrid;

    private Set<Integer> dirtyChunks = new HashSet<>();

    public MapGrid(int width, int height) {
        initMapGrid(width, height);
    }

    public void initMapGrid(int mapWidth, int mapHeight){
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        mapGrid = new List[mapWidth * mapHeight];
        for(var i = 0; i < mapGrid.length; i++)
            mapGrid[i] = new ArrayList<>();
    }

    public List<GameObject> getMatchingMapGridObjets(GameObject obj){
        var list = new ArrayList<GameObject>();

        var pos = obj.getPosition();
        var mX = (int) pos.x;
        var mZ = (int) pos.z;
        mX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, mX / MAP_GRID_SIZE));
        mZ = Math.min(mapHeight / MAP_GRID_SIZE - 1, Math.max(0, mZ / MAP_GRID_SIZE));

        for(var x = Math.max(0, mX - 1); x < Math.min((mapWidth - 1) / MAP_GRID_SIZE, mX + 3); x++)
            for(var z = Math.max(0, mZ - 1); z <= Math.min((mapHeight - 1) / MAP_GRID_SIZE ,mZ + 3); z++)
                list.addAll(mapGrid[x + z * mapWidth / MAP_GRID_SIZE]);

        return list;
    }

    public void relocateGridObject(Vector3f prePos, Vector3f newPos, GameObject object) {
        var preX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) prePos.x / MAP_GRID_SIZE));
        var preZ = Math.min(mapHeight / MAP_GRID_SIZE - 1,Math.max(0, (int) prePos.z / MAP_GRID_SIZE));
        mapGrid[preX + preZ * mapWidth / MAP_GRID_SIZE].remove(object);
        markDirty(preX, preZ);

        var newX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) newPos.x / MAP_GRID_SIZE));
        var newZ = Math.min(mapHeight / MAP_GRID_SIZE - 1,Math.max(0, (int) newPos.z / MAP_GRID_SIZE));
        mapGrid[newX + newZ * mapWidth / MAP_GRID_SIZE].add(object);
        markDirty(newX, newZ);
    }

    public void markDirty(GameObject go){
        var pos = go.getPosition();
        var x = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.x / MAP_GRID_SIZE));
        var z = Math.min(mapHeight / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.z / MAP_GRID_SIZE));
        markDirty(x, z);
    }

    public void markDirty(int x, int z){
        _markDirty(x - 1, z - 1);
        _markDirty(x, z - 1);
        _markDirty(x + 1, z - 1);
        _markDirty(x - 1, z);
        _markDirty(x, z);
        _markDirty(x + 1, z);
        _markDirty(x - 1, z + 1);
        _markDirty(x, z + 1);
        _markDirty(x + 1, z + 1);
    }

    private void _markDirty(int x, int z){
        var mW = mapWidth / MAP_GRID_SIZE;
        dirtyChunks.add(Math.max(0, Math.min(mW - 1, x)) + Math.max(0, Math.min(mapHeight / MAP_GRID_SIZE - 1, z)) * mW);
    }

    public List<GameObject> getDirtyObjects(){
        var list = new ArrayList<GameObject>();
        for(var dirtyChunk: dirtyChunks)
            list.addAll(mapGrid[dirtyChunk]);
        dirtyChunks.clear();
        return list;
    }

}
