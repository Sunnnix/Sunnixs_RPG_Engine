package de.sunnix.srpge.engine.registry;

import de.sunnix.srpge.engine.ecs.components.Component;

public class CoreRegistry implements IRegistry{

    @Override
    public void register() {
        Component.registerComponents();
    }

}
