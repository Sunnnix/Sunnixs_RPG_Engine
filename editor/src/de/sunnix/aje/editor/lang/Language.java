package de.sunnix.aje.editor.lang;

import de.sunnix.aje.editor.window.Config;
import de.sunnix.aje.engine.util.Tuple;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Language {

    private static final String LANGUAGE_PATH = "/de/sunnix/aje/editor/lang/";
    private static Map<String, String> texts = new HashMap<>();
    private static Map<String, String> texts_en = new HashMap<>();

    @Getter
    @Setter
    private static boolean useEnglishForMissing = true;

    private static List<Tuple.Tuple3<String, String, Boolean>> languages = new ArrayList<>(List.of(
            EN_LANG = Tuple.create("en", "English", true),
            Tuple.create("de", "Deutsch", true),
            Tuple.create("it", "Italiano", true),
            Tuple.create("fr", "Français", true),
            Tuple.create("es", "Español", true)
    ));

    private static final Tuple.Tuple3<String, String, Boolean> EN_LANG;

    private static Map<String, String> externalLanguages = new HashMap<>();

    @Getter
    private static int selectedLanguage = 0;

    public static String[] getLanguages(){
        return languages.stream().map(Tuple.Tuple3::t1).toArray(String[]::new);
    }

    public static String[] genLanguageNames(){
        return languages.stream().map(Tuple.Tuple3::t2).toArray(String[]::new);
    }

    public static Tuple.Tuple3<String, String, Boolean>[] getLanguagePacks(){
        return languages.toArray(Tuple.Tuple3[]::new);
    }

    public static void setupConfig(Config config){
        config.get("imported_languages", Collections.<String>emptyList()).forEach(s -> {
            try {
                var split = s.split("=");
                if(split.length < 2)
                    return;
                if(languages.stream().anyMatch(l -> l.t1().equals(split[0])))
                    return;
                externalLanguages.put(split[0], split[1]);
                loadLanguagePack(split[1], config);
            } catch (Exception e){}
        });
    }

    public static boolean loadLanguagePack(String file, Config config){
        try (var reader = new BufferedReader(new FileReader(file))){
            var line = reader.readLine();
            if(!line.startsWith("Language File")){
                // todo show error text
                System.out.println("Invalid lang file");
                return false;
            }
            var index = line.indexOf('{');
            var index2 = line.indexOf(',');
            if(index == -1 || index2 == -1){
                // todo show error text
                System.out.println("Invalid lang file");
                return false;
            }
            var lang = line.substring(index + 1, index2).trim();
            index = index2;
            index2 = line.indexOf('}');
            if(index2 == -1){
                // todo show error text
                System.out.println("Invalid lang file");
                return false;
            }
            var langName = line.substring(index + 1, index2).trim();
            if(!Language.loadLanguagePack(lang, langName)){
                // todo show error text
                System.out.println("Couldn't load language");
                return false;
            }
            externalLanguages.put(lang, file);
            config.set("imported_languages", externalLanguages.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean loadLanguagePack(String lang, String langName){
        if(languages.stream().anyMatch(l -> l.t1().equals(lang)))
            return false;
        languages.add(new Tuple.Tuple3<>(lang, langName, false));
        return true;
    }

    public static boolean removeLanguagePack(String lang, Config config){
        var value = languages.stream().filter(l -> l.t1().equalsIgnoreCase(lang)).findFirst().orElse(null);
        if(value != null && value.t3())
            throw new RuntimeException("The Language is a default type and can't be removed");
        languages.removeIf(l -> l.t1().equalsIgnoreCase(lang));
        externalLanguages.remove(lang);
        config.set("imported_languages", externalLanguages.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList());
        return true;
    }

    public static boolean setLanguage(String lang){
        var data = languages.stream().filter(l -> l.t1().equalsIgnoreCase(lang)).findFirst().orElse(EN_LANG);
        try {
            texts_en = loadLanguageIntoMap("en", true);
            if(data.t1().equalsIgnoreCase("en"))
                texts = texts_en;
            else
                if(data.t3())
                    texts = loadLanguageIntoMap(data.t1(), true);
                else
                    texts = loadLanguageIntoMap(externalLanguages.get(data.t1()), false);
            selectedLanguage = Arrays.stream(getLanguages()).toList().indexOf(data.t1());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        UIManager.put("OptionPane.okButtonText", getString("button.ok"));
        UIManager.put("OptionPane.cancelButtonText", getString("button.cancel"));
        UIManager.put("OptionPane.yesButtonText", getString("button.yes"));
        UIManager.put("OptionPane.noButtonText", getString("button.no"));
        return true;
    }

    private static HashMap<String, String> loadLanguageIntoMap(String file, boolean resource) throws IOException{
        var map = new HashMap<String, String>(texts.size());
        try (var reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(resource ? Language.class.getResourceAsStream(LANGUAGE_PATH + file + ".lang") : new FileInputStream(file)), StandardCharsets.ISO_8859_1))) {
            var lines = reader.lines().toArray(String[]::new);
            for (var line : lines) {
                if (line.startsWith("#"))
                    continue;
                var pair = line.split("=", 2);
                if (pair.length < 2)
                    continue;
                map.put(pair[0], pair[1].replace("\\n", "\n").trim());
            }
        }
        return map;
    }

    public static String getString(String key){
        return texts.getOrDefault(key, useEnglishForMissing ? texts_en.getOrDefault(key, key) : key);
    }

    public static String getString(String key, Object... args){
        var s = getString(key);
        try {
            return String.format(s, args);
        } catch (IllegalFormatException e) {
            return s;
        }
    }
}
