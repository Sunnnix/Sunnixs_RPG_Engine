package de.sunnix.engine.registry;

import de.sunnix.engine.ecs.components.Component;

public class CoreRegistry implements IRegistry{

    @Override
    public void register() {
        Component.registerComponents();
    }

}
