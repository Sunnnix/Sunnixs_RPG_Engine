package de.sunnix.srpge.engine.ecs.systems;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The MapGrid class manages a grid-based map for spatial partitioning
 * of game objects. It organizes objects into grid cells based on their position
 * to allow efficient querying of nearby objects and to track dirty chunks (grid
 * cells that need updates).
 */
public class MapGrid {

    /** The size of each grid cell in the map. */
    private static final int MAP_GRID_SIZE = 5;
    /** The width of the map in grid cells. */
    private int mapWidth;
    /** The height of the map in grid cells. */
    private int mapHeight;
    /** A grid storing lists of {@link GameObject} in each cell. */
    private List<GameObject>[] mapGrid;

    /** A set of dirty chunks (cells that need to be updated). */
    private Set<Integer> dirtyChunks = new HashSet<>();

    /**
     * Constructs a new MapGrid with the given dimensions.
     *
     * @param width  the width of the map in world units.
     * @param height the height of the map in world units.
     */
    public MapGrid(int width, int height) {
        initMapGrid(width, height);
    }

    /**
     * Initializes the map grid with the specified dimensions. Each grid cell is
     * represented by a list of game objects.
     *
     * @param mapWidth  the width of the map in world units.
     * @param mapHeight the height of the map in world units.
     */
    public void initMapGrid(int mapWidth, int mapHeight){
        this.mapWidth = (int)Math.ceil((float)mapWidth / MAP_GRID_SIZE);
        this.mapHeight = (int)Math.ceil((float)mapHeight / MAP_GRID_SIZE);
        mapGrid = new List[mapWidth * mapHeight];
        for(var i = 0; i < mapGrid.length; i++)
            mapGrid[i] = new ArrayList<>();
    }

    /**
     * Returns a list of game objects from the grid cells that match the given object's
     * position. It retrieves nearby objects based on the object's position.
     *
     * @param obj the game object whose neighboring grid cells are to be queried.
     * @return a list of game objects in the objects and neighboring grid cells.
     */
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

        list.removeIf(go -> !go.isEnabled());

        return list;
    }

    /**
     * Adds the object to the map grid at the map grid position
     * @param object the object to add
     */
    public void initObject(GameObject object) {
        var pos = object.getPosition();
        var gX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.x / MAP_GRID_SIZE));
        var gZ = Math.min(mapHeight / MAP_GRID_SIZE - 1,Math.max(0, (int) pos.z / MAP_GRID_SIZE));
        mapGrid[gX + gZ * mapWidth / MAP_GRID_SIZE].add(object);
        markDirty(gX, gZ);
    }

    /**
     * Relocates a game object from one grid cell to another based on its previous
     * and new positions. Marks both the old and new cells as dirty.
     *
     * @param prePos the previous position of the object.
     * @param newPos the new position of the object.
     * @param object the game object to relocate.
     */
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

    public void removeObject(GameObject object){
        var pos = object.getPosition();
        var gX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.x / MAP_GRID_SIZE));
        var gZ = Math.min(mapHeight / MAP_GRID_SIZE - 1,Math.max(0, (int) pos.z / MAP_GRID_SIZE));
        mapGrid[gX + gZ * mapWidth / MAP_GRID_SIZE].remove(object);
    }

    /**
     * Marks the grid cell containing the given object as dirty, indicating it
     * requires updates.
     *
     * @param go the game object to mark the containing cell as dirty.
     */
    public void markDirty(GameObject go){
        var pos = go.getPosition();
        var x = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.x / MAP_GRID_SIZE));
        var z = Math.min(mapHeight / MAP_GRID_SIZE - 1, Math.max(0, (int) pos.z / MAP_GRID_SIZE));
        markDirty(x, z);
    }

    /**
     * Marks a grid cell and its surrounding neighbors as dirty.
     *
     * @param x the x-coordinate of the grid cell.
     * @param z the z-coordinate of the grid cell.
     */
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

    /**
     * Marks a single grid cell as dirty.
     *
     * @param x the x-coordinate of the grid cell.
     * @param z the z-coordinate of the grid cell.
     */
    private void _markDirty(int x, int z){
        var mW = mapWidth / MAP_GRID_SIZE;
        dirtyChunks.add(Math.max(0, Math.min(mW - 1, x)) + Math.max(0, Math.min(mapHeight / MAP_GRID_SIZE - 1, z)) * mW);
    }

    /**
     * Retrieves all game objects from the grid cells marked as dirty and clears
     * the dirty chunks set.
     *
     * @return a list of game objects in the dirty grid cells.
     */
    public List<GameObject> getDirtyObjects(){
        var list = new ArrayList<GameObject>();
        for(var dirtyChunk: dirtyChunks)
            list.addAll(mapGrid[dirtyChunk]);
        list.removeIf(go -> !go.isEnabled());
        dirtyChunks.clear();
        return list;
    }

    /**
     * Returns a list of game objects within the grid cells intersecting the given hitbox.
     *
     * @param x the x position in world units.
     * @param z the z position in world units.
     * @return a list of game objects within the positions grid cells.
     */
    public List<GameObject> getObjectFieldOf(float x, float z) {
        var list = new ArrayList<GameObject>();

        var mX = Math.min(mapWidth / MAP_GRID_SIZE - 1, Math.max(0, (int) x / MAP_GRID_SIZE));
        var mZ = Math.min(mapHeight / MAP_GRID_SIZE - 1, Math.max(0, (int) z / MAP_GRID_SIZE));

        for(var fX = Math.max(0, mX - 1); fX < Math.min((mapWidth - 1) / MAP_GRID_SIZE, mX + 3); fX++)
            for(var fZ = Math.max(0, mZ - 1); fZ <= Math.min((mapHeight - 1) / MAP_GRID_SIZE, mZ + 3); fZ++)
                list.addAll(mapGrid[fX + fZ * mapWidth / MAP_GRID_SIZE]);

        list.removeIf(go -> !go.isEnabled());

        return list;
    }

}
