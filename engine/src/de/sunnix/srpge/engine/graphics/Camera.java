package de.sunnix.srpge.engine.graphics;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {

    @Getter
    private static Matrix4f projection = new Matrix4f();
    @Getter
    private static Matrix4f view = new Matrix4f();
    @Getter
    private static Vector2f size = new Vector2f();
    @Getter
    private static Vector2f pos = new Vector2f();

    public static void process(){
        view = new Matrix4f().translate(-pos.x + size.x / 2, -pos.y + size.y / 2, -1999 + 20);
//        projection = new Matrix4f().ortho(0, size.x, 0, size.y, 0, 1e10f);
        projection = new Matrix4f().ortho(0, size.x, 0, size.y, -20, 2000);
    }

}
