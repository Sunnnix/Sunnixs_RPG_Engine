package de.sunnix.engine.registry;

import de.sunnix.engine.Core;
import de.sunnix.engine.util.Tuple;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GeneratorRegistrar<T extends ISavable> {

    protected Map<String, Supplier<T>> objects = new HashMap<>();
    protected Map<Class<? extends T>, String> dataIDList = new HashMap<>();

    @Setter
    public Supplier<T> defaultValue;

    public GeneratorRegistrar(Supplier<T> defaultValue){
        this.defaultValue = defaultValue;
    }

    public void register(String registryName, Class<? extends T> clazz, Supplier<T> generator){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        this.objects.put(registryName, generator);
        this.dataIDList.put(clazz, registryName);
    }

    public void register(Tuple.Tuple3<String, Class<? extends T>, Supplier<T>>... objects){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        for(var object : objects)
            this.register(object.t1(), object.t2(), object.t3());
    }

    public T create(String registryName) {
        return objects.getOrDefault(registryName, defaultValue).get();
    }

    public String getRegistryNameOf(T object){
        var key = dataIDList.get(object.getClass());
        if(key == null)
            throw new NullPointerException("object unknown");
        return key;
    }

}