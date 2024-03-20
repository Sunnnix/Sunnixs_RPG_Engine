package de.sunnix.game.textures;

import de.sunnix.engine.graphics.Texture;
import de.sunnix.engine.registry.Registry;

public class Textures {

    public static final String TEST = "test";

    public static void registerTextures() {
        var registrar = Registry.TEXTURE;
        registrar.setDefaultValue(Texture.MISSING_IMAGE);
        registrar.register(TEST, new Texture("/assets/textures/test.png"));
    }

}
