package de.sunnix.engine.debug;

import java.util.HashMap;
import java.util.Map;

public class BuildData {

    private static final Map<String, String> properties = new HashMap<>();

    public static void create() {
        properties.clear();
        try(var file = BuildData.class.getResourceAsStream("/debug/project.properties")) {
            var data = new String(file.readAllBytes()).split("\n");
            for(var s : data) {
                if(s.startsWith("#"))
                    continue;
                var i = s.indexOf('=');
                if (i <= 0 && i < s.length() - 1)
                    continue;
                properties.put(s.substring(0, i), s.substring(i + 1).trim());
            }
        } catch (Exception e){
            new RuntimeException("Error creating BuildData. Can't read project.properites", e).printStackTrace();
        }
    }

    public static String getData(String key){
        return getData(key, null);
    }

    public static String getData(String key, String defaultValue){
        return properties.getOrDefault(key, defaultValue);
    }

}
