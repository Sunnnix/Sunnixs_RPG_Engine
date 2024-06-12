package de.sunnix.srpge.engine;

public enum DamageType {

    NORMAL(0), UNKNOWN(-1);

    public final int ID;

    DamageType(int id){
        this.ID = id;
    }

}
