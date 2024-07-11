package de.sunnix.srpge.editor.window.object;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.ecs.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class States extends de.sunnix.srpge.engine.ecs.States {

    private static final List<String> nonRemovable = new ArrayList<>();

    static {
        addState(MOVING.id(), MOVING.priority(), false);
        addState(FALLING.id(), FALLING.priority(), false);
        addState(HURT.id(), HURT.priority(), false);
    }

    private static State addState(String id, int priority, boolean removable){
        id = id.toLowerCase();
        if(nonRemovable.contains(id))
            throw new RuntimeException("The state " + id + " already exists and cannot be overwritten!");
        var state = new State(id, priority);
        states.put(id, state);
        if(!removable)
            nonRemovable.add(id);
        return state;
    }

    public static State addState(String id, int priority){
        return addState(id, priority, true);
    }

    public static Collection<State> getStates(){
        return states.values().stream().toList();
    }

    public static boolean isRemovable(String id){
        if(id == null)
            return false;
        id = id.toLowerCase();
        return !nonRemovable.contains(id);
    }

    public static DataSaveObject save(DataSaveObject dso){
        dso.putList("states", states.values().stream().map(s -> {
            var sDSO = new DataSaveObject();
            sDSO.putString("id", s.id());
            sDSO.putInt("prio", s.priority());
            return sDSO;
        }).toList());
        return dso;
    }

    public static State changeStateId(State state, String newID) {
        newID = newID.toLowerCase();
        states.remove(state.id());
        var newState = new State(newID, state.priority());
        states.put(newID, newState);
        return newState;
    }

    public static State changeStatePrio(State state, int newPrio) {
        var newState = new State(state.id(), newPrio);
        states.put(state.id(), newState);
        return newState;
    }

    public static boolean hasStateID(String id) {
        id = id.toLowerCase();
        return states.containsKey(id);
    }

    public static State removeState(String id) {
        id = id.toLowerCase();
        if(!isRemovable(id))
            throw new RuntimeException("State " + id + " can't be removed!");
        return states.remove(id);
    }
}
