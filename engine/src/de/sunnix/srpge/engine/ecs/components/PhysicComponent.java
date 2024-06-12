package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.systems.PhysicSystem;

public class PhysicComponent extends Component {

    @Override
    public void init(GameObject go) {
        super.init(go);
        PhysicSystem.add(go);
    }
}
