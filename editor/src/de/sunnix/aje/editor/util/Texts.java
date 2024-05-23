package de.sunnix.aje.editor.util;

import de.sunnix.aje.engine.Core;

import static de.sunnix.aje.editor.lang.Language.getString;

public class Texts {

    public static final String FULL_NAME = "Alundra Java Engine Creator";
    public static final String WINDOW_NAME = "AJE Creator V " + Core.VERSION;
    public static final String CREATOR = "Sunnix";
    public static final String WEBSITE_LABEL = "sunnix.de";
    public static final String WEBSITE_LINK = "https://sunnix.de";
    public static final String ABOUT_HTML_TEXT = String.format(getAboutSite(), FULL_NAME, Core.VERSION, CREATOR, WEBSITE_LINK, WEBSITE_LABEL);

    public static String getAboutSite(){
        try(var stream = Texts.class.getResourceAsStream("/de/sunnix/aje/editor/window/about/AboutSite.html")){
            return new String(stream.readAllBytes());
        } catch (Exception e){
            return getString("texts.error_loading_about", e.getMessage());
        }
    }



}
