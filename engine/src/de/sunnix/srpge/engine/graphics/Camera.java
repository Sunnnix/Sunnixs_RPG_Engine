package de.sunnix.srpge.engine.graphics;

import de.sunnix.srpge.engine.ecs.GameObject;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static de.sunnix.srpge.engine.Core.TILE_HEIGHT;
import static de.sunnix.srpge.engine.Core.TILE_WIDTH;

/**
 * The `Camera` class manages the camera's position and projection in the game world.<br>
 * It provides functions to attach the camera to an object, calculate positions,
 * and update the projection and view matrices for rendering.
 */
public class Camera {

    /**
     * Flag indicating whether the camera is attached to an object.<br>
     * If the camera is attached to an object, it will try to move towards the object.
     */
    @Getter
    @Setter
    private static boolean attachObject = true;
    /**
     * The object the camera is attached to.
     */
    @Getter
    @Setter
    private static GameObject attachedObject;

    @Getter
    private static Matrix4f projection = new Matrix4f();
    @Getter
    private static Matrix4f view = new Matrix4f();
    @Getter
    private static Vector2f size = new Vector2f();
    @Getter
    private static Vector2f pos = new Vector2f();

    /**
     * Updates the camera's view and projection matrices. The view matrix adjusts
     * based on the camera's current position, centering the camera in the viewport.
     */
    public static void process(){
        view = new Matrix4f().translate(-pos.x + size.x / 2, -pos.y + size.y / 2, -1999 + 20);
//        projection = new Matrix4f().ortho(0, size.x, 0, size.y, 0, 1e10f);
        projection = new Matrix4f().ortho(0, size.x, 0, size.y, -20, 2000);
    }

    /**
     * Calculates and adjusts the camera's position based on the attached object's position, when {@link #attachObject} is true and am {@link #attachedObject object} is attached.
     * The camera smoothly follows the object with a slight delay, determined by a factor.
     */
    public static void calculateCameraPosition(){
        if(!attachObject || attachedObject == null || !attachedObject.isValid())
            return;

        var tPos = attachedObject.getPosition();

        var xTarget = tPos.x * TILE_WIDTH;
        var yTarget = (-tPos.z + tPos.y) * TILE_HEIGHT;
        var xDiff = xTarget - pos.x;
        var yDiff = yTarget - pos.y;

        float factor = 0.075f;

        pos.x += xDiff * factor;
        pos.y += yDiff * factor;
    }

    /**
     * Directly sets the camera's position to the specified coordinates, converting
     * world coordinates to screen coordinates.
     *
     * @param x The x-coordinate in the game world.
     * @param y The y-coordinate in the game world.
     * @param z The z-coordinate in the game world.
     */
    public static void setPositionTo(float x, float y, float z){
        Camera.pos.set(x * TILE_WIDTH, (-z + y) * TILE_HEIGHT);
    }

    /**
     * Sets the camera's position to the specified {@link Vector3f} position in world coordinates.
     *
     * @param pos The position in the game world as a {@link Vector3f}.
     */
    public static void setPositionTo(Vector3f pos){
        setPositionTo(pos.x, pos.y, pos.z);
    }

    /**
     * Sets the camera's position to the specified `x` and `z` world coordinates.
     * This overload ignores the y-coordinate.
     *
     * @param x The x-coordinate in the game world.
     * @param z The z-coordinate in the game world.
     */
    public static void setPositionTo(float x, float z){
        setPositionTo(x, 0, z);
    }

}
