package de.sunnix.engine.stage;

import de.sunnix.engine.ecs.World;
import lombok.Getter;

import static de.sunnix.engine.InputManager.*;

public class GameplayState implements IState {

    @Getter
    private World world;

    @Override
    public void onStart() {
        if(world == null) {
            world = new World();
            world.init();
        }
    }

    @Override
    public void update() {
        var player = world.getPlayer();
        if(PAD_RIGHT.isPressed())
            player.getPosition().add(.5f, 0, 0);
        if(PAD_LEFT.isPressed())
            player.getPosition().add(-.5f, 0, 0);
        if(PAD_DOWN.isPressed())
            player.getPosition().add(0, .5f, 0);
        if(PAD_UP.isPressed())
            player.getPosition().add(0, -.5f, 0);
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
}
