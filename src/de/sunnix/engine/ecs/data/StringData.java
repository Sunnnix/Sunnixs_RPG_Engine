package de.sunnix.engine.ecs.data;

import de.sunnix.sdso.DataSaveObject;

import java.util.function.Supplier;

public class StringData extends Data<String>{
    public StringData(String key, String defValue) {
        super(key, () -> defValue);
    }

}
