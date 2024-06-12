package de.sunnix.srpge.engine.stage;

import de.sunnix.srpge.engine.Core;

public class MainMenuState implements IState {
    @Override
    public void onStart() {

    }

    @Override
    public void update() {
        Core.setNext_game_state(Core.GameState.GAMEPLAY);
    }

    @Override
    public void render() {

    }

    @Override
    public void postUpdate() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }
}
