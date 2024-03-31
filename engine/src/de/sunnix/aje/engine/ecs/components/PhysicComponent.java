package de.sunnix.aje.engine.ecs.components;

import de.sunnix.aje.engine.ecs.GameObject;
import de.sunnix.aje.engine.ecs.systems.PhysicSystem;

public class PhysicComponent extends Component {

    @Override
    public void init(GameObject go) {
        super.init(go);
        PhysicSystem.add(go);
    }
}
