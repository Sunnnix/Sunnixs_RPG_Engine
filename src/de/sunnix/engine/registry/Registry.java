package de.sunnix.engine.registry;

import de.sunnix.engine.ecs.components.Component;
import de.sunnix.engine.ecs.components.RenderComponent;
import de.sunnix.engine.graphics.Texture;
import de.sunnix.engine.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Registry {

    private static List<IRegistry> registries = new ArrayList<>();
    static{
        registries.add(new CoreRegistry());
    }
    public static final ReferenceRegistrar<Texture> TEXTURE = new ReferenceRegistrar<>(Texture.MISSING_IMAGE);
    public static final GeneratorRegistrar<Component> COMPONENT = new GeneratorRegistrar<>(null);

    public static void addRegistry(IRegistry registry){
        registries.add(registry);
    }

    public static void registerAll(){
        registries.forEach(IRegistry::register);
    }

}
