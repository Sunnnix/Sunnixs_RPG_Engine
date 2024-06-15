package de.sunnix.srpge.editor.data;

import de.sunnix.srpge.editor.window.object.components.Component;
import de.sunnix.srpge.editor.window.object.events.EventList;
import de.sunnix.sdso.DataSaveObject;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;

@Getter
@Setter
public class GameObject {

    public final int ID;

    private String name;
    private float x, y, z;
    private float width, height;

    @Getter
    private EventList eventList = new EventList();
    @Getter
    private List<Component> components = new ArrayList<>();

    public GameObject(int id, float x, float y, float z){
        this.ID = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = .75f;
        this.height = .8f;
        this.x -= width / 2;
        this.z -= width / 2;
    }

    public GameObject(MapData map, DataSaveObject dso){
        this.ID = load(map, dso);
    }

    private static final Color OBJECT_TOP_COLOR = new Color(.2f, .6f, .8f, .65f);
    private static final Color OBJECT_TOP_COLOR_S = new Color(.8f, .8f, 0f, .65f);
    private static final Color OBJECT_SIDE_COLOR = new Color(.0f, .5f, 1f, .65f);
    private static final Color OBJECT_SIDE_COLOR_S = new Color(.6f, .6f, 0f, .65f);

    public void draw(Graphics2D g, float zoom, int offsetX, int offsetY, boolean selected){
        drawHitbox(g, zoom, offsetX, offsetY, selected);
    }

    private void drawHitbox(Graphics2D g, float zoom, int offsetX, int offsetY, boolean selected){
        var TW = (int)(TILE_WIDTH * zoom);
        var TH = (int)(TILE_HEIGHT * zoom);
        var x = (int)(this.x * TW) + offsetX;
        var y = (int)(this.z * TH) + offsetY;
        var w = (int)(this.width * TW);
        var h = (int)(this.height * TH);
        var d = (int)(this.width * TH);
        g.setColor(selected ? OBJECT_SIDE_COLOR_S : OBJECT_SIDE_COLOR);
        g.fillRect(x, y, w, h);
        y -= d;
        g.setColor(selected ? OBJECT_TOP_COLOR_S : OBJECT_TOP_COLOR);
        g.fillRect(x, y, w, d);
    }

    public boolean intersects(float x, float y) {
        return !(x < this.x) && !(x >= this.x + this.width) && !(y < this.z - height) && !(y >= this.z + width);
    }

    public boolean hasComponent(String id) {
        return components.stream().anyMatch(comp -> comp.ID.equals(id));
    }

    public int load(MapData map, DataSaveObject dso){
        this.name = dso.getString("name", null);
        this.x = dso.getFloat("x", 0);
        this.y = dso.getFloat("y", 0);
        this.z = dso.getFloat("z", 0);
        this.width = dso.getFloat("width", 0);
        this.height = dso.getFloat("height", 0);

        var eventDSO = dso.getObject("events");
        eventList.load(eventDSO == null ? new DataSaveObject() : eventDSO);

        return dso.getInt("ID", -1);
    }

    public DataSaveObject save(DataSaveObject dso){
        dso.putInt("ID", ID);
        dso.putString("name", name);
        dso.putFloat("x", x);
        dso.putFloat("y", y);
        dso.putFloat("z", z);
        dso.putFloat("width", width);
        dso.putFloat("height", height);

        dso.putObject("events", eventList.save(new DataSaveObject()));

        return dso;
    }

    @Override
    public String toString() {
        return String.format("GameObject(ID: %s, %s)", getID(), getName() == null ? "" : getName());
    }

}
