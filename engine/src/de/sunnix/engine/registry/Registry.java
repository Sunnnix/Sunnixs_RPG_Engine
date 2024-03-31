package de.sunnix.engine.registry;

import de.sunnix.engine.ecs.components.Component;
import de.sunnix.engine.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

public class Registry {

    private static final List<IRegistry> registries = new ArrayList<>();
    static{
        registries.add(new CoreRegistry());
    }
    public static final ReferenceRegistrar<Texture> TEXTURE = new ReferenceRegistrar<>(Texture.MISSING_IMAGE);
    public static final ReferenceRegistrar<Component> COMPONENT = new ReferenceRegistrar<>(null);

    public static void addRegistry(IRegistry registry){
        registries.add(registry);
    }

    public static void registerAll(){
        registries.forEach(IRegistry::register);
    }

}
