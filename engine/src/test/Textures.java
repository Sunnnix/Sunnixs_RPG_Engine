package test;

import de.sunnix.srpge.engine.graphics.Texture;
import de.sunnix.srpge.engine.registry.Registry;

public class Textures {

    public static void registerTextures() {
        var registrar = Registry.TEXTURE;
        registrar.setDefaultValue(Texture.MISSING_IMAGE);
    }

}
