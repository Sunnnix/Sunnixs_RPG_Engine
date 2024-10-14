package de.sunnix.srpge.engine.ecs.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.audio.AudioManager;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.States;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.ecs.systems.physics.AABB;
import de.sunnix.srpge.engine.ecs.systems.physics.CombatSystem;
import de.sunnix.srpge.engine.resources.Resources;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class CombatComponent extends Component{

    @Setter(AccessLevel.NONE)
    private AABB hitbox;
    private float width, height;
    private float maxHealth;
    private float maxMana;
    private float damage;
    private float armorFlat;
    private float damageReist;
    private float crit, critDmg = 2;
    private float knockBackResist;
    private int maxInvTime; // invulnerability time

    // on time values
    private float health;
    private float mana;
    private float kbX, kbZ;
    private float invTime;

    public CombatComponent(DataSaveObject dso) {
        super(dso);
    }

    @Override
    protected void load(DataSaveObject dso) {
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
    }

    @Override
    public void init(World world, GameObject go) {
        super.init(world, go);
        var pos = go.getPosition();
        hitbox = new AABB(pos.x, pos.y, pos.z, width, height);
        CombatSystem.add(go);
        go.addPositionSubscriber((oldPos, newPos, object) -> {
            CombatSystem.relocateGridObject(oldPos, newPos, object);
            hitbox = new AABB(newPos.x, newPos.y, newPos.z, width, height);
            if(go.getID() == 999){
                var colliding = new ArrayList<>(CombatSystem.getCollidingObjects(go));
                colliding.remove(go);
                if(colliding.isEmpty())
                   return;
                var enemy = colliding.get(0);
                damage(world, enemy, enemy.getComponent(CombatComponent.class).damage);
            } else {
                var player = world.getPlayer().getComponent(CombatComponent.class);
                if(hitbox.intersects(player.hitbox))
                    player.damage(world, go, damage);
            }
        });
    }

    public void update(World world, GameObject object) {
        if(invTime > 0) {
            invTime--;
            if(invTime == 0) {
                object.removeState(States.HURT.id());
                if(health <= 0)
                    onDie(world);
            }
        }
        if(kbX != 0 || kbZ != 0){
            var factor = .08f;
            object.getVelocity().add(kbX * factor * (1 - knockBackResist), 0, kbZ * factor * (1 - knockBackResist));
            kbX -= kbX * factor;
            if(kbX < .1 && kbX > -.1)
                kbX = 0;
            kbZ -= kbZ * factor;
            if(kbZ < .1 && kbZ > -.1)
                kbZ = 0;
        }
    }

    public boolean damage(World world, GameObject source, float damage){
        if(invTime > 0)
            return false;
        damage -= armorFlat;
        damage *= 1 - damageReist;
        damage = Math.max(0, damage);
        if(Math.random() < crit)
            damage *= critDmg;
        health -= damage;
        invTime = maxInvTime;
        parent.addState(States.HURT.id());
        AudioManager.get().playSound(Resources.get().getAudio("SE/Hurt"));
        var pos = parent.getPosition();
        var oPos = source.getPosition();
        var kb = 2f;
        var dX = Math.abs(pos.x - oPos.x);
        var dZ = Math.abs(pos.z - oPos.z);
        var xFactor = Math.min(1, dZ == 0 ? 1 : dX / dZ);
        var zFactor = Math.min(1, dX == 0 ? 1 : dZ / dX);
        kbX += kb * xFactor * (pos.x < oPos.x ? -1 : 1);
        kbZ += kb * zFactor * (pos.z < oPos.z ? -1 : 1);
        return true;
    }

    public void onDie(World world){
//        world.removeEntity(parent);
        parent.addState(States.DEAD.id());
    }

    @Override
    protected void free() {
        CombatSystem.remove(parent);
    }

}
