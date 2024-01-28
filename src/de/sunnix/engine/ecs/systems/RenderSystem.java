package de.sunnix.engine.ecs.systems;

import de.sunnix.engine.Core;
import de.sunnix.engine.ecs.ComponentManager;
import de.sunnix.engine.ecs.components.RenderComponent;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class RenderSystem {

    public static void renderObjects() {
        var bgc = Core.getBackgroundColor();
        glClearColor(bgc.x, bgc.y, bgc.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        ComponentManager.getComponentList(RenderComponent.class).forEach(RenderComponent::render);
    }
}
