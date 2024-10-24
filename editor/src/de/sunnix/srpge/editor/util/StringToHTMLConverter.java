package de.sunnix.srpge.editor.util;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToHTMLConverter {

    public static String formatSimpleToHTML(String text){
        text = escapeHTML(text);

        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        text = text.replaceAll("\\*(.+?)\\*", "<i>$1</i>");

        text = replaceCommand("(?<!\\/)\\[#([0-9a-fA-F]{3}):(.*?)(?<!\\/)\\]", text, "<span style='color:#%s'>%s</span>"); // color hex 3
        text = replaceCommand("(?<!\\/)\\[#([0-9a-fA-F]{6}):(.*?)(?<!\\/)\\]", text, "<span style='color:#%s'>%s</span>"); // color hex 6

        text = text.replaceAll("\\/\\[", "[");
        text = text.replaceAll("\\/]", "]");

        return "<html>" + text + "</html>";
    }

    public static String escapeHTML(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String replaceCommand(String regex, String text, String replacement) {
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(text);

        while (matcher.find()) {
            var command = matcher.group(1);
            var innerText = matcher.group(2);

            var replaced = String.format(replacement, command, innerText);

            text = matcher.replaceFirst(Matcher.quoteReplacement(replaced));
            matcher = pattern.matcher(text);
        }
        return text;
    }

    public static String fat(Object text){
        return "**" +  text + "**";
    }

    public static String italic(Object text){
        return "*" +  text + "*";
    }

    public static String color(String hex, Object text){
        return "[#" + hex + ":" +  text + "]";
    }

    public static String color(Color color, Object text){
        return color(Integer.toHexString(color.getRGB()).substring(2, 8), text);
    }

}
