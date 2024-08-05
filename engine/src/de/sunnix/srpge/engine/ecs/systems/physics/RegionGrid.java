package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.components.PhysicComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionGrid {
    private static final int REGION_SIZE = 1000;
    private Map<String, List<GameObject>> grid;

    public RegionGrid() {
        this.grid = new HashMap<>();
    }

    private String getCellKey(float x, float z) {
        int cellX = (int) Math.floor(x / REGION_SIZE);
        int cellZ = (int) Math.floor(z / REGION_SIZE);
        return cellX + "," + cellZ;
    }

    public void addObject(GameObject obj) {
        var hb = obj.getComponent(PhysicComponent.class).getHitbox();
        String key = getCellKey(hb.getMinX(), hb.getMinZ());
        grid.computeIfAbsent(key, k -> new ArrayList<>()).add(obj);
    }

    public void clear() {
        grid.clear();
    }

    public List<GameObject> getPossibleCollisions(GameObject obj) {
        var hb = obj.getComponent(PhysicComponent.class).getHitbox();
        var possibleCollisions = new ArrayList<GameObject>();
        String[] keys = {
                getCellKey(hb.getMinX(), hb.getMinZ()),
                getCellKey(hb.getMaxX(), hb.getMinZ()),
                getCellKey(hb.getMinX(), hb.getMinZ()),
                getCellKey(hb.getMaxX(), hb.getMinZ()),
                getCellKey(hb.getMinX(), hb.getMaxZ()),
                getCellKey(hb.getMaxX(), hb.getMaxZ()),
                getCellKey(hb.getMinX(), hb.getMaxZ()),
                getCellKey(hb.getMaxX(), hb.getMaxZ())
        };
        for (var key : keys) {
            var cellObjects = grid.get(key);
            if (cellObjects != null) {
                for (var cellObject : cellObjects) {
                    if (!possibleCollisions.contains(cellObject)) {
                        possibleCollisions.add(cellObject);
                    }
                }
            }
        }
        return possibleCollisions;
    }
}
