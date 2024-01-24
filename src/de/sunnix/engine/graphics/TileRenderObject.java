package de.sunnix.engine.graphics;

import de.sunnix.engine.Core;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class TileRenderObject extends RectangularRenderObject{

    private static final Vector2f SIZE = new Vector2f(Core.TILE_WIDTH, Core.TILE_HEIGHT);

    @Override
    public Vector2f getSize() {
        return SIZE;
    }
}
