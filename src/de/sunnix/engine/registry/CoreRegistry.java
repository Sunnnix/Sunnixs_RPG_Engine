package de.sunnix.engine.registry;

import de.sunnix.engine.ecs.components.Component;
import de.sunnix.engine.ecs.components.RenderComponent;

public class CoreRegistry implements IRegistry{

    @Override
    public void register() {
        Component.registerComponents();
    }

}
