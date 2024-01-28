package de.sunnix.engine.stage;

import de.sunnix.engine.ecs.World;
import lombok.Getter;

public class GameplayState implements IState {

    @Getter
    private World world = new World();

    @Override
    public void onStart() {

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
}
