package de.sunnix.aje.editor.window.object;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public abstract class AbstractEvent<T extends AbstractEvent> {

    private static Map<String, Class<? extends AbstractEvent>> registeredEvents = new HashMap<>();

    public static void registerEvent(Class<? extends AbstractEvent> event){
        var annotation = (Event) Arrays.stream(event.getAnnotations()).filter(a -> a.equals(Event.class)).findFirst().orElse(null);
        if (annotation == null)
            throw new RuntimeException(String.format("The event %s is missing the Event annotation!", event.getName()));
        registeredEvents.put(annotation.id(), event);
    }

    @interface Event {
        String id();
        String text();
    }

}
