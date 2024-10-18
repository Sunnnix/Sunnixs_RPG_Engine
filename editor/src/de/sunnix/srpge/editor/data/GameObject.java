package de.sunnix.srpge.editor.data;

import de.sunnix.srpge.editor.window.object.components.Component;
import de.sunnix.srpge.editor.window.object.components.ComponentRegistry;
import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.window.Window;
import de.sunnix.srpge.editor.window.object.events.EventList;
import de.sunnix.srpge.engine.ecs.Direction;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Comparator;
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
    private Direction facing = Direction.SOUTH;
    private boolean enabled = true;

    @Getter
    private final List<EventList> eventLists = new ArrayList<>();
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

    public GameObject(DataSaveObject dso, int[] version){
        this.ID = load(dso, version);
    }

    public static final Color OBJECT_TOP_COLOR = new Color(.2f, .6f, .8f, .65f);
    public static final Color OBJECT_TOP_COLOR_S = new Color(.8f, .8f, 0f, .65f);
    public static final Color OBJECT_SIDE_COLOR = new Color(.0f, .5f, 1f, .65f);
    public static final Color OBJECT_SIDE_COLOR_S = new Color(.6f, .6f, 0f, .65f);
    public static final Color OBJECT_TOP_COLOR_DISABLED = new Color(.8f, .8f, .8f, .65f);
    public static final Color OBJECT_TOP_COLOR_DISABLED_S = new Color(1f, 1f, 1f, .65f);
    public static final Color OBJECT_SIDE_COLOR_DISABLED = new Color(.5f, .5f, .5f, .65f);
    public static final Color OBJECT_SIDE_COLOR_DISABLED_S = new Color(.75f, .75f, .75f, .65f);
    public static final Color PLAYER_OBJECT_TOP_COLOR = new Color(.6f, 1f, 1f, .65f);
    public static final Color PLAYER_OBJECT_SIDE_COLOR = new Color(.4f, .75f, .75f, .65f);

    public void draw(Window window, Graphics2D g, float zoom, float offsetX, float offsetY, boolean selected){
        var drawDefaultHitbox = true;
        for(var comp: components.stream().sorted(Comparator.comparing(Component::getRenderPriority)).toList())
            if(!comp.onDraw(window, this, g, zoom, offsetX, offsetY, selected))
                drawDefaultHitbox = false;
        if(drawDefaultHitbox)
            drawHitbox(g, zoom, offsetX, offsetY, selected);
    }

    private void drawHitbox(Graphics2D g, float zoom, float offsetX, float offsetY, boolean selected){
        var TW = TILE_WIDTH * zoom;
        var TH = TILE_HEIGHT * zoom;
        var x = this.x * TW + offsetX;
        var y = (this.z - this.y) * TH + offsetY;
        var w = this.width * TW;
        var h = this.height * TH;
        var d = this.width * TH;

        var g2d = (Graphics2D) g.create();

        var rectX = (int)(x - w / 2);
        var rectY = (int)(y + (d / 2) - h);
        var rectW = (int)w;
        var rectH = (int)h;

        int stripeWidth = 5;

        g2d.setColor(selected ? (enabled ? OBJECT_SIDE_COLOR_S : OBJECT_SIDE_COLOR_DISABLED_S) : getID() == 999 ? PLAYER_OBJECT_SIDE_COLOR : (enabled ? OBJECT_SIDE_COLOR : OBJECT_SIDE_COLOR_DISABLED));
        g2d.setClip(rectX, rectY, rectW + 1, rectH + 1);
        g2d.drawRect(rectX, rectY, rectW, rectH);
        for (int i = rectX - rectH; i < rectX + rectW; i += stripeWidth)
            g2d.drawLine(i, rectY, i + rectH, rectY + rectH);
        rectY = (int)(y - (d / 2) - h);
        rectH = (int)d;
        stripeWidth = 3;
        g2d.setColor(selected ? (enabled ? OBJECT_TOP_COLOR_S : OBJECT_TOP_COLOR_DISABLED_S) : getID() == 999 ? PLAYER_OBJECT_TOP_COLOR : (enabled ? OBJECT_TOP_COLOR : OBJECT_TOP_COLOR_DISABLED));
        g2d.setClip(rectX, rectY, rectW + 1, rectH + 1);
        g2d.drawRect(rectX, rectY, rectW, rectH);
        for (int i = rectX - rectH; i < rectX + rectW; i += stripeWidth)
            g2d.drawLine(i, rectY, i + rectH, rectY + rectH);
        g2d.dispose();
    }

    public boolean intersects(float x, float y) {
        return components.stream().anyMatch(comp -> comp.intersects(x, y, this)) || !(x < this.x - width / 2) && !(x >= this.x + this.width / 2) && !(y < this.z - this.y - width / 2 - height) && !(y >= this.z - this.y + width / 2);
    }

    public boolean hasComponent(String id) {
        return components.stream().anyMatch(comp -> comp.ID.equals(id));
    }

    public boolean hasComponent(Class<? extends Component> clazz){
        return components.stream().anyMatch(comp -> comp.getClass().equals(clazz));
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> clazz) {
        T comp = null;
        for(var component : components)
            if(component.getClass().equals(clazz)){
                comp = (T) component;
                break;
            }
        return comp;
    }

    public int load(DataSaveObject dso, int[] version){
        this.name = dso.getString("name", null);
        this.x = dso.getFloat("x", 0);
        this.y = dso.getFloat("y", 0);
        this.z = dso.getFloat("z", 0);
        this.width = dso.getFloat("width", 0);
        this.height = dso.getFloat("height", 0);
        this.facing = Direction.values()[dso.getByte("facing", (byte) Direction.SOUTH.ordinal())];
        this.enabled = dso.getBool("enabled", true);

        if(version[1] < 7) {
            var eventDSO = dso.getObject("events");
            eventLists.add(new EventList(eventDSO == null ? new DataSaveObject() : new DataSaveObject().putList("events", eventDSO.getList("list"))));
        } else if(version[1] < 8)
            eventLists.addAll(dso.<DataSaveObject>getList("event_lists").stream().map(elDSO -> {
                // change old object id's to new object select system
                elDSO.<DataSaveObject>getList("events").forEach(data -> {
                    var objID = data.getInt("object", Integer.MIN_VALUE);
                    if(objID != Integer.MIN_VALUE)
                        data.putObject("obj", new DataSaveObject().putInt("o", objID));
                    objID = data.getInt("look_at_obj", Integer.MIN_VALUE);
                    if(objID != Integer.MIN_VALUE)
                        data.putObject("look_at_obj", new DataSaveObject().putInt("o", objID));
                    objID = data.getInt("other", Integer.MIN_VALUE);
                    if(objID != Integer.MIN_VALUE)
                        data.putObject("other", new DataSaveObject().putInt("o", objID));
                });
                return new EventList(elDSO);
            }).toList());
        else
            eventLists.addAll(dso.<DataSaveObject>getList("event_lists").stream().map(EventList::new).toList());
        components.clear();
        components.addAll(dso.<DataSaveObject>getList("components").stream().map(x ->
            ComponentRegistry.loadComponent(x.getString("id", null), x)
        ).toList());

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
        dso.putByte("facing", (byte) facing.ordinal());
        dso.putBool("enabled", enabled);

        dso.putList("event_lists", eventLists.stream().map(list -> list.save(new DataSaveObject())).toList());

        dso.putList("components", components.stream().map(x -> {
            var xDSO = new DataSaveObject();
            xDSO.putString("id", x.ID);
            return x.save(xDSO);
        }).toList());

        return dso;
    }

    @Override
    public String toString() {
        return String.format("%03d: %s", getID(), getName() == null ? "Unnamed" : getName());
    }

}
