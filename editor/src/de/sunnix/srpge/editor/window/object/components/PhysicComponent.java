package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.Window;
import lombok.Getter;

import javax.swing.*;

import java.awt.*;

import static de.sunnix.srpge.editor.data.GameObject.*;
import static de.sunnix.srpge.editor.lang.Language.getString;
import static de.sunnix.srpge.editor.window.Window.TILE_HEIGHT;
import static de.sunnix.srpge.editor.window.Window.TILE_WIDTH;

public class PhysicComponent extends Component{

    @Getter
    private float width = 1, height = 1;
    private float weight = .85f, jumpSpeed = .25f;
    private boolean collision = true;
    private boolean flying = false;
    private boolean platform = false;
    private boolean canClimb;
    private boolean hasShadow = true;

    @Getter
    private float baseMoveSpeed = .035f;

    public PhysicComponent() {
        super("physic");
    }

    @Override
    public String genName() {
        return getString("component.physic");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        width = dso.getFloat("width", 1);
        height = dso.getFloat("height", 1);
        weight = dso.getFloat("weight", .85f);
        jumpSpeed = dso.getFloat("jump_speed", .25f);
        collision = dso.getBool("collision", true);
        flying = dso.getBool("flying", false);
        platform = dso.getBool("platform", false);
        canClimb = dso.getBool("can_climb", false);
        hasShadow = dso.getBool("has_shadow", true);
        baseMoveSpeed = dso.getFloat("base_ms", .035f);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putFloat("width", width);
        dso.putFloat("height", height);
        dso.putFloat("weight", weight);
        dso.putFloat("jump_speed", jumpSpeed);
        dso.putBool("collision", collision);
        dso.putBool("flying", flying);
        dso.putBool("platform", platform);
        dso.putBool("can_climb", canClimb);
        dso.putBool("has_shadow", hasShadow);
        dso.putFloat("base_ms", baseMoveSpeed);
        return dso;
    }

    @Override
    public Runnable createView(Window window, GameObject object, JPanel parent) {
        var width = new JSpinner(new SpinnerNumberModel(this.width, .2, 10, .2));
        width.addChangeListener(l -> this.width = ((Number)width.getValue()).floatValue());
        var height = new JSpinner(new SpinnerNumberModel(this.height, .2, 10, .2));
        height.addChangeListener(l -> this.height = ((Number)height.getValue()).floatValue());
        var weight = new JSpinner(new SpinnerNumberModel(this.weight, 0, 10, .05));
        weight.addChangeListener(l -> this.weight = ((Number)weight.getValue()).floatValue());
        var jumpSpeed = new JSpinner(new SpinnerNumberModel(this.jumpSpeed, 0, 10, .05));
        jumpSpeed.addChangeListener(l -> this.jumpSpeed = ((Number)jumpSpeed.getValue()).floatValue());
        var baseMoveSpeed = new JSpinner(new SpinnerNumberModel(this.baseMoveSpeed, 0.005, 1, .005));
        baseMoveSpeed.addChangeListener(l -> this.baseMoveSpeed = ((Number)baseMoveSpeed.getValue()).floatValue());
        var collision = new JCheckBox("Collision", this.collision);
        collision.addActionListener(l -> this.collision = collision.isSelected());
        var flying = new JCheckBox("Flying", this.flying);
        flying.addActionListener(l -> this.flying = flying.isSelected());
        var platform = new JCheckBox("Platform", this.platform);
        platform.addActionListener(l -> this.platform = platform.isSelected());
        var canClimb = new JCheckBox("Can climb", this.canClimb);
        canClimb.addActionListener(l -> this.canClimb = canClimb.isSelected());
        var hasShadow = new JCheckBox("Has shadow", this.hasShadow);
        hasShadow.addActionListener(l -> this.hasShadow = hasShadow.isSelected());

        var label3 = new JLabel("Width:");
        var label4 = new JLabel("Height:");
        var label1 = new JLabel("Weight:");
        var label2 = new JLabel("Jump speed:");
        var label5 = new JLabel("Base move speed:");
        label1.setPreferredSize(label5.getPreferredSize());
        label2.setPreferredSize(label5.getPreferredSize());
        label3.setPreferredSize(label5.getPreferredSize());
        label4.setPreferredSize(label5.getPreferredSize());

        addView(parent, new JPanel() {
            {
                setLayout(new BorderLayout(5,0));
                add(label3, BorderLayout.WEST);
                add(width, BorderLayout.CENTER);
            }
        });
        addView(parent, new JPanel() {
            {
                setLayout(new BorderLayout(5,0));
                add(label4, BorderLayout.WEST);
                add(height, BorderLayout.CENTER);
            }
        });
        addView(parent, new JPanel() {
            {
                setLayout(new BorderLayout(5,0));
                add(label1, BorderLayout.WEST);
                add(weight, BorderLayout.CENTER);
            }
        });
        addView(parent, new JPanel() {
            {
                setLayout(new BorderLayout(5,0));
                add(label2, BorderLayout.WEST);
                add(jumpSpeed, BorderLayout.CENTER);
            }
        });
        addView(parent, new JPanel(){
            {
                setLayout(new BorderLayout(5,0));
                add(label5, BorderLayout.WEST);
                add(baseMoveSpeed, BorderLayout.CENTER);
            }
        });
        addView(parent, collision);
        addView(parent, flying);
        addView(parent, platform);
        addView(parent, canClimb);
        addView(parent, hasShadow);

        return null;
    }

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

        g.setColor(selected ? (parent.isEnabled() ? OBJECT_SIDE_COLOR_S : OBJECT_SIDE_COLOR_DISABLED_S) : parent.getID() == 999 ? PLAYER_OBJECT_SIDE_COLOR : (parent.isEnabled() ? OBJECT_SIDE_COLOR : OBJECT_SIDE_COLOR_DISABLED));
        g.fillRect(rectX, rectY, rectW, rectH);
        rectY = (int)(y - (d / 2) - h);
        rectH = (int)d;
        g.setColor(selected ? (parent.isEnabled() ? OBJECT_TOP_COLOR_S : OBJECT_TOP_COLOR_DISABLED_S) : parent.getID() == 999 ? PLAYER_OBJECT_TOP_COLOR : (parent.isEnabled() ? OBJECT_TOP_COLOR : OBJECT_TOP_COLOR_DISABLED));
        g.fillRect(rectX, rectY, rectW, rectH);
        return false;
    }

    @Override
    public int getRenderPriority() {
        return 5;
    }

    @Override
    public boolean intersects(float x, float y, GameObject parent) {
        var pX = parent.getX();
        var pY = parent.getY();
        var pZ = parent.getZ();
        return !(x < pX - width / 2) && !(x >= pX + this.width / 2) && !(y < pZ - pY - width / 2 - height) && !(y >= pZ - pY + width / 2);
    }
}
