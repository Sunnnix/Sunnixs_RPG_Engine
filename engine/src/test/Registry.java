package test;

import de.sunnix.aje.engine.registry.IRegistry;

public class Registry implements IRegistry {

    public void register(){
        Components.registerComponents();
        Textures.registerTextures();
    }

}
