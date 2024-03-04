package de.sunnix.game.registry;

import de.sunnix.engine.registry.IRegistry;
import de.sunnix.game.components.Components;
import de.sunnix.game.textures.Textures;

public class Registry implements IRegistry {

    public void register(){
        Components.registerComponents();
        Textures.registerTextures();
    }

}
