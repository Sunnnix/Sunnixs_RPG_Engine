package de.sunnix.aje.engine.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BetterJSONObject extends JSONObject {

    private static final Method wrongValueFormatException;

    static {
        try {
            wrongValueFormatException = JSONObject.class.getDeclaredMethod("wrongValueFormatException", String.class, String.class, Object.class, Throwable.class);
            wrongValueFormatException.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public BetterJSONObject(){
        super();
    }

    public BetterJSONObject(String src){
        super(src);
    }

    public BetterJSONObject(JSONObject obj){
        super(obj, obj.keySet().toArray(String[]::new));
    }

    public BetterJSONObject(Object bean) {
        super(bean);
    }

    public String get(String key, String defaultValue){
        if(has(key))
            return getString(key);
        else
            return defaultValue;
    }

    public int get(String key, int defaultValue){
        if(has(key))
            return getInt(key);
        else
            return defaultValue;
    }

    public float get(String key, float defaultValue){
        if(has(key))
            return getFloat(key);
        else
            return defaultValue;
    }

    public boolean get(String key, boolean defaultValue){
        if(has(key))
            return getBoolean(key);
        else
            return defaultValue;
    }

    @Override
    public BetterJSONObject getJSONObject(String key){
        if(has(key))
            return new BetterJSONObject(super.getJSONObject(key));
        else
            return new BetterJSONObject();
    }

    public byte[] getByteArray(String key) throws JSONException, InvocationTargetException, IllegalAccessException {
        var list = getJSONArray(key).toList();
        if(list.isEmpty())
            return new byte[0];
        var arr = new byte[list.size()];
        for (var i = 0; i < list.size(); i++) {
            var obj = list.get(i);
            if(obj instanceof Number num)
                arr[i] = num.byteValue();
            else
                throw (JSONException) wrongValueFormatException.invoke(this, key, "byte", obj, new ClassCastException("Value " + obj + " is no number!"));
        }
        return arr;
    }

}
