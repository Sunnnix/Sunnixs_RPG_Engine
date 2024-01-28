package de.sunnix.engine.stage;

import de.sunnix.engine.Core;

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
}
