package de.sunnix.srpge.engine.stage;

import de.sunnix.srpge.engine.Core;

public class IntroState implements IState {
    @Override
    public void onStart() {

    }

    @Override
    public void update() {
        Core.setNext_game_state(Core.GameState.MAIN_MENU);
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
