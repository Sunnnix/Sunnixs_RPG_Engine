package de.sunnix.sunlundra;

import java.util.HashMap;
import java.util.Map;

public class BuildData {

    static {
        var map = new HashMap<String, String>();
        try(var file = BuildData.class.getResourceAsStream("/project.properties")) {
            var data = new String(file.readAllBytes()).split("\n");
            for(var s : data){
                if(s.startsWith("#"))
                    continue;
                var i = s.indexOf('=');
                if (i <= 0 && i < s.length() - 1)
                    continue;
                map.put(s.substring(0, i), s.substring(i + 1).trim());
            }
        } catch (Exception e){
            new RuntimeException("Error reading project.properites", e).printStackTrace();
        }
        properties = map;
    }

    private static final Map<String, String> properties;

    public static String getData(String key){
        return getData(key, null);
    }

    public static String getData(String key, String defaultValue){
        return properties.getOrDefault(key, defaultValue);
    }

}
