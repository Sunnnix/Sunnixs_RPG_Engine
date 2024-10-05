package de.sunnix.srpge.editor.util;

import de.sunnix.srpge.engine.Core;

import static de.sunnix.srpge.editor.lang.Language.getString;

public class Texts {

    public static final String FULL_NAME = "Sunnix's RPG Engine";
    public static final String WINDOW_NAME = "Sunnix's RPG Creator V " + Core.VERSION;
    public static final String CREATOR = "Sunnix";
    public static final String WEBSITE_LABEL = "sunnix.de";
    public static final String WEBSITE_LINK = "https://sunnix.de";
    public static final String[] SPECIAL_THANKS = {
            "Märchenwald",
            "Epix"
    };
    public static final String ABOUT_HTML_TEXT = String.format(getAboutSite(), FULL_NAME, Core.VERSION, getSpecialThanks(), CREATOR, WEBSITE_LINK, WEBSITE_LABEL);

    private static String getSpecialThanks(){
        var sb = new StringBuilder();
        sb.append("<b>").append(SPECIAL_THANKS[0]).append("</b>");
        for (int i = 1; i < SPECIAL_THANKS.length; i++) {
            if(i == SPECIAL_THANKS.length - 1)
                sb.append(" and ");
            else
                sb.append(", ");
            sb.append("<b>").append(SPECIAL_THANKS[i]).append("</b>");
        }
        return sb.toString();
    }

    public static String getAboutSite(){
        try(var stream = Texts.class.getResourceAsStream("/de/sunnix/srpge/editor/window/about/AboutSite.html")){
            return new String(stream.readAllBytes());
        } catch (Exception e){
            return getString("texts.error_loading_about", e.getMessage());
        }
    }



}
