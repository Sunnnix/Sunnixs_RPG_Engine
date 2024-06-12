package de.sunnix.srpge.engine.stage;

import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.util.BetterJSONObject;
import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameplayState implements IState {

    @Getter
    private World world;

    @Override
    public void onStart() {
        if(world == null)
            try(var zip = new ZipFile(Core.getGameFile())){
                var config = new BetterJSONObject(new String(zip.getInputStream(new ZipEntry("game.config")).readAllBytes()));
                var version = Arrays.stream(config.get("editor_version", "0.0").split("\\.")).mapToInt(Integer::parseInt).toArray();
                if(version[0] != Core.MAJOR_VERSION || version[1] != Core.MINOR_VERSION)
                    throw new IOException("The version of the GameFile is not equal to the version of the Engine!");
                Resources.get().loadResources(zip);
                world = new World();
                world.init(zip, config);
            } catch (Exception e){
                GameLogger.logException("World", e);
            }
    }

    @Override
    public void update() {
        world.update();
    }

    @Override
    public void render() {
        world.render();
    }

    @Override
    public void postUpdate() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        world.onDestroy();
    }
}
