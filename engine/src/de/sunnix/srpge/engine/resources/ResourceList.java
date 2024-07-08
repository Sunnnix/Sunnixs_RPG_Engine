package de.sunnix.srpge.engine.resources;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.debug.GameLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceList<T> {

    /**
     * The name of the resource list, used as the base folder name for storing its data.
     */
    public final String name;

    /**
     * A consumer that defines actions to be performed when a resource is removed.
     */
    private final Consumer<T> onRemoving;

    /**
     * A map storing the resources, categorized by their respective categories.
     */
    private final Map<String, Map<String, T>> resources = new HashMap<>();


    /**
     * Constructs a ResourceList with a specified name and an onRemoving action.
     *
     * @param name       The name of the resource list. It should be unique as it is used to create a folder for its data.
     * @param onRemoving A consumer that defines actions to be performed when a resource is removed. Pass null if not needed.
     */
    public ResourceList(String name, Consumer<T> onRemoving) {
        this.name = name;
        this.onRemoving = onRemoving;
    }

    /**
     * Constructs a ResourceList with a specified name.
     *
     * @param name The name of the resource list. It should be unique as it is used to create a folder for its data.
     */
    public ResourceList(String name){
        this(name, null);
    }

    /**
     * Resets the resource list by clearing all resources. If an onRemoving action is defined, it is applied to each resource before removal.
     */
    public void reset(){
        if(onRemoving != null)
            resources.values().forEach(entry -> entry.values().forEach(onRemoving));
        resources.clear();
    }

    /**
     * Retrieves a resource by its full name (category/name).
     *
     * @param name The full name of the resource.
     * @return The resource, or null if not found.
     */
    public T getData(String name){
        if(name == null)
            return null;
        var split = name.split("/");
        if(split.length < 2)
            return null;
        return getData(split[0], split[1]);
    }

    /**
     * Retrieves a resource by its category and name.
     *
     * @param category The category name.
     * @param name     The resource name.
     * @return The resource, or null if not found.
     */
    public T getData(String category, String name){
        if(category == null || name == null)
            return null;
        var cat = resources.get(category);
        if(cat == null)
            return null;
        return cat.get(name);
    }

    /**
     * Loads resources from a zip file.
     *
     * @param zip          The zip file containing the resources.
     * @param generateData A function to generate resource objects from data save objects.
     */
    public void load(ZipFile zip, Function<DataSaveObject, T> generateData) {
        reset();
        var subFolder = new File("res", name);
        try{
            var entries = zip.entries();
            var files = new ArrayList<ZipEntry>();
            while(entries.hasMoreElements()){
                var e = entries.nextElement();
                if(!e.toString().startsWith(subFolder.getPath()))
                    continue;
                files.add(e);
            }
            for(var e: files){
                var category = resources.computeIfAbsent(e.getName().substring(subFolder.getPath().length() + 1), k -> new HashMap<>());
                var dsos = new DataSaveObject().load(zip.getInputStream(e)).<DataSaveObject>getList(name);
                for (DataSaveObject dso : dsos) {
                    var name = dso.getString("name", null);
                    if(name == null)
                        continue;
                    var data = generateData.apply(dso);
                    if(data == null) {
                        GameLogger.logE("Resource Loading", "Error on loading data %s from %s resources", name, this.name);
                        continue;
                    }
                    category.put(name, data);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            GameLogger.logException("Resource Loading", e);
        }
    }

}
