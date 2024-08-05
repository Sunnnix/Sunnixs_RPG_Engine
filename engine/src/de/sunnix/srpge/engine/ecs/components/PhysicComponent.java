package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.DebugRenderObject;
import de.sunnix.srpge.engine.ecs.systems.physics.PhysicSystem;
import lombok.Getter;

@Getter
public class PhysicComponent extends Component {

    private AABB hitbox;

    private DebugRenderObject dro;

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        hitbox = new AABB(go);
        PhysicSystem.add(go);
        dro = new DebugRenderObject(hitbox.getWidth(), hitbox.getHeight());
    }

    public void reloadHitbox() {
        hitbox = new AABB(parent);
    }
}
