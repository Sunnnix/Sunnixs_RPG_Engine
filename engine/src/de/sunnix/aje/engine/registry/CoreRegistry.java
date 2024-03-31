package de.sunnix.aje.engine.registry;

import de.sunnix.aje.engine.ecs.components.Component;

public class CoreRegistry implements IRegistry{

    @Override
    public void register() {
        Component.registerComponents();
    }

}
