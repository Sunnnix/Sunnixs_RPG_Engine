package de.sunnix.engine.ecs.data;

import de.sunnix.engine.registry.IRegistry;
import de.sunnix.sdso.DataSaveObject;

import java.util.HashMap;
import java.util.function.Supplier;

public class DataRegistry implements IRegistry {

    private static HashMap<Byte, Supplier<Data<?>>> data = new HashMap<>();
    private static HashMap<Class<? extends Data>, Byte> dataIDList = new HashMap<>();

    @Override
    public void register() {

    }

    public static Data<?> createData(DataSaveObject dso){
        return switch (dso.getByte("type", (byte) 0)){
            case 0 -> new StringData(dso.getString("key", null), null);
            default -> throw new IllegalStateException("Unexpected value: " + dso.getByte("type", (byte) 0));
        };
    }

}
