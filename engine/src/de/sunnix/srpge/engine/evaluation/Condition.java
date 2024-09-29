package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.debug.GameLogger;
import de.sunnix.srpge.engine.ecs.World;
import lombok.Getter;

public abstract class Condition<T> {

    @Getter
    public final String ID;
    protected ValueProvider<? extends T> provider;

    public Condition(String id){
        this.ID = id;
    }

    @SuppressWarnings("unchecked")
    public void load(DataSaveObject dso) {
        var pDSO = dso.getObject("provider");
        if(pDSO != null) {
            try {
                provider = (ValueProvider<? extends T>) EvaluationRegistry.createProvider(pDSO.getString("id", null), pDSO);
            } catch (ClassCastException e) {
                GameLogger.logException("Condition-Loading", e);
            }
        }
    }

    public abstract boolean evaluate(World world);


}
