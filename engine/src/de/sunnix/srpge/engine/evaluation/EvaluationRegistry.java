package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EvaluationRegistry {

    private static final NULLCondition NULL_CONDITION = new NULLCondition();

    private static final Map<String, Supplier<Condition<?>>> conditions = new HashMap<>();
    private static final Map<String, Supplier<ValueProvider<?>>> providers = new HashMap<>();

    public static void registerCondition(String id, Supplier<Condition<?>> conditionBuilder){
        conditions.put(id, conditionBuilder);
    }

    public static void registerProvider(String id, Supplier<ValueProvider<?>> providerBuilder){
        providers.put(id, providerBuilder);
    }

    public static Condition<?> createCondition(String id, DataSaveObject dso){
        var builder = conditions.get(id);
        if(builder == null)
            return NULL_CONDITION;
        var condition = builder.get();
        condition.load(dso);
        return condition;
    }

    public static ValueProvider<?> createProvider(String id, DataSaveObject dso){
        var builder = providers.get(id);
        if(builder == null)
            return null;
        var provider = builder.get();
        provider.load(dso);
        return provider;
    }

}
