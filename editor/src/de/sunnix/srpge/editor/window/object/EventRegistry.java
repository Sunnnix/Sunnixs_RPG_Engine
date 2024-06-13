package de.sunnix.srpge.editor.window.object;

import de.sunnix.srpge.editor.window.object.events.Event;
import de.sunnix.srpge.editor.window.object.events.NULLEvent;
import de.sunnix.srpge.engine.util.Tuple;
import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EventRegistry {

    private static Map<String, Tuple.Tuple2<String, Supplier<? extends Event>>> registeredEvents = new HashMap<>();

    public static void registerEvent(String id, String displayName, Supplier<? extends Event> eventSupplier){
        registeredEvents.put(id, new Tuple.Tuple2<>(displayName, eventSupplier));
    }

    public static List<Tuple.Tuple2<String, String>> getAll(){
        return registeredEvents.entrySet().stream().map(e -> new Tuple.Tuple2<>(e.getKey(), e.getValue().t1())).toList();
    }

    public static Event createEvent(String id){
        var eventSupplier = registeredEvents.get(id);
        if(eventSupplier != null)
            return eventSupplier.t2().get();
        return new NULLEvent();
    }

    public static Event loadEvent(String id, DataSaveObject dso){
        var eventSupplier = registeredEvents.get(id);
        Event event;
        if(eventSupplier == null)
            event = new NULLEvent();
        else
            event = eventSupplier.t2().get();
        event._load(dso);
        return event;
    }

}
