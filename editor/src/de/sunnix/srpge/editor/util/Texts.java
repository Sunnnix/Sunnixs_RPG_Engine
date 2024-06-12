package de.sunnix.srpge.editor.util;

import de.sunnix.srpge.engine.Core;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class Texts {

    public static final String FULL_NAME = "Sunnix's RPG Engine";
    public static final String WINDOW_NAME = "Sunnix's RPG Creator V " + Core.VERSION;
    public static final String CREATOR = "Sunnix";
    public static final String WEBSITE_LABEL = "sunnix.de";
    public static final String WEBSITE_LINK = "https://sunnix.de";
    public static final String ABOUT_HTML_TEXT = String.format(getAboutSite(), FULL_NAME, Core.VERSION, CREATOR, WEBSITE_LINK, WEBSITE_LABEL);

    public static String getAboutSite(){
        try(var stream = Texts.class.getResourceAsStream("/de/sunnix/srpge/editor/window/about/AboutSite.html")){
            return new String(stream.readAllBytes());
        } catch (Exception e){
            return getString("texts.error_loading_about", e.getMessage());
        }
    }



}
