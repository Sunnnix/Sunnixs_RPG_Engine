package de.sunnix.srpge.editor.data;

import de.sunnix.sdso.DataSaveObject;

import java.util.Arrays;

public class Variables extends de.sunnix.srpge.engine.evaluation.Variables {

    private static String[] intNamings;
    private static String[] floatNamings;
    private static String[] boolNamings;

    private static String getName(int index, String[] array){
        if(index < 0 || index >= array.length)
            return null;
        return array[index];
    }

    public static String getIntName(int index){
        return getName(index, intNamings);
    }

    public static String getFloatName(int index){
        return getName(index, floatNamings);
    }

    public static String getBoolName(int index){
        return getName(index, boolNamings);
    }

    private static void setName(int index, String name, String[] array){
        if(index < 0 || index >= array.length)
            return;
        array[index] = name;
    }

    public static void setIntName(int index, String name){
        setName(index, name, intNamings);
    }


    public static void setFloatName(int index, String name){
        setName(index, name, floatNamings);
    }


    public static void setBoolName(int index, String name){
        setName(index, name, boolNamings);
    }

    public static int getIntsSize(){
        return intNamings.length;
    }

    public static int getFloatsSize(){
        return floatNamings.length;
    }

    public static int getBoolsSize(){
        return boolNamings.length;
    }

    public static void setIntsSize(int newSize){
        intVars = new int[newSize];
        intNamings = Arrays.copyOf(intNamings, newSize);
        // fill array with empty strings because DataSaveObject has a bug, where it can't handle null values in arrays
        for(var i = 0; i < intNamings.length; i++)
            if(intNamings[i] == null)
                intNamings[i] = "";
    }

    public static void setFloatsSize(int newSize){
        floatVars = new float[newSize];
        floatNamings = Arrays.copyOf(floatNamings, newSize);
        // fill array with empty strings because DataSaveObject has a bug, where it can't handle null values in arrays
        for(var i = 0; i < floatNamings.length; i++)
            if(floatNamings[i] == null)
                floatNamings[i] = "";
    }

    public static void setBoolsSize(int newSize){
        boolVars = new boolean[newSize];
        boolNamings = Arrays.copyOf(boolNamings, newSize);
        // fill array with empty strings because DataSaveObject has a bug, where it can't handle null values in arrays
        for(var i = 0; i < boolNamings.length; i++)
            if(boolNamings[i] == null)
                boolNamings[i] = "";
    }

    public static void reset(){
        intVars = new int[10];
        floatVars = new float[10];
        boolVars = new boolean[10];
        intNamings = new String[10];
        Arrays.fill(intNamings, "");
        floatNamings = new String[10];
        Arrays.fill(floatNamings, "");
        boolNamings = new String[10];
        Arrays.fill(boolNamings, "");
    }

    public static void load(DataSaveObject dso){
        de.sunnix.srpge.engine.evaluation.Variables.load(dso);
        intNamings = dso.getArray("s_ints", String[]::new);
        floatNamings = dso.getArray("s_floats", String[]::new);
        boolNamings = dso.getArray("s_bools", String[]::new);
        // for older game files
        if(intNamings.length < 10) {
            intNamings = new String[10];
            Arrays.fill(intNamings, "");
        }
        if(floatNamings.length < 10) {
            floatNamings = new String[10];
            Arrays.fill(floatNamings, "");
        }
        if(boolNamings.length < 10) {
            boolNamings = new String[10];
            Arrays.fill(boolNamings, "");
        }
    }

    public static DataSaveObject save(DataSaveObject dso){
        dso.putInt("ints", intVars.length);
        dso.putInt("floats", floatVars.length);
        dso.putInt("bools", boolVars.length);
        dso.putArray("s_ints", intNamings);
        dso.putArray("s_floats", floatNamings);
        dso.putArray("s_bools", boolNamings);
        return dso;
    }

}
