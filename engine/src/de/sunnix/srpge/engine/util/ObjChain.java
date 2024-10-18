package de.sunnix.srpge.engine.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A utility class that allows for chaining operations on an object while
 * providing null safety. This class helps to streamline the handling of
 * optional values and enables fluent programming by supporting method chaining.
 *
 * @param <T> the type of the object being wrapped by this chain
 */
public class ObjChain<T> {

    private final T object;

    /**
     * Constructs a new ObjChain with the specified object.
     *
     * @param object the object to be wrapped
     */
    public ObjChain(T object) {
        this.object = object;
    }

    /**
     * Applies a function to the wrapped object and returns a new ObjChain
     * containing the result. If the wrapped object is null, the resulting
     * ObjChain will also contain null.
     *
     * @param func the function to apply to the wrapped object
     * @param <U>  the type of the result
     * @return a new ObjChain containing the result of the function application
     */
    public <U> ObjChain<U> next(Function<T, U> func) {
        return new ObjChain<>(object != null ? func.apply(object) : null);
    }

    /**
     * Executes a specified consumer function if the wrapped object is not null.
     *
     * @param func the consumer function to execute
     * @return this ObjChain for method chaining
     */
    public ObjChain<T> ifPresent(Consumer<T> func) {
        if (object != null) {
            func.accept(object);
        }
        return this;
    }

    /**
     * Returns the wrapped object.
     *
     * @return the wrapped object
     */
    public T get() {
        return object;
    }

    /**
     * Returns the wrapped object if present; otherwise, returns a specified
     * default value.
     *
     * @param def the default value builder to return if the wrapped object is null
     * @return the wrapped object or the specified default value
     */
    public T orElse(Supplier<T> def) {
        return object != null ? object : def.get();
    }

    /**
     * Returns the wrapped object if present; otherwise, returns a specified
     * default value.
     *
     * @param def the default value to return if the wrapped object is null
     * @return the wrapped object or the specified default value
     */
    public T orElse(T def) {
        return object != null ? object : def;
    }
}
