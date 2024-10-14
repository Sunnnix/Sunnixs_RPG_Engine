package de.sunnix.srpge.engine.ecs;

import de.sunnix.sdso.DataSaveObject;

import java.util.*;

public class States {

    private static final State NULLState = new State(null, 0);

    public static final State MOVING = new State("moving", 1);
    public static final State JUMPING = new State("jumping", 3);
    public static final State FALLING = new State("falling", 2);
    public static final State HURT = new State("hurt", 5);
    public static final State CLIMB = new State("climb", 1);
    public static final State CLIMBING_UP = new State("climbing_up", 2);
    public static final State CLIMBING_DOWN = new State("climbing_down", 2);
    public static final State DEAD = new State("dead", 99);

    protected static final Map<String, State> states = new HashMap<>();

    public static void load(DataSaveObject dso){
        states.clear();
        dso.<DataSaveObject>getList("states").forEach(x -> {
            var state = new State(x.getString("id", null), x.getInt("prio", 0));
            states.put(state.id(), state);
        });
    }

    public static State getState(String id){
        return states.getOrDefault(id, NULLState);
    }

}
