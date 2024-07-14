package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.srpge.engine.util.Tuple;
import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EventRegistry {

    private static Map<String, Tuple.Tuple2<String, Supplier<? extends IEvent>>> registeredEvents = new HashMap<>();

    public static void registerEvent(String id, String displayName, Supplier<? extends IEvent> eventSupplier){
        registeredEvents.put(id, new Tuple.Tuple2<>(displayName, eventSupplier));
    }

    public static List<Tuple.Tuple2<String, String>> getAll(){
        return registeredEvents.entrySet().stream().map(e -> new Tuple.Tuple2<>(e.getKey(), e.getValue().t1())).toList();
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
