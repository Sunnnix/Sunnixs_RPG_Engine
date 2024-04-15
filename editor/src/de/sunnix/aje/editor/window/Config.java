package de.sunnix.aje.editor.window;

import de.sunnix.aje.editor.util.BetterJSONObject;
import org.json.JSONArray;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Config {

    public static final File saveFile = new File("ajee.config");

    private BetterJSONObject data = new BetterJSONObject();

    public void loadConfig(){
        synchronized (saveFile) {
            if (!saveFile.exists()) {
                data = new BetterJSONObject();
                return;
            }
            try (var stream = new FileInputStream(saveFile)) {
                data = new BetterJSONObject(new String(stream.readAllBytes()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "There was an error reading configuration!\n" + e.getMessage(), "Can't read config", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveConfig() {
        synchronized (saveFile) {
            if (!saveFile.exists()) {
                try {
                    var parent = saveFile.getParentFile();
                    if(parent != null)
                        parent.mkdirs();
                    saveFile.createNewFile();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Config couldn't be created!\n" + e.getMessage(), "Can't create config", JOptionPane.ERROR_MESSAGE);
                }
            }
            try (var stream = new FileOutputStream(saveFile)) {
                stream.write(data.toString(2).getBytes());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "There was an error saving configuration!\n" + e.getMessage(), "Can't save config", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -----------------               GET               -----------------

    public String get(String key, String def){
        if(data.has(key))
            return data.getString(key);
        else
            return set(key, def);
    }

    public int get(String key, int def){
        if(data.has(key))
            return data.getInt(key);
        else
            return set(key, def);
    }

    public float get(String key, float def){
        if(data.has(key))
            return data.getFloat(key);
        else
            return set(key, def);
    }

    public boolean get(String key, boolean def){
        if(data.has(key))
            return data.getBoolean(key);
        else
            return set(key, def);
    }

    public <T> List<T> get(String key, List<T> def) throws ClassCastException{
        if(data.has(key))
            return data.getJSONArray(key).toList().stream().map(o -> (T) o).toList();
        else
            return set(key, def);
    }

    public int[] get(String key, int[] def) throws ClassCastException{
        if(data.has(key))
            return data.getJSONArray(key).toList().stream().mapToInt(o -> (int) o).toArray();
        else
            return set(key, def);
    }

    public double[] get(String key, double[] def) throws ClassCastException{
        if(data.has(key))
            return data.getJSONArray(key).toList().stream().mapToDouble(o -> (double) o).toArray();
        else
            return set(key, def);
    }

    public BetterJSONObject getJSONObject(String key){
        if(data.has(key))
            return data.getJSONObject(key);
        else
            return set(key, new BetterJSONObject());
    }

    // -----------------               SET               -----------------

    public <T> T set(String key, T value){
        if(value instanceof List<?> l)
            if(l.isEmpty())
                data.put(key, new JSONArray());
            else
                data.put(key, new JSONArray(l));
        else if(value instanceof int[] i)
            if(i.length == 0)
                data.put(key, new JSONArray());
            else
                data.put(key, new JSONArray(i));
        else if(value instanceof double[] d)
            if(d.length == 0)
                data.put(key, new JSONArray());
            else
                data.put(key, new JSONArray(d));
        else
            data.put(key, value);
        return value;
    }

    // -----------------               Misc               -----------------

    public <T> List<T> change(String key, List<T> def, Function<List<T>, List<T>> change){
        return set(key, change.apply(new ArrayList<>(get(key, def))));
    }

}
