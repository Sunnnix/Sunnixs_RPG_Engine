package de.sunnix.aje.editor.util;

public class StringToHTMLConverter {

    public static final String[] colors = {
            "0ff"
    };

    public static String convertToHTML(String text){
        StringBuilder html = new StringBuilder("<html>");
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '/' && i + 1 < text.length()) {
                switch (text.charAt(i + 1)){
                    case 'c', 'C': // Color
                        i += 2; // Skip /c
                        if (i < text.length()) {
                            char nextChar = text.charAt(i);
                            if (nextChar == 'x') {
                                html.append("</span>");
                                i++;
                            } else if (nextChar == 'v' && i + 2 < text.length()) {
                                String varIdStr = text.substring(i + 1, i + 3);
                                try {
                                    int varId = Integer.parseInt(varIdStr);
                                    String colorCode = varId >= 0 && varId < colors.length ? colors[varId] : null;
                                    if (colorCode != null) {
                                        html.append("<span style='color:#").append(colorCode).append(";'>");
                                    } else {
                                        html.append("</span>");
                                    }
                                } catch (NumberFormatException e) {
                                    // Falls die Variable keine gültige Zahl ist, ignorieren wir sie und machen weiter.
                                }
                                i += 3;
                            } else if (i + 2 < text.length()) {
                                String colorCode = text.substring(i, i + 3);
                                html.append("<span style='color:#").append(colorCode).append(";'>");
                                i += 3;
                            }
                        }
                        break;
                    case 'b', 'B': // Bold
                        html.append("<b>");
                        i += 2;
                        break;
                    case 'i', 'I': // Italic
                        html.append("<i>");
                        i += 2;
                        break;
                    case 'n', 'N': // Normal
                        html.append("</b></i>");
                        i += 2;
                        break;
                    default:
                        html.append(text.charAt(i + 1));
                        i++;
                        continue;
                }
            } else {
                html.append(text.charAt(i));
                i++;
            }
        }
        html.append("</html>");
        return html.toString();
    }

}
