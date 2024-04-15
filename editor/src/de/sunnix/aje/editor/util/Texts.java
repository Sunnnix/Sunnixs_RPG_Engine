package de.sunnix.aje.editor.util;

public class Texts {

    public static final String FULL_NAME = "Alundra Java Engine Creator";
    public static final String WINDOW_NAME = "AJE Creator";
    public static final String VERSION = "INDEV 1.0";
    public static final String CREATOR = "Sunnix";
    public static final String WEBSITE_LABEL = "sunnix.de";
    public static final String WEBSITE_LINK = "https://sunnix.de";
    public static final String ABOUT_HTML_TEXT = String.format(getHTMLText("AboutSite.html"), FULL_NAME, VERSION, CREATOR, WEBSITE_LINK, WEBSITE_LABEL);

    public static String getHTMLText(String html){
        try(var stream = Texts.class.getResourceAsStream("/de/sunnix/aje/editor/window/html/" + html)){
            return new String(stream.readAllBytes());
        } catch (Exception e){
            return html + ": " + e.getMessage();
        }
    }



}
