package de.sunnix.engine.ecs;

import de.sunnix.engine.debug.GameLogger;
import de.sunnix.engine.ecs.data.Data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GameData {

    String key();

}
