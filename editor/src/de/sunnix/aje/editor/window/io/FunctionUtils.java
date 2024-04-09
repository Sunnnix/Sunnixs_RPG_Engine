package de.sunnix.aje.editor.window.io;

import java.util.Collection;
import java.util.function.Function;

public class FunctionUtils {

    public static <T> T firstOrNull(Collection<T> collection, Function<T, Boolean> expression){
        for(var item: collection){
            if(expression.apply(item))
                return item;
        }
        return null;
    }

    public static <T> T first(Collection<T> collection, Function<T, Boolean> expression){
        var value = firstOrNull(collection, expression);
        if(value == null)
            throw new NullPointerException("No matching value with this expression");
        return value;
    }

    public static <T> T firstOrElse(Collection<T> collection, Function<T, Boolean> expression, T elseValue){
        var value = firstOrNull(collection, expression);
        return value == null ? elseValue : value;
    }

}
