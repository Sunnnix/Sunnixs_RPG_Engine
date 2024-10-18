package de.sunnix.srpge.engine.ecs.event;

import de.sunnix.sdso.DataSaveObject;
import de.sunnix.srpge.engine.Core;
import de.sunnix.srpge.engine.ecs.GameObject;
import de.sunnix.srpge.engine.ecs.World;
import de.sunnix.srpge.engine.resources.Resources;
import de.sunnix.srpge.engine.resources.ScriptList;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaScriptEvent extends Event{

    protected String script;
    private LuaValue chunk;

    private static Globals globals;

    static {
        globals = Core.isDebug() ? JsePlatform.debugGlobals() : JsePlatform.standardGlobals();
    }

    public LuaScriptEvent() {
        super("script-lua");
    }

    @Override
    public void load(DataSaveObject dso) {
        script = dso.getString("script", null);
    }

    @Override
    public void prepare(World world, GameObject parent) {
        var rawScript = Resources.get().scripts.getScript(ScriptList.ScriptType.Event, script);
        if(rawScript != null)
            chunk = globals.load(rawScript);
    }

    @Override
    public void run(World world) {
        if(chunk == null)
            return;
        var lua_world = CoerceJavaToLua.coerce(world);
        globals.set("world", lua_world);
        chunk.call();
    }

    @Override
    public boolean isFinished(World world) {
        return chunk == null;
    }

    @Override
    public void finish(World world) {

    }

}
