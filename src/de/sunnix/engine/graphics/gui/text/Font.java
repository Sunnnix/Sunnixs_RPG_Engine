package de.sunnix.engine.graphics.gui.text;

import de.sunnix.engine.graphics.Texture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Font {
    public static final Font COMIC_SANS = new Font("/data/font/cs/cs");
    public static final Font TIMES_NEW_ROMAN = new Font("/data/font/tnr/tnr");
    public static final Font CASCADIA_CODE = new Font("/data/font/cc/cc");
    public static final Font AGENCY_FB = new Font("/data/font/afb/afb");
    public static final byte STYLE_NORMAL = 0b00;
    public static final byte STYLE_ITALIC = 0b01;
    public static final byte STYLE_BOLD = 0b10;

    private final Texture tex_normal;
    private Texture tex_italic, tex_bold, tex_italic_bold;
    private final Map<Character, Glyph> glyphs_normal;
    private Map<Character, Glyph> glyphs_italic, glyphs_bold, glyphs_italic_bold;

    public Font(String fontPath) {
        glyphs_normal = loadFontData(fontPath);
        tex_normal = new Texture(fontPath + ".png");
        // italic
        try {
            glyphs_italic = loadFontData(fontPath + "_italic");
            tex_italic = new Texture(fontPath + "_italic.png");
        } catch (Exception e){
            glyphs_italic = glyphs_normal;
            tex_italic = tex_normal;
        }
        // bold
        try {
            glyphs_bold = loadFontData(fontPath + "_bold");
            tex_bold = new Texture(fontPath + "_bold.png");
        } catch (Exception e){
            glyphs_bold = glyphs_normal;
            tex_bold = tex_normal;
        }
        // italic
        try {
            glyphs_italic_bold = loadFontData(fontPath + "_italic_bold");
            tex_italic_bold = new Texture(fontPath + "_italic_bold.png");
        } catch (Exception e){
            glyphs_italic_bold = glyphs_normal;
            tex_italic_bold = tex_normal;
        }
    }

    private Map<Character, Glyph> loadFontData(String fontFntPath) {
        Map<Character, Glyph> characterMap = new HashMap<>();

        try (InputStream inputStream = Font.class.getResourceAsStream(fontFntPath + ".fnt");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("char "))
                    continue;
                String[] parts = line.trim().replaceAll(" +", " ").split(" ");
                if (parts.length >= 9) {
                    char character = (char) Integer.parseInt(parts[1].split("=")[1]);
                    int x = Integer.parseInt(parts[2].split("=")[1]);
                    int y = Integer.parseInt(parts[3].split("=")[1]);
                    int width = Integer.parseInt(parts[4].split("=")[1]);
                    int height = Integer.parseInt(parts[5].split("=")[1]);
                    int xOffset = Integer.parseInt(parts[6].split("=")[1]);
                    int yOffset = Integer.parseInt(parts[7].split("=")[1]);
                    int xAdvance = Integer.parseInt(parts[8].split("=")[1]);

                    Glyph glyph = new Glyph(character, x, y, width, height, xOffset, yOffset, xAdvance);
                    characterMap.put(character, glyph);
                }
            }

        } catch (NullPointerException e){
            throw new RuntimeException("Font " + fontFntPath + " don't exist", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return characterMap;
    }

    public Texture getTexture(byte style) {
        return switch (style){
            case STYLE_ITALIC -> tex_italic;
            case STYLE_BOLD -> tex_bold;
            case STYLE_ITALIC | STYLE_BOLD -> tex_italic_bold;
            default -> tex_normal;
        };
    }

    public Glyph getGlyph(byte style, char character) {
        return switch (style){
            case STYLE_ITALIC -> glyphs_italic.getOrDefault(character, glyphs_italic.get('?'));
            case STYLE_BOLD -> glyphs_bold.getOrDefault(character, glyphs_bold.get('?'));
            case STYLE_ITALIC | STYLE_BOLD -> glyphs_italic_bold.getOrDefault(character, glyphs_italic_bold.get('?'));
            default -> glyphs_normal.getOrDefault(character, glyphs_normal.get('?'));
        };
    }

}
