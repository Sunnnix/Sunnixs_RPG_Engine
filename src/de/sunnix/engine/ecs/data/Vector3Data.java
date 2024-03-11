package de.sunnix.engine.ecs.data;

import org.joml.Vector3f;

import java.util.function.Supplier;

public class Vector3Data extends Data<Vector3f> {

    public Vector3Data(String key, Supplier<Vector3f> generator) {
        super(key, generator);
    }

}
