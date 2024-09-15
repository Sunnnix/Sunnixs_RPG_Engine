package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.editor.data.GameObject;
import de.sunnix.srpge.editor.window.Window;

import javax.swing.*;

import java.awt.*;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class PhysicComponent extends Component{

    private float weight = .8f, jumpSpeed = .26f;
    private boolean collision = true;
    private boolean flying = false;
    private boolean platform = false;
    private boolean canClimb;
    private boolean hasShadow = true;

    public PhysicComponent() {
        super("physic");
    }

    @Override
    public String genName() {
        return getString("component.physic");
    }

    @Override
    public DataSaveObject load(DataSaveObject dso) {
        weight = dso.getFloat("weight", .8f);
        jumpSpeed = dso.getFloat("jump_speed", .26f);
        flying = dso.getBool("collision", true);
        flying = dso.getBool("flying", false);
        platform = dso.getBool("platform", false);
        canClimb = dso.getBool("can_climb", false);
        hasShadow = dso.getBool("has_shadow", true);
        return dso;
    }

    @Override
    public DataSaveObject save(DataSaveObject dso) {
        dso.putFloat("weight", weight);
        dso.putFloat("jump_speed", jumpSpeed);
        dso.putBool("collision", collision);
        dso.putBool("flying", flying);
        dso.putBool("platform", platform);
        dso.putBool("can_climb", canClimb);
        dso.putBool("has_shadow", hasShadow);
        return dso;
    }

    @Override
    public Runnable createView(Window window, GameObject object, JPanel parent) {
        var weight = new JSpinner(new SpinnerNumberModel(this.weight, 0, 10, .05));
        weight.addChangeListener(l -> this.weight = ((Number)weight.getValue()).floatValue());
        var jumpSpeed = new JSpinner(new SpinnerNumberModel(this.jumpSpeed, 0, 10, .05));
        jumpSpeed.addChangeListener(l -> this.jumpSpeed = ((Number)jumpSpeed.getValue()).floatValue());
        var collision = new JCheckBox("Collision", this.collision);
        collision.addActionListener(l -> this.collision = collision.isSelected());
        var flying = new JCheckBox("Flying", this.flying);
        flying.addActionListener(l -> this.flying = flying.isSelected());
        var platform = new JCheckBox("Platform", this.platform);
        platform.addActionListener(l -> this.platform = platform.isSelected());
        var hasShadow = new JCheckBox("Has shadow", this.hasShadow);
        hasShadow.addActionListener(l -> this.hasShadow = hasShadow.isSelected());

        var label1 = new JLabel("Weight:");
        var label2 = new JLabel("Jump speed:");
        label1.setPreferredSize(label2.getPreferredSize());

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
        addView(parent, collision);
        addView(parent, flying);
        addView(parent, platform);
        addView(parent, hasShadow);

        return null;
    }
}
