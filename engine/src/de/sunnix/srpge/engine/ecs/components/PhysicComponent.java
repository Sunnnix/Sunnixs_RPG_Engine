package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.PhysicSystem;

public class PhysicComponent extends Component {

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        PhysicSystem.add(go);
    }
}
