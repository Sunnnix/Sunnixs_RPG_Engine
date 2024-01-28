package de.sunnix.engine.ecs.systems;

import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.components.RenderComponent;

public class RenderSystem {

    public static void renderObjects() {
        ComponentManager.getComponentList(RenderComponent.class).forEach(RenderComponent::render);
    }
}
