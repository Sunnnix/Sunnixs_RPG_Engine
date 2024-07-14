package de.sunnix.srpge.editor.window.object.events;

import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class EventList {

    @Getter
    private List<IEvent> events = new ArrayList<>();

    public void load(DataSaveObject dso) {
        events.addAll(dso.<DataSaveObject>getList("list").stream().map(eDSO -> EventRegistry.loadEvent(eDSO.getString("ID", null), eDSO)).toList());
    }

    public DataSaveObject save(DataSaveObject dso) {
        dso.putList("list", events.stream().map(e -> {
            var eDSO = e.save(new DataSaveObject());
            eDSO.putString("ID", e.getID());
            return eDSO;
        }).toList());
        return dso;
    }

    public List<IEvent> getEventsCopy(){
        return events.stream().map(x -> (IEvent) x.clone()).toList();
    }

    public void putEvents(List<IEvent> events) {
        this.events.clear();
        this.events.addAll(events);
    }
}
