package de.sunnix.aje.engine.memory;

import de.sunnix.aje.engine.util.Tuple;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An overview of all allocated MemoryHolder's to see, what how much of what category of memory are loaded
 */
public class MemoryHandler {

    private MemoryHandler() {}

    private static final HashMap<MemoryCategory, List<MemoryHolderRecord>> records = new HashMap<>();

    static {
        for(var category: MemoryCategory.values())
            records.put(category, Collections.synchronizedList(new ArrayList<>()));
    }

    public static void create(MemoryCategory category, String info, MemoryHolder memoryHolder){
        records.get(category).add(new MemoryHolderRecord(formatTime(GLFW.glfwGetTime()), category, info, memoryHolder));
    }

    public static void remove(MemoryHolder memoryHolder) {
        var list = records.get(memoryHolder.getMemoryCategory());
        var holder = list.stream().filter(mh -> mh.memoryHolder.equals(memoryHolder))
                .findFirst().orElse(null);
        if(holder == null)
            return;
        list.remove(holder);
    }

    public static int getAllSize(){
        return (int) records.values().stream().mapToLong(List::size).sum();
    }

    public static int getCategorySize(MemoryCategory category){
        return records.get(category).size();
    }

    public static List<Tuple.Tuple2<MemoryCategory, Integer>> getSizesWithCategories(){
        return records.entrySet().stream()
                .map(entry -> Tuple.create(entry.getKey(), entry.getValue().size()))
                .collect(Collectors.toList());
    }

    public static String getAllString(){
        return records.toString();
    }

    public static String getCategory(MemoryCategory category){
        return records.get(category).toString();
    }

    private static String formatTime(double time){
        var millis = (int)((time - (int) time) * 1000);
        var seconds = (int)(time % 60);
        var minutes = (int)(time / 60 % 60);
        var hours = (int)(time / 60 / 60);
        return String.format("%s:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }

    /**
     * Frees all resources.
     * Causes crash if not called in main thread
     */
    public static void freeAll() {
        records.values().forEach(list -> {
            // prevent ConcurrentModificationException
            var it = list.iterator();
            while(it.hasNext()) {
                var record = it.next();
                it.remove();
                record.memoryHolder.freeMemory();
            }
        });
    }

    private record MemoryHolderRecord(String time, MemoryCategory category, String info, MemoryHolder memoryHolder){

        @Override
        public String toString() {
            return String.format("%s - %s: (%s)", time, category, info);
        }
    }

}
