package de.sunnix.engine.stage;

public interface IState {

    void onStart();

    void update();

    void render();

    void postUpdate();

    void onStop();

}
