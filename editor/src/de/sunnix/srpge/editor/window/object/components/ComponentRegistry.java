package de.sunnix.srpge.editor.window.object.components;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.util.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ComponentRegistry {

    private static Map<String, Tuple.Tuple2<String, Supplier<? extends Component>>> registeredComponents = new HashMap<>();

    public static void registerComponent(String id, String displayName, Supplier<? extends Component> compSupplier){
        registeredComponents.put(id, new Tuple.Tuple2<>(displayName, compSupplier));
    }

    public static List<Tuple.Tuple2<String, String>> getAll(){
        return registeredComponents.entrySet().stream().map(e -> new Tuple.Tuple2<>(e.getKey(), e.getValue().t1())).toList();
    }

    public static Component createComponent(String id){
        var eventSupplier = registeredComponents.get(id);
        if(eventSupplier != null)
            return eventSupplier.t2().get();
        return null;
    }

    public static Component loadComponent(String id, DataSaveObject dso){
        var compSupplier = registeredComponents.get(id);
        Component comp;
        if(compSupplier == null)
            return null;
        else
            comp = compSupplier.t2().get();
        comp.load(dso);
        return comp;
    }

}