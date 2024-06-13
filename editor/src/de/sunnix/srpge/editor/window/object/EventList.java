package de.sunnix.srpge.editor.window.object;

import de.sunnix.srpge.editor.window.object.events.Event;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class EventList {

    @Getter
    private List<Event> events = new ArrayList<>();

    public void load(DataSaveObject dso) {
        events.addAll(dso.<DataSaveObject>getList("list").stream().map(eDSO -> EventRegistry.loadEvent(eDSO.getString("ID", null), eDSO)).toList());
    }

    public DataSaveObject save(DataSaveObject dso) {
        dso.putList("list", events.stream().map(e -> {
            var eDSO = e._save(new DataSaveObject());
            eDSO.putString("ID", e.ID);
            return eDSO;
        }).toList());
        return dso;
    }

    public List<Event> getEventsCopy(){
        return events.stream().map(Event::clone).toList();
    }

    public void putEvents(List<Event> events) {
        this.events.clear();
        this.events.addAll(events);
    }
}
