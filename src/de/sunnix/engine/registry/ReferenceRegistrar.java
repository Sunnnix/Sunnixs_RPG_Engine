package de.sunnix.engine.registry;

import de.sunnix.engine.Core;
import de.sunnix.engine.util.Tuple;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ReferenceRegistrar<T> {

    protected Map<String, T> objects = new HashMap<>();

    @Setter
    public T defaultValue;

    public ReferenceRegistrar(T defaultValue){
        this.defaultValue = defaultValue;
    }

    public void register(String registryName, T object){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        this.objects.put(registryName, object);
    }

    public void register(Tuple.Tuple2<String, T>... objects){
        Core.validateCoreStage(Core.CoreStage.STARTING);
        for(var object : objects)
            this.objects.put(object.t1(), object.t2());
    }

    public T get(String registryName){
        return this.objects.getOrDefault(registryName, defaultValue);
    }

    public String getRegistryNameOf(T object){
        for(var entry: this.objects.entrySet()){
            if(entry.getValue().equals(object))
                return entry.getKey();
        }
        return null;
    }

}
