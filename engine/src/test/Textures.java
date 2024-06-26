package test;

import de.sunnix.srpge.engine.graphics.Texture;
import de.sunnix.srpge.engine.graphics.TextureAtlas;
import de.sunnix.srpge.engine.registry.Registry;

public class Textures {

    public static final String TEST = "test";
    public static final String ALUNDRA_WALKING = "entity/alundra_idle";
    public static final String BOX = "entity/box";

    public static void registerTextures() {
        var registrar = Registry.TEXTURE;
        registrar.setDefaultValue(Texture.MISSING_IMAGE);
        registrar.register(TEST, new Texture("/assets/textures/test.png"));
        registrar.register(ALUNDRA_WALKING, new TextureAtlas("/assets/textures/entity/player/alundra_idle.png", 6, 4));
        registrar.register(BOX, new Texture("/assets/textures/entity/misc/box.png"));
    }

}
