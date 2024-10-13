package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.engine.util.Tuple;
import de.sunnix.sdso.DataSaveObject;

import java.util.*;
import java.util.function.Supplier;

public class EventRegistry {

    public static final String GROUP_MOVE = "Movement";
    public static final String GROUP_OBJECT = "Object";
    public static final String GROUP_SCRIPT = "Script";
    public static final String GROUP_SYSTEM = "System";

    private static Map<String, Tuple.Tuple2<String, Supplier<? extends IEvent>>> registeredEvents = new HashMap<>();
    private static Map<String, List<Tuple.Tuple2<String, String>>> eventGroups = new HashMap<>();

    public static void registerEvent(String group, String id, String displayName, Supplier<? extends IEvent> eventSupplier){
        registeredEvents.put(id, new Tuple.Tuple2<>(displayName, eventSupplier));
        eventGroups.computeIfAbsent(group, k -> new ArrayList<>()).add(new Tuple.Tuple2<>(id, displayName));
    }

    public static List<Tuple.Tuple2<String, String>> getAll(){
        return registeredEvents.entrySet().stream().map(e -> new Tuple.Tuple2<>(e.getKey(), e.getValue().t1())).toList();
    }

    public static List<String> getGroups(){
        return eventGroups.keySet().stream().toList();
    }

    public static List<Tuple.Tuple2<String, String>> getGroupEvents(String group){
        return eventGroups.getOrDefault(group, Collections.emptyList());
    }

    public static IEvent createEvent(String id){
        var eventSupplier = registeredEvents.get(id);
        if(eventSupplier != null)
            return eventSupplier.t2().get();
        return new NULLEvent();
    }

    public static IEvent loadEvent(String id, DataSaveObject dso){
        var eventSupplier = registeredEvents.get(id);
        IEvent event;
        if(eventSupplier == null)
            event = new NULLEvent();
        else
            event = eventSupplier.t2().get();
        event.load(dso);
        return event;
    }

}
