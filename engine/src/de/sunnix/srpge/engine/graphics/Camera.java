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

    private static Vector2f targetPosition = new Vector2f();

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
     * Calculates and adjusts the camera's position based on the current {@code targetPosition}.
     * <p>
     * If {@link #attachObject} is set to {@code true} and an {@link #attachedObject} is valid
     * (i.e., it is not null and passes the validity check), the camera will smoothly follow
     * this object's position. The target position is then set based on the object's tile-based
     * coordinates, using the constants {@code TILE_WIDTH} and {@code TILE_HEIGHT} for scaling.
     * </p>
     * <p>
     * If no object is attached or {@link #attachObject} is {@code false}, the camera moves
     * towards a manually set target position, which can be defined using the {@link #setPositionTo(float, float, float, boolean)}
     * method. This allows the camera to move to a fixed point without following any object.
     * </p>
     * The camera moves towards the {@code targetPosition} using a smooth interpolation determined
     * by a factor
     */
    public static void calculateCameraPosition(){
        if(attachObject && attachedObject != null && attachedObject.isValid()) {
            var tPos = attachedObject.getPosition();
            targetPosition.set(tPos.x * TILE_WIDTH, (-tPos.z + tPos.y) * TILE_HEIGHT);
        }
        var xDiff = targetPosition.x - pos.x;
        var yDiff = targetPosition.y - pos.y;

        float factor = 0.075f;

        pos.x += xDiff * factor;
        pos.y += yDiff * factor;
    }

    /**
     * Sets the camera's target position to the specified coordinates, converting
     * world coordinates to screen coordinates.<br>
     * The camera will slowly move to that position.
     *
     * @param x The x-coordinate in the game world.
     * @param y The y-coordinate in the game world.
     * @param z The z-coordinate in the game world.
     * @see #setPositionTo(float, float, float, boolean)
     */
    public static void setPositionTo(float x, float y, float z){
        setPositionTo(x, y, z, false);
    }

    /**
     * Sets a new static position for the camera.<br>
     * If instant is true the camera's position will directly set to the specified coordinates, converting
     * world coordinates to screen coordinates. Otherwise, the target position will be set and the camera moves towards it
     *
     * @param x The x-coordinate in the game world.
     * @param y The y-coordinate in the game world.
     * @param z The z-coordinate in the game world.
     * @param instant should the camera will be set instant to that position
     */
    public static void setPositionTo(float x, float y, float z, boolean instant){
        targetPosition.set(x * TILE_WIDTH, (-z + y) * TILE_HEIGHT);
        if(instant)
            pos.set(targetPosition.x, targetPosition.y);
    }

    /**
     * The {@link Vector3f} function of {@link #setPositionTo(float, float, float)}.
     *
     * @param pos The position in the game world as a {@link Vector3f}.
     * @see #setPositionTo(Vector3f, boolean)
     */
    public static void setPositionTo(Vector3f pos){
        setPositionTo(pos.x, pos.y, pos.z);
    }

    /**
     * The {@link Vector3f} function of {@link #setPositionTo(float, float, float, boolean)}.
     *
     * @param pos The position in the game world as a {@link Vector3f}.
     * @param instant should the camera will be set instant to that position
     */
    public static void setPositionTo(Vector3f pos, boolean instant){
        setPositionTo(pos.x, pos.y, pos.z, instant);
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

    /**
     * Sets the attached object and may set the camera position instant to the objects position.
     * @param go the object to be attached
     * @param instant if the position would be set instant via {@link #setPositionTo(Vector3f, boolean)}
     */
    public static void setAttachedObject(GameObject go, boolean instant){
        attachedObject = go;
        if(go != null && instant)
            setPositionTo(go.getPosition(), true);
    }

}
