package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;

public class CombatComponent extends Component {

    private float width = 1, height = 1;
    private float maxHealth = 10;
    private float maxMana;
    private float damage;
    private float armorFlat;
    private float damageReist;
    private float crit, critDmg = 2;
    private float knockBackResist;
    private int maxInvTime; // invulnerability time

    public CombatComponent() {
        super("combat");
    }

    @Override
    public String genName() {
        return "Combat";
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        width = dso.getFloat("w", 1);
        height = dso.getFloat("h", 1);
        maxHealth = dso.getFloat("m_hp", 10);
        maxMana = dso.getFloat("m_m", 0);
        damage = dso.getFloat("dmg", 1);
        armorFlat = dso.getFloat("armor", 0);
        damageReist = dso.getFloat("dmg_res", 0);
        crit = dso.getFloat("crit", 0);
        critDmg = dso.getFloat("crit_dmg", 2);
        knockBackResist = dso.getFloat("kb_res", 0);
        maxInvTime = dso.getInt("m_inv", 0);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putFloat("w", width);
        dso.putFloat("h", height);
        dso.putFloat("m_hp", maxHealth);
        dso.putFloat("m_m", maxMana);
        dso.putFloat("dmg", damage);
        dso.putFloat("armor", armorFlat);
        dso.putFloat("dmg_res", damageReist);
        dso.putFloat("crit", crit);
        dso.putFloat("crit_dmg", critDmg);
        dso.putFloat("kb_res", knockBackResist);
        dso.putInt("m_inv", maxInvTime);
        return dso;
    }

    @Override
    public Runnable createView(Window window, GameObject object, JPanel parent) {
        var labels = new ArrayList<JLabel>();
        labels.add(createNameValView(parent, "Width:", width, .2, 10, .2, value -> width = value.floatValue()));
        labels.add(createNameValView(parent, "Height:", height, .2, 10, .2, value -> height = value.floatValue()));
        labels.add(createNameValView(parent, "Max health:", maxHealth, 1, Integer.MAX_VALUE, 1, value -> maxHealth = value.floatValue()));
        labels.add(createNameValView(parent, "Max mana:", maxMana, 0, Integer.MAX_VALUE, 1, value -> maxMana = value.floatValue()));
        labels.add(createNameValView(parent, "Damage:", damage, 0, Integer.MAX_VALUE, 1, value -> damage = value.floatValue()));
        labels.add(createNameValView(parent, "Armor:", armorFlat, 0, Integer.MAX_VALUE, 1, value -> armorFlat = value.floatValue()));
        labels.add(createNameValView(parent, "Dmg. res.:", (int)(damageReist * 100), 0, 100, 1, value -> damageReist = value.intValue() / 100f));
        labels.add(createNameValView(parent, "Critical chance:", (int)(crit * 100), 0, 100, 1, value -> crit = value.intValue() / 100f));
        labels.add(createNameValView(parent, "Critical multiplier:", (int)(critDmg * 100), 0, Integer.MAX_VALUE, 1, value -> critDmg = value.intValue() / 100f));
        labels.add(createNameValView(parent, "Knock back resistance:", (int)(knockBackResist * 100), 0, 100, 1, value -> knockBackResist = value.intValue() / 100f));
        labels.add(createNameValView(parent, "Invulnerability time:", maxInvTime, 0, 100, 1, value -> maxInvTime = value.intValue()));

        var labelWidth = 0;
        for(var label: labels)
            if(label.getPreferredSize().width > labelWidth)
                labelWidth = label.getPreferredSize().width;
        for(var label: labels)
            label.setPreferredSize(new Dimension(labelWidth, label.getHeight()));

        return null;
    }

    private <T extends Number> JLabel createNameValView(JPanel parent, String name, T value, T min, T max, T step, Consumer<Number> onChange){
        var label = new JLabel(name);
        var _value = (Number) Math.max(min.doubleValue(), Math.min(max.doubleValue(), value.doubleValue()));
        var spinner = new JSpinner(new SpinnerNumberModel(_value, min.doubleValue(), max.doubleValue(), step));
        spinner.setPreferredSize(new Dimension(0, 25));
        spinner.addChangeListener(l -> onChange.accept((Number) spinner.getValue()));
        addView(parent, new JPanel(){
            {
                setLayout(new BorderLayout(5, 0));
                add(label, BorderLayout.WEST);
                add(spinner, BorderLayout.CENTER);
            }
        });
        return label;
    }

    public static final Color OBJECT_TOP_COLOR = new Color(.8f, .2f, 0f, .65f);
    public static final Color OBJECT_SIDE_COLOR = new Color(.6f, .15f, 0f, .65f);

    @Override
    public boolean onDraw(Window window, GameObject parent, Graphics2D g, float zoom, float offsetX, float offsetY, boolean selected) {
        var TW = TILE_WIDTH * zoom;
        var TH = TILE_HEIGHT * zoom;
        var x = parent.getX() * TW + offsetX;
        var y = (parent.getZ() - parent.getY()) * TH + offsetY;
        var w = this.width * TW;
        var h = this.height * TH;
        var d = this.width * TH;

        var rectX = (int)(x - w / 2);
        var rectY = (int)(y + (d / 2) - h);
        var rectW = (int)w;
        var rectH = (int)h;

        g.setColor(OBJECT_SIDE_COLOR);
        g.fillRect(rectX, rectY, rectW, rectH);
        rectY = (int)(y - (d / 2) - h);
        rectH = (int)d;
        g.setColor(OBJECT_TOP_COLOR);
        g.fillRect(rectX, rectY, rectW, rectH);
        return true;
    }

    @Override
    public int getRenderPriority() {
        return 4;
    }

    @Override
    public boolean intersects(float x, float y, GameObject parent) {
        var pX = parent.getX();
        var pY = parent.getY();
        var pZ = parent.getZ();
        return !(x < pX - width / 2) && !(x >= pX + this.width / 2) && !(y < pZ - pY - width / 2 - height) && !(y >= pZ - pY + width / 2);
    }

}
