/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.font;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFontRenderer extends CFont {
    protected final CFont.CharData[] boldChars = new CFont.CharData[256];
    protected final CFont.CharData[] italicChars = new CFont.CharData[256];
    protected final CFont.CharData[] boldItalicChars = new CFont.CharData[256];
    private final int[] colorCode = new int[32];
    private final String colorcodeIdentifiers = "0123456789abcdefklmnor";
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;

    public CFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        setupMinecraftColorcodes();
        setupBoldItalicIDs();
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        return Math.max(drawString(text, x + 0.5d, y + 0.5d, color, true), drawString(text, x, y, color, false));
    }

    public float drawString(String text, float x, float y, int color) {
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        RenderUtils.glColor(color);
        return drawString(text, x, (double) y, color, false);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        return drawString(text, (float) (x - ((double) ((float) (getStringWidth(text) / 2)))), (float) y, color);
    }

    public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
        return drawStringWithShadow(text, x - ((float) (getStringWidth(text) / 2)), y, color);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        return drawStringWithShadow(text, x - ((double) (getStringWidth(text) / 2)), y, color);
    }
    public static boolean isChinese(char c) {
        String s = String.valueOf(c);
        return "1234567890abcdefghijklmnopqrstuvwxyz!<>@#$%^&*()-_=+[]{}|\\/'\",.~`".contains(s.toLowerCase());
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[一-龥]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static char validateLegalString(String content) {
        String illegal = "`~!#%^&*=+\\|{};:'\",<>/?○●★☆☉♀♂※¤╬の〆";
        char isLegalChar = 't';

        for(int i = 0; i < content.length(); ++i) {
            for(int j = 0; j < illegal.length(); ++j) {
                if (content.charAt(i) == illegal.charAt(j)) {
                    isLegalChar = content.charAt(i);
                    return isLegalChar;
                }
            }
        }

        return isLegalChar;
    }
    public static int DisplayFontWidth(String str, CFontRenderer font) {
        str = LanguageManager.INSTANCE.get(LanguageManager.INSTANCE.replace(str));
        int x=0;
        for(int iF = 0; iF < str.length(); ++iF) {
            String s = String.valueOf(str.toCharArray()[iF]);
            if (s.contains("§") && iF + 1 <= str.length()) {
                iF++;
            } else if (isChinese(s.charAt(0))) {
                x += (float)font.getStringWidth(s);
            } else {
                x += (float)Fonts.font35.getStringWidth(s);
            }
        }
        return x+5;
    }

    public int DisplayFontWidths(CFontRenderer font,String str) {
        return DisplayFontWidths(str,font);
    }
    public int DisplayFontWidths(String str, CFontRenderer font) {
        str = LanguageManager.INSTANCE.get(LanguageManager.INSTANCE.replace(str));
        int x=0;
        for(int iF = 0; iF < str.length(); ++iF) {
            String s = String.valueOf(str.toCharArray()[iF]);
            if (s.contains("§") && iF + 1 <= str.length()) {
                iF++;
            } else if (isChinese(s.charAt(0))) {
                x += (float)font.getStringWidth(s);
            } else {
                x += (float)Fonts.font35.getStringWidth(s);
            }
        }
        return x+5;
    }

    public static void DisplayFont(CFontRenderer font, String str, float x, float y, int color) {
        DisplayFont(str, x, y, color, font);
    }

    public static void DisplayFonts(CFontRenderer font, String str, float x, float y, int color) {
        DisplayFont(str, x, y, color, font);
    }

    public float DisplayFont2(CFontRenderer font,String str, float x, float y, int color,boolean shadow) {
        if(shadow)
            return DisplayFont(str,x,y,color, true,font);
        else{
            return DisplayFont(str,x,y,color,font);
        }
    }
    public static float DisplayFont(String str, float x, float y, int color,boolean shadow, CFontRenderer font) {
        str = LanguageManager.INSTANCE.get(LanguageManager.INSTANCE.replace(str));
        str=" "+str;
        //ClientUtils.INSTANCE.displayAlert(str);
        for(int iF = 0; iF < str.length(); ++iF) {
            String s = String.valueOf(str.toCharArray()[iF]);
            if (s.contains("§") && iF + 1 <= str.length()) {
                color = getColor(String.valueOf(str.toCharArray()[iF + 1]));
                iF++;
            } else if (isChinese(s.charAt(0))) {
                font.drawString(s, x+0.5f, y+1.5f, new Color(0,0,0,100).getRGB());
                font.drawString(s, x-0.5f, y+0.5f, color);
                x += (float)font.getStringWidth(s);
            } else {
                Fonts.font35.drawString(s, x+1.5f, y+2, new Color(0,0,0,50).getRGB());
                Fonts.font35.drawString(s, x+0.5f, y+1, color);
                x += (float)Fonts.font35.getStringWidth(s);
            }
        }
        return x;
        //return font.drawString(str, x, y, color);
    }

    public static float DisplayFont(String str, float x, float y, int color, CFontRenderer font) {
        str = LanguageManager.INSTANCE.get(LanguageManager.INSTANCE.replace(str));
        str=" "+str;
        for(int iF = 0; iF < str.length(); ++iF) {
            String s = String.valueOf(str.toCharArray()[iF]);
            if (s.contains("§") && iF + 1 <= str.length()) {
                color = getColor(String.valueOf(str.toCharArray()[iF + 1]));
                iF++;
            } else if (isChinese(s.charAt(0))) {
                font.drawString(s, x-0.5f, y+1, color);
                x += (float)font.getStringWidth(s);
            } else{
                Fonts.font35.drawString(s, x+0.5f, y+1, color);
                x += (float)Fonts.font35.getStringWidth(s);
            }
        }
        return x;
    }

    public float DisplayFonts(String str, float x, float y, int color, CFontRenderer font) {
        str = LanguageManager.INSTANCE.get(LanguageManager.INSTANCE.replace(str));
        str=" "+str;
        for(int iF = 0; iF < str.length(); ++iF) {
            String s = String.valueOf(str.toCharArray()[iF]);
            if (s.contains("§") && iF + 1 <= str.length()) {
                color = getColor(String.valueOf(str.toCharArray()[iF + 1]));
                iF++;
            } else if (isChinese(s.charAt(0))) {
                font.drawString(s, x-0.5f, y+1, color);
                x += (float)font.getStringWidth(s);
            } else{
                Fonts.font35.drawString(s, x+0.5f, y+1, color);
                x += (float)Fonts.font35.getStringWidth(s);
            }
        }
        return x;
    }

    public static int getColor(String str) {
        switch(str.hashCode()) {
            case 48:
                if (str.equals("0")) {
                    return (new Color(0, 0, 0)).getRGB();
                }
                break;
            case 49:
                if (str.equals("1")) {
                    return (new Color(0, 0, 189)).getRGB();
                }
                break;
            case 50:
                if (str.equals("2")) {
                    return (new Color(0, 192, 0)).getRGB();
                }
                break;
            case 51:
                if (str.equals("3")) {
                    return (new Color(0, 190, 190)).getRGB();
                }
                break;
            case 52:
                if (str.equals("4")) {
                    return (new Color(190, 0, 0)).getRGB();
                }
                break;
            case 53:
                if (str.equals("5")) {
                    return (new Color(189, 0, 188)).getRGB();
                }
                break;
            case 54:
                if (str.equals("6")) {
                    return (new Color(218, 163, 47)).getRGB();
                }
                break;
            case 55:
                if (str.equals("7")) {
                    return (new Color(190, 190, 190)).getRGB();
                }
                break;
            case 56:
                if (str.equals("8")) {
                    return (new Color(63, 63, 63)).getRGB();
                }
                break;
            case 57:
                if (str.equals("9")) {
                    return (new Color(63, 64, 253)).getRGB();
                }
                break;
            case 97:
                if (str.equals("a")) {
                    return (new Color(63, 254, 63)).getRGB();
                }
                break;
            case 98:
                if (str.equals("b")) {
                    return (new Color(62, 255, 254)).getRGB();
                }
                break;
            case 99:
                if (str.equals("c")) {
                    return (new Color(254, 61, 62)).getRGB();
                }
                break;
            case 100:
                if (str.equals("d")) {
                    return (new Color(255, 64, 255)).getRGB();
                }
                break;
            case 101:
                if (str.equals("e")) {
                    return (new Color(254, 254, 62)).getRGB();
                }
                break;
            case 102:
                if (str.equals("f")) {
                    return (new Color(255, 255, 255)).getRGB();
                }
        }

        return (new Color(255, 255, 255)).getRGB();
    }
    /* JADX WARN: Type inference failed for: r0v21, types: [double] */
    /* JADX WARN: Type inference failed for: r0v55, types: [double] */

    public float drawString(String text, float x, float y, int color, boolean shadow) {
        return drawString(text, (double) x, (double) y,color,shadow);
    }
    public float drawString(String text, double x, double y, int color, boolean shadow) {
        GlStateManager.enableBlend();
        GlStateManager.disableBlend();
        double x2 = x - 1.0d;
        if (text == null) {
            return 0.0f;
        }
        if (color == 553648127) {
            color = 16777215;
        }
        if ((color & -67108864) == 0) {
            color |= -16777216;
        }
        if (shadow) {
            color = new Color(0, 0, 0).getRGB();
        }
        CFont.CharData[] currentData = this.charData;
        float alpha = ((float) ((color >> 24) & 255)) / 255.0f;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        char c = (char) (x2 * 2.0d);
        double y2 = (y - 3.0d) * 2.0d;
        GL11.glPushMatrix();
        GlStateManager.scale(0.5d, 0.5d, 0.5d);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, alpha);
        int size = text.length();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.tex.getGlTextureId());
        GL11.glBindTexture(3553, this.tex.getGlTextureId());
        int i = 0;
        while (i < size) {
            char character = text.charAt(i);
            if (character == '\u00a7') {
                int colorIndex = 21;
                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                    currentData = this.charData;
                    if (colorIndex < 0) {
                        colorIndex = 15;
                    }
                    if (shadow) {
                        colorIndex += 16;
                    }
                    int colorcode = this.colorCode[colorIndex];
                    GlStateManager.color(((float) ((colorcode >> 16) & 255)) / 255.0f, ((float) ((colorcode >> 8) & 255)) / 255.0f, ((float) (colorcode & 255)) / 255.0f, alpha);
                } else if (colorIndex != 16) {
                    if (colorIndex == 17) {
                        bold = true;
                        if (italic) {
                            GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                            currentData = this.boldItalicChars;
                        } else {
                            GlStateManager.bindTexture(this.texBold.getGlTextureId());
                            currentData = this.boldChars;
                        }
                    } else if (colorIndex == 18) {
                        strikethrough = true;
                    } else if (colorIndex == 19) {
                        underline = true;
                    } else if (colorIndex == 20) {
                        italic = true;
                        if (bold) {
                            GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                            currentData = this.boldItalicChars;
                        } else {
                            GlStateManager.bindTexture(this.texItalic.getGlTextureId());
                            currentData = this.italicChars;
                        }
                    } else {
                        bold = false;
                        italic = false;
                        underline = false;
                        strikethrough = false;
                        GlStateManager.color(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, alpha);
                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                    }
                }
                i++;
            } else if (character < currentData.length) {
                GL11.glBegin(4);
                drawChar(currentData, character, (float) c, (float) y2);
                GL11.glEnd();
                if (strikethrough) {
                    drawLine(c, y2 + ((double) (currentData[character].height / 2)), (c + ((double) currentData[character].width)) - 8.0d, y2 + ((double) (currentData[character].height / 2)));
                }
                if (underline) {
                    drawLine(c, (y2 + ((double) currentData[character].height)) - 2.0d, (c + ((double) currentData[character].width)) - 8.0d, (y2 + ((double) currentData[character].height)) - 2.0d);
                }
                c += (double) ((currentData[character].width - 8) + this.charOffset);
            }
            i++;
        }
        GL11.glHint(3155, 4352);
        GL11.glPopMatrix();
        return ((float) c) / 2.0f;
    }
    public int drawStringi(String text, double x, double y, int color, boolean shadow) {
        GlStateManager.enableBlend();
        GlStateManager.disableBlend();
        double x2 = x - 1.0d;
        if (text == null) {
            return 0;
        }
        if (color == 553648127) {
            color = 16777215;
        }
        if ((color & -67108864) == 0) {
            color |= -16777216;
        }
        if (shadow) {
            color = new Color(0, 0, 0).getRGB();
        }
        CFont.CharData[] currentData = this.charData;
        float alpha = ((float) ((color >> 24) & 255)) / 255.0f;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        char c = (char) (x2 * 2.0d);
        double y2 = (y - 3.0d) * 2.0d;
        GL11.glPushMatrix();
        GlStateManager.scale(0.5d, 0.5d, 0.5d);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, alpha);
        int size = text.length();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.tex.getGlTextureId());
        GL11.glBindTexture(3553, this.tex.getGlTextureId());
        int i = 0;
        while (i < size) {
            char character = text.charAt(i);
            if (character == '\u00a7') {
                int colorIndex = 21;
                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                    currentData = this.charData;
                    if (colorIndex < 0) {
                        colorIndex = 15;
                    }
                    if (shadow) {
                        colorIndex += 16;
                    }
                    int colorcode = this.colorCode[colorIndex];
                    GlStateManager.color(((float) ((colorcode >> 16) & 255)) / 255.0f, ((float) ((colorcode >> 8) & 255)) / 255.0f, ((float) (colorcode & 255)) / 255.0f, alpha);
                } else if (colorIndex != 16) {
                    if (colorIndex == 17) {
                        bold = true;
                        if (italic) {
                            GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                            currentData = this.boldItalicChars;
                        } else {
                            GlStateManager.bindTexture(this.texBold.getGlTextureId());
                            currentData = this.boldChars;
                        }
                    } else if (colorIndex == 18) {
                        strikethrough = true;
                    } else if (colorIndex == 19) {
                        underline = true;
                    } else if (colorIndex == 20) {
                        italic = true;
                        if (bold) {
                            GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                            currentData = this.boldItalicChars;
                        } else {
                            GlStateManager.bindTexture(this.texItalic.getGlTextureId());
                            currentData = this.italicChars;
                        }
                    } else {
                        bold = false;
                        italic = false;
                        underline = false;
                        strikethrough = false;
                        GlStateManager.color(((float) ((color >> 16) & 255)) / 255.0f, ((float) ((color >> 8) & 255)) / 255.0f, ((float) (color & 255)) / 255.0f, alpha);
                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                    }
                }
                i++;
            } else if (character < currentData.length) {
                GL11.glBegin(4);
                drawChar(currentData, character, (float) c, (float) y2);
                GL11.glEnd();
                if (strikethrough) {
                    drawLine(c, y2 + ((double) (currentData[character].height / 2)), (c + ((double) currentData[character].width)) - 8.0d, y2 + ((double) (currentData[character].height / 2)));
                }
                if (underline) {
                    drawLine(c, (y2 + ((double) currentData[character].height)) - 2.0d, (c + ((double) currentData[character].width)) - 8.0d, (y2 + ((double) currentData[character].height)) - 2.0d);
                }
                c += (double) ((currentData[character].width - 8) + this.charOffset);
            }
            i++;
        }
        GL11.glHint(3155, 4352);
        GL11.glPopMatrix();
        return ((int) c) / 2;
    }

    @Override // net.ccbluex.liquidbounce.CFont
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        CFont.CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();
        int i = 0;
        while (i < size) {
            char character = text.charAt(i);
            if (character == '\u00a7') {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                bold = false;
                italic = false;
                i++;
            } else if (character < currentData.length) {
                width += (currentData[character].width - 8) + this.charOffset;
            }
            i++;
        }
        return width / 2;
    }

    @Override // net.ccbluex.liquidbounce.CFont
    public void setFont(Font font) {
        setFont(font);
        setupBoldItalicIDs();
    }

    @Override // net.ccbluex.liquidbounce.CFont
    public void setAntiAlias(boolean antiAlias) {
        setAntiAlias(antiAlias);
        setupBoldItalicIDs();
    }

    @Override // net.ccbluex.liquidbounce.CFont
    public void setFractionalMetrics(boolean fractionalMetrics) {
        setFractionalMetrics(fractionalMetrics);
        setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        this.texBold = setupTexture(this.font.deriveFont(Font.BOLD), this.antiAlias, this.fractionalMetrics, this.boldChars);
        this.texItalic = setupTexture(this.font.deriveFont(Font.ITALIC), this.antiAlias, this.fractionalMetrics, this.italicChars);
    }

    private void drawLine(double x, double y, double x1, double y1) {
        GL11.glDisable(3553);
        GL11.glLineWidth((float) 1.0);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public List<String> wrapWords(String text, double width) {
        ArrayList<String> finalWords = new ArrayList<>();
        if (((double) getStringWidth(text)) > width) {
            String[] words = text.split(" ");
            String currentWord = "";
            char c = '\uffff';
            for (String word : words) {
                for (int i = 0; i < word.toCharArray().length; i++) {
                    if (word.toCharArray()[i] == '\u00a7' && i < word.toCharArray().length - 1) {
                        c = word.toCharArray()[i + 1];
                    }
                }
                if (((double) getStringWidth(currentWord + word + " ")) < width) {
                    currentWord = currentWord + word + " ";
                } else {
                    finalWords.add(currentWord);
                    currentWord = '\u00a7' + c + word + " ";
                }
            }
            if (currentWord.length() > 0) {
                if (((double) getStringWidth(currentWord)) < width) {
                    finalWords.add('\u00a7' + c + currentWord + " ");
                } else {
                    finalWords.addAll(formatString(currentWord, width));
                }
            }
        } else {
            finalWords.add(text);
        }
        return finalWords;
    }

    public List<String> formatString(String string, double width) {
        ArrayList<String> finalWords = new ArrayList<>();
        String currentWord = "";
        int lastColorCode = 65535;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\u00a7' && i < chars.length - 1) {
                lastColorCode = chars[i + 1];
            }
            if (((double) getStringWidth(currentWord + c)) < width) {
                currentWord = currentWord + c;
            } else {
                finalWords.add(currentWord);
                currentWord = String.valueOf(167 + lastColorCode) + c;
            }
        }
        if (currentWord.length() > 0) {
            finalWords.add(currentWord);
        }
        return finalWords;
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; index++) {
            int noClue = ((index >> 3) & 1) * 85;
            int red = (((index >> 2) & 1) * Opcodes.TABLESWITCH) + noClue;
            int green = (((index >> 1) & 1) * Opcodes.TABLESWITCH) + noClue;
            int blue = (((index) & 1) * Opcodes.TABLESWITCH) + noClue;
            if (index == 6) {
                red += 85;
            }
            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[index] = ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
        }
    }
}
