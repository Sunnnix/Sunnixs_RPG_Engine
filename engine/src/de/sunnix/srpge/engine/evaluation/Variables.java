package de.sunnix.srpge.engine.evaluation;

import de.sunnix.sdso.DataSaveObject;

public class Variables {

    protected static int[] intVars;
    protected static float[] floatVars;
    protected static boolean[] boolVars;

    public static int getInt(int index, int def){
        if(index < 0 || index >= intVars.length)
            return def;
        else
            return intVars[index];
    }

    public static int getInt(int index) {
        return getInt(index, 0);
    }

    public static float getFloat(int index, float def){
        if(index < 0 || index >= floatVars.length)
            return def;
        else
            return floatVars[index];
    }

    public static float getFloat(int index) {
        return getFloat(index, 0);
    }

    public static boolean getBool(int index, boolean def){
        if(index < 0 || index >= boolVars.length)
            return def;
        else
            return boolVars[index];
    }

    public static boolean getBool(int index){
        return getBool(index, false);
    }

    public static void setIntVar(int index, int value){
        if(index < 0 || index >= intVars.length)
            return;
        intVars[index] = value;
    }

    public static void setFloatVar(int index, float value){
        if(index < 0 || index >= floatVars.length)
            return;
        floatVars[index] = value;
    }

    public static void setBoolVar(int index, boolean value){
        if(index < 0 || index >= boolVars.length)
            return;
        boolVars[index] = value;
    }

    public static void load(DataSaveObject dso){
        intVars = new int[dso.getInt("ints", 10)];
        floatVars = new float[dso.getInt("floats", 10)];
        boolVars = new boolean[dso.getInt("bools", 10)];
    }

}
