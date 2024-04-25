package de.sunnix.aje.engine;

public enum DamageType {

    NORMAL(0), UNKNOWN(-1);

    public final int ID;

    DamageType(int id){
        this.ID = id;
    }

}
