package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.DebugRenderObject;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import de.sunnix.srpge.engine.graphics.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;

@Getter
@Setter
public class PhysicComponent extends Component {

    @Setter(AccessLevel.NONE)
    private static final Texture SHADOW_TEXTURE = new Texture("/data/texture/shadow.png");

    @Setter(AccessLevel.NONE)
    private AABB hitbox;
    private float weight = .8f, jumpSpeed = .26f, fallSpeed;
    private boolean falling;
    private boolean flying;
    private boolean platform = true;

    // debug
    @Setter(AccessLevel.NONE)
    private DebugRenderObject dro;

    private float groundPos;

    @Setter(AccessLevel.NONE)
    private RenderObject shadow;

    private boolean hasShadow = true;

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        hitbox = new AABB(go);
        PhysicSystem.add(go);
        if(Core.isDebug())
            dro = new DebugRenderObject(hitbox.getWidth(), hitbox.getHeight());
        if(hasShadow && go.getComponent(RenderComponent.class) != null){
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

    public void setFalling(boolean falling) {
        if(falling)
            parent.addState(States.FALLING.id());
        else
            parent.removeState(States.FALLING.id());
        this.falling = falling;
    }

    public void jump(){
        if(falling || flying)
            return;
        fallSpeed = jumpSpeed;
        setFalling(true);
    }

}
