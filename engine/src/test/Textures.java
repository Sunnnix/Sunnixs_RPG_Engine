package test;

import de.sunnix.aje.engine.graphics.Texture;
import de.sunnix.aje.engine.graphics.TextureAtlas;
import de.sunnix.aje.engine.registry.Registry;

public class Textures {

    public static final String TEST = "test";
    public static final TextureAtlas TILESET_INOA = new TextureAtlas("/assets/textures/tileset/inoa.png", 30, 48);
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
