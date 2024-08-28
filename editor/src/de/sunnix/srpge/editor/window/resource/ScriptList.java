package de.sunnix.srpge.editor.window.resource;

import de.sunnix.srpge.editor.util.LoadingDialog;

import java.io.File;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ScriptList extends de.sunnix.srpge.engine.resources.ScriptList {

    public Collection<String> getScriptNames(ScriptType scriptType){
        return switch (scriptType) {
            case Event -> event.keySet();
        };
    }

    public void setScript(ScriptType scriptType, String scriptName, String script){
        switch (scriptType) {
            case Event -> event.put(scriptName, script);
        }
    }

    public void renameScript(ScriptType scriptType, String preName, String newName){
        var map = switch (scriptType) {
            case Event -> event;
        };
        var script = map.remove(preName);
        if(script != null)
            map.put(preName, newName);
    }

    public void removeScript(ScriptType scriptType, String scriptName){
        switch (scriptType) {
            case Event -> event.remove(scriptName);
        }
    }

    public void saveScripts(LoadingDialog dialog, ZipOutputStream zip) {
        try {
            var rootFolder = new File(new File("res", "script"), "lua");

            var subFolder = new File(rootFolder, "event");
            for (var e : event.entrySet()) {
                zip.putNextEntry(new ZipEntry(new File(subFolder, e.getKey() + ".lua").getPath()));
                zip.write(e.getValue().getBytes());
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
