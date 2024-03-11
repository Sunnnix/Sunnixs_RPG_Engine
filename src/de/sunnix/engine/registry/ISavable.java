package de.sunnix.engine.registry;

import de.sunnix.sdso.DataSaveObject;

public interface ISavable {

    void save(DataSaveObject dso);

    void load(DataSaveObject dso);

}
