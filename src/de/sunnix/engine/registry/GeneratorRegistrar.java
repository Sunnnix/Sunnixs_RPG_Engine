package de.sunnix.engine.registry;

import de.sunnix.engine.Core;
import de.sunnix.engine.util.Tuple;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GeneratorRegistrar<T> {

    protected Map<String, Tuple.Tuple2<Class<?>, Supplier<T>>> objects = new HashMap<>();

    @Setter
    public Tuple.Tuple2<Class<?>, Supplier<T>> defaultValue;

    public GeneratorRegistrar(Supplier<T> defaultValue){
        this.defaultValue = Tuple.create(null, defaultValue);
    }

    public void register(String registryName, Class<?> clazz, Supplier<T> object){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        this.objects.put(registryName, Tuple.create(clazz, object));
    }

    public void register(Tuple.Tuple3<String, Class<?>, Supplier<T>>... objects){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        for(var object : objects)
            this.objects.put(object.t1(), Tuple.create(object.t2(), object.t3()));
    }

    public T get(String registryName) {
        return this.objects.getOrDefault(registryName, defaultValue).t2().get();
    }

    public String getRegistryNameOf(T object){
        var clazz = object.getClass();
        for(var entry: this.objects.entrySet()){
            if(entry.getValue().t1().equals(clazz))
                return entry.getKey();
        }
        return null;
    }

}