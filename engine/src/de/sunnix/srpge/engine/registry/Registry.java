package de.sunnix.srpge.engine.registry;

import de.sunnix.srpge.engine.ecs.components.Component;
import de.sunnix.srpge.engine.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

public class Registry {

    private static final List<IRegistry> registries = new ArrayList<>();

    public static final ReferenceRegistrar<Texture> TEXTURE = new ReferenceRegistrar<>(Texture.MISSING_IMAGE);
    public static final ReferenceRegistrar<Component> COMPONENT = new ReferenceRegistrar<>(null);

    public static void addRegistry(IRegistry registry){
        registries.add(registry);
    }

    public static void registerAll(){
        registries.forEach(IRegistry::register);
    }

}
