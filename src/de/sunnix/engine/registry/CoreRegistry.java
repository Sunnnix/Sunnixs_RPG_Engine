package de.sunnix.engine.registry;

import de.sunnix.engine.ecs.components.RenderComponent;

public class CoreRegistry implements IRegistry{

    @Override
    public void register() {
        registerComponents();
    }

    private void registerComponents(){
        var registrar = Registry.COMPONENT;
        registrar.register("render", RenderComponent.class, RenderComponent::new);
    }

}
