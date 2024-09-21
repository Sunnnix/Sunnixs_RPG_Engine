package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;
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

    public static final byte RUN_TYPE_PLAYER_CONSULT = 100;
    public static final byte RUN_TYPE_TOUCH = 101;
    public static final byte RUN_TYPE_TOUCH_TOP = 102;
    public static final byte RUN_TYPE_TOUCH_BOTTOM = 103;
    public static final byte RUN_TYPE_TOUCH_SOUTH = 104;
    public static final byte RUN_TYPE_TOUCH_EAST = 105;
    public static final byte RUN_TYPE_TOUCH_WEST = 106;
    public static final byte RUN_TYPE_TOUCH_NORTH = 107;

    @Setter(AccessLevel.NONE)
    private AABB hitbox;
    private float width, height;
    private float weight, jumpSpeed, fallSpeed;
    private boolean collision;
    private boolean falling;
    private boolean flying;
    private boolean platform;
    private boolean canClimb;

    private int fallingTime;
    private boolean jumped;
    private int noobHelpTime = 12;

    // debug
    @Setter(AccessLevel.NONE)
    private DebugRenderObject dro;

    private float groundPos;

    @Setter(AccessLevel.NONE)
    private RenderObject shadow;

    private boolean hasShadow;

    public PhysicComponent(DataSaveObject dso) {
        width = dso.getFloat("width", 1);
        height = dso.getFloat("height", 1);
        weight = dso.getFloat("weight", .85f);
        jumpSpeed = dso.getFloat("jump_speed", .25f);
        collision = dso.getBool("collision", true);
        flying = dso.getBool("flying", false);
        platform = dso.getBool("platform", false);
        canClimb = dso.getBool("can_climb", false);
        hasShadow = dso.getBool("has_shadow", true);
    }

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        parent.size.set(width, height);
        hitbox = new AABB(go);
        PhysicSystem.add(go);
        go.addPositionSubscriber(PhysicSystem::relocateGridObject);
        go.addMarkDirtySubscriber(obj -> {
            reloadHitbox();
            PhysicSystem.markDirty(obj);
        });
        PhysicSystem.relocateGridObject(go.getPosition(), go.getPosition(), go);
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
        var pPos = parent.getPosition();
        hitbox = new AABB(pPos.x, pPos.y, pPos.z, width, height);
    }

    public void setFalling(boolean falling) {
        if(falling)
            parent.addState(States.FALLING.id());
        else
            parent.removeState(States.FALLING.id());
        if(falling && this.falling)
            fallingTime++;
        else if(!falling) {
            fallingTime = 0;
            fallSpeed = 0;
            jumped = false;
        }
        this.falling = falling;
    }

    public void jump(){
        if(jumped || fallingTime > noobHelpTime && (falling || flying))
            return;
        jumped = true;
        fallSpeed = jumpSpeed;
        setFalling(true);
    }

    @Override
    protected void free() {
        super.free();
        if(dro != null)
            dro.freeMemory();
        if(shadow != null)
            shadow.freeMemory();
    }
}
