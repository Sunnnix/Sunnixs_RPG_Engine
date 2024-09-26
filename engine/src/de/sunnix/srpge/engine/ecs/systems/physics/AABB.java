package de.sunnix.srpge.engine.ecs.systems.physics;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.Tile;
import lombok.Getter;
import org.joml.Vector3f;

import static de.sunnix.srpge.engine.util.FunctionUtils.EPSILON;

@Getter
public class AABB {

    private final float x, y, z;
    private final float width, height;

    public AABB(float x, float y, float z, float width, float height){
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    public AABB(GameObject go){
        this(go.getPosition().x, go.getPosition().y, go.getPosition().z, go.size.x, go.size.y);
    }

    public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ){
        this(
                maxX - (maxX - minX) / 2,
                minY,
                maxZ - (maxZ - minZ) / 2,
                maxX - minX,
                maxY - minY
        );
    }

    public AABB transform(float x, float y, float z){
        return new AABB(this.x + x, this.y + y, this.z + z, this.width, this.height);
    }

    public AABB resize(float w, float h){
        return new AABB(x, y, z, w, h);
    }

    public boolean intersects(AABB other) {
        if(other instanceof TileAABB tile)
            tile.prepare(this);
        if(this instanceof TileAABB tile)
            tile.prepare(other);
        return this.getMaxX() > other.getMinX() + EPSILON &&
                this.getMinX() < other.getMaxX() - EPSILON &&
                this.getMaxY() >= other.getMinY() + EPSILON &&
                this.getMinY() < other.getMaxY()  - EPSILON &&
                this.getMaxZ() > other.getMinZ() + EPSILON &&
                this.getMinZ() < other.getMaxZ() - EPSILON;
    }

    @Override
    public String toString() {
        return String.format("AABB(%.2f - %.2f, %.2f - %.2f, %.2f - %.2f)", getMinX(), getMaxX(), getMinY(), getMaxY(), getMinZ(), getMaxZ());
    }

    public float getMinX(){
        return x - width / 2;
    }

    public float getMaxX(){
        return x + width / 2;
    }

    public float getMinY(){
        return y;
    }

    public float getMaxY(){
        return y + height;
    }

    public float getMinZ(){
        return z - width / 2;
    }

    public float getMaxZ(){
        return z + width / 2;
    }

    public AABB alignRight(AABB other){
        return new AABB(other.getMinX() - width / 2, y, z, width, height);
    }

    public AABB alignLeft(AABB other){
        return new AABB(other.getMaxX() + width / 2, y, z, width, height);
    }

    public AABB alignBottom(AABB other){
        return new AABB(x, y, other.getMinZ() - width / 2, width, height);
    }

    public AABB alignTop(AABB other){
        return new AABB(x, y, other.getMaxZ() + width / 2, width, height);
    }

    public AABB alignUp(AABB other){
        return new AABB(x, other.getY() - height, z, width, height);
    }

    public AABB alignDown(AABB other){
        return new AABB(x, other.getMaxY(), z, width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof AABB other) {
            return  obj == this ||
                    width == other.width &&
                    height == other.height &&
                    x + EPSILON / 2 >= other.getX() &&
                    x - EPSILON / 2 <= other.getX() &&
                    y + EPSILON / 2 >= other.getY() &&
                    y - EPSILON / 2 <= other.getY() &&
                    z + EPSILON / 2 >= other.getZ() &&
                    z - EPSILON / 2 <= other.getZ();
        } else
            return false;
    }

    public Vector3f getDistance(AABB other) {
        var vec = new Vector3f();
        if(!other.equals(this))
            vec.set(other.x - x, other.y - y, other.z - z);
        return vec;
    }

    public static class TileAABB extends AABB {

        private final byte slopeDirection;
        private float maxY;

        public TileAABB(int x, int z, int height, byte slopeDirection){
            super(x, 0, z, 1, height);
            this.slopeDirection = slopeDirection;
        }

        public void prepare(AABB other){
            maxY = switch (slopeDirection){
                case Tile.SLOPE_DIRECTION_NORTH -> super.getMaxY() - 1 + Math.min(1, getMaxZ() - other.getMinZ());
                case Tile.SLOPE_DIRECTION_EAST -> super.getMaxY() - 1 + Math.min(1, getMaxX() - other.getMinX());
                case Tile.SLOPE_DIRECTION_WEST -> super.getMaxY() - 1 + Math.min(1, other.getMaxX() - getMinX());
                case Tile.SLOPE_DIRECTION_SOUTH -> super.getMaxY() - 1 + Math.min(1, other.getMaxZ() - getMinZ());
                default -> super.getMaxY();
            };
        }

        @Override
        public float getMaxY() {
            return maxY;
        }

    }

}