package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.DebugRenderObject;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.graphics.*;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;

@Getter
public class PhysicComponent extends Component {

    private static final Texture SHADOW_TEXTURE = new Texture("/data/texture/shadow.png");

    private AABB hitbox;

    private DebugRenderObject dro;

    @Setter
    private float groundPos;

    @Getter
    private RenderObject shadow;

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        hitbox = new AABB(go);
        PhysicSystem.add(go);
        dro = new DebugRenderObject(hitbox.getWidth(), hitbox.getHeight());
        if(go.getComponent(RenderComponent.class) != null){
            shadow = new TextureRenderObject(SHADOW_TEXTURE){
                @Override
                public Vector2f getSize() {
                    var distance = go.getPosition().y - groundPos;
                    var size = go.size.x;
                    if(distance > 3)
                        size = Math.max(0, size - (distance - 3) * .15f);
                    return new Vector2f(size * Core.TILE_WIDTH, size * Core.TILE_HEIGHT);
                }
            };
        }
    }

    public void reloadHitbox() {
        hitbox = new AABB(parent);
    }
}
