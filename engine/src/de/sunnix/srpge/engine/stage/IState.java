package de.sunnix.srpge.engine.stage;

public interface IState {

    void onStart();

    void update();

    void render();

    void postUpdate();

    void onStop();

    void onDestroy();
}
