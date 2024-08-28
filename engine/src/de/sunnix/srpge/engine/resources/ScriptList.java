package de.sunnix.srpge.engine.resources;

import java.io.File;
import java.util.HashMap;
import java.util.zip.ZipFile;

public class ScriptList {

    protected HashMap<String, String> event = new HashMap<>();

    private static final String[] EVENT_PARAMS = {
            "world"
    };

    public enum ScriptType{
        Event(EVENT_PARAMS);

        public final String[] defaultParams;

        ScriptType(String... defaultParams){
            this.defaultParams = defaultParams;
        }
    }

    public String getScript(ScriptType scriptType, String scriptName){
        return switch (scriptType) {
            case Event -> event.get(scriptName);
        };
    }

    public void reset() {
        event.clear();
    }

    public void loadScripts(ZipFile zip) {
        try {
            reset();
            var rootFolder = new File(new File("res", "script"), "lua");

            var subFolder = new File(rootFolder, "event");
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                if (!e.toString().startsWith(subFolder.getPath()))
                    continue;
                var name = new File(e.getName()).getName();
                name = name.substring(0, name.length() - 4);
                try (var stream = zip.getInputStream(e)) {
                    event.put(name, new String(stream.readAllBytes()));
                }
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
