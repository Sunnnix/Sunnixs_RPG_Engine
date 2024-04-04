package de.sunnix.aje.editor.window;

public class Texts {

    public static final String FULL_NAME = "Alundra Java Engine Creator";
    public static final String WINDOW_NAME = "AJE Creator";
    public static final String VERSION = "INDEV 1.0";
    public static final String CREATOR = "Sunnix";
    public static final String WEBSITE_LABEL = "sunnix.de";
    public static final String WEBSITE_LINK = "https://sunnix.de";
    public static final String ABOUT_TEXT = """
            This editor is intended for creating and modifying game files for the Alundra Java Engine.<br>
            The game file is structured like a ZIP and the resources and maps are stored in different folders.""";
    public static final String ABOUT_HTML_TEXT = String.format("""
                <html>
                <body>
                <h1>%s</h1>
                <h2>Version: %s</h2>
                <p>%s</p>
                <br>
                <p>Created by %s</p>
                <p>visit <a href="%s">%s</a></p>
                </body>
                </html>""", FULL_NAME, VERSION, ABOUT_TEXT, CREATOR, WEBSITE_LINK, WEBSITE_LABEL);

}
