package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.sdso.DataSaveObject;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class EventRegistry {

    private static final Map<String, Supplier<Event>> events = new HashMap<>();

    public static void registerEvent(String id, Supplier<Event> supplier) {
        events.put(id, supplier);
    }

    public static Event createEvent(String id, DataSaveObject dso) {
        Supplier<Event> supplier = events.get(id);
        Event event;
        if (supplier != null)
            event = supplier.get();
        else {
            GameLogger.logE("EventRegistry", "Unknown event %s", id);
            event = new NULLEvent();
        }
        event.load(dso);
        return event;
    }

}
