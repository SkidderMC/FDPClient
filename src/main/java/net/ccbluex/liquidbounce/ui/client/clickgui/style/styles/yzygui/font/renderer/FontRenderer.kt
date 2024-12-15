/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.CustomFont;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author opZywl - Font Renderer
 */
public final class FontRenderer extends CustomFont {

    private final CharData[] boldItalicChars = new CharData[256];
    private final CharData[] italicChars = new CustomFont.CharData[256];
    private final CharData[] boldChars = new CharData[256];

    private final int[] colorCode = new int[32];
    private final char COLOR_CODE_START = '§';
    private final float[] charWidthFloat = new float[256];
    private final byte[] glyphWidth = new byte[65536];
    private DynamicTexture texBold;
    private DynamicTexture texItalic;
    private DynamicTexture texItalicBold;
    private boolean unicodeFlag;

    /**
     * @param resourceLocation A resource location for the font.
     * @param size             The font size
     */
    public FontRenderer(ResourceLocation resourceLocation, float size) {
        super(resourceLocation, size);

        this.setupMinecraftColorCodes();
        this.setupBoldItalicIDs();
    }

    public float drawStringWithShadow(String text, double x, double y, int color, int shadowColor) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, shadowColor, false);

        return Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawStringWithShadow(String text, double x, double y, Color color, Color shadowColor) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, shadowColor.getRGB(), false);

        return Math.max(shadowWidth, drawString(text, x, y, color.getRGB(), false));
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, color, true);

        return Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawStringWithShadow(String text, double x, double y, Color color) {
        return drawStringWithShadow(text, x, y, color.getRGB());
    }

    public float drawString(String text, float x, float y, int color) {
        return drawString(text, x, y, color, false);
    }

    public float drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, x - (float) getWidth(text) / 2, y - getHeight() / 2, color);
    }

    public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x - (double) getWidth(text) / 2 + 0.55D, y - getHeight() / 2 + 0.55D, color, true);
        return drawString(text, x - (float) getWidth(text) / 2, y - getHeight() / 2, color);
    }

    public int getCharWidth(char character) {
        return Math.round(this.getCharWidthFloat(character));
    }

    private float getCharWidthFloat(char p_getCharWidthFloat_1_) {
        if (p_getCharWidthFloat_1_ == 167) {
            return -1.0F;
        } else if (p_getCharWidthFloat_1_ != 32 && p_getCharWidthFloat_1_ != 160) {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(p_getCharWidthFloat_1_);

            if (p_getCharWidthFloat_1_ > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidthFloat[i];
            } else if (this.glyphWidth[p_getCharWidthFloat_1_] != 0) {
                int j = this.glyphWidth[p_getCharWidthFloat_1_] >>> 4;
                int k = this.glyphWidth[p_getCharWidthFloat_1_] & 15;

                if (k > 7) {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (float) ((k - j) / 2 + 1);
            } else {
                return 0.0F;
            }
        } else {
            return this.charWidthFloat[32];
        }
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidthFloat(c0);

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;

                if (flag1) {
                    ++f;
                }
            }

            if (f > (float) width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        x -= 1;
        y -= 0.5D;

        if (text == null) {
            return 0.0F;
        }

        if (color == 553648127) {
            color = 16777215;
        }

        if ((color & 0xFC000000) == 0) {
            color |= -16777216;
        }

        if (shadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }

        CharData[] currentData = this.charData;

        float alpha = (color >> 24 & 0xFF) / 255.0F;

        boolean bold = false;
        boolean italic = false;
        boolean strike = false;
        boolean underline = false;
        boolean render = true;

        x *= 2.0D;
        y = (y - 5.0D) * 2.0D;

        GL11.glPushMatrix();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glScaled(0.5D, 0.5D, 0.5D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(770, 771);

        GL11.glColor4f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, alpha);

        int size = text.length();

        GL11.glEnable(3553);

        GL11.glBindTexture(3553, tex.getGlTextureId());

        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);

            if (character == COLOR_CODE_START) {
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
                    strike = false;

                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getGlTextureId());

                    currentData = this.charData;

                    if (colorIndex < 0) {
                        colorIndex = 15;
                    }

                    if (shadow) {
                        colorIndex += 16;
                    }

                    int cc = this.colorCode[colorIndex];
                    GL11.glColor4f((cc >> 16 & 0xFF) / 255.0F, (cc >> 8 & 0xFF) / 255.0F,
                            (cc & 0xFF) / 255.0F, alpha);
                } else if (colorIndex == 16) {
                } else if (colorIndex == 17) {
                    bold = true;

                    if (italic) {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalicBold.getGlTextureId());
                        currentData = this.boldItalicChars;
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texBold.getGlTextureId());
                        currentData = this.boldChars;
                    }
                } else if (colorIndex == 18) {
                    strike = true;
                } else if (colorIndex == 19) {
                    underline = true;
                } else if (colorIndex == 20) {
                    italic = true;

                    if (bold) {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalicBold.getGlTextureId());
                        currentData = this.boldItalicChars;
                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texItalic.getGlTextureId());
                        currentData = this.italicChars;
                    }
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strike = false;
                    GL11.glColor4f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F,
                            (color & 0xFF) / 255.0F, alpha);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex.getGlTextureId());
                    currentData = this.charData;
                }

                i++;
            } else if (character < currentData.length) {
                GL11.glBegin(GL11.GL_TRIANGLES);
                drawChar(currentData, character, (float) x, (float) y + 6F);
                GL11.glEnd();

                if (strike) {
                    drawLine(x, y + (double) currentData[character].height / 2, x + currentData[character].width - 8.0D,
                            y + (double) currentData[character].height / 2, 1.0F);
                }

                if (underline) {
                    drawLine(x, y + currentData[character].height - 2.0D, x + currentData[character].width - 8.0D,
                            y + currentData[character].height - 2.0D, 1.0F);
                }

                x += currentData[character].width - 8 + this.charOffset;
            }
        }

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glPopMatrix();

        return (float) x / 2.0F;
    }

    public int getWidth(String text) {
        if (text == null) {
            return 0;
        }

        int width = 0;
        CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();

        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);

            if (character == COLOR_CODE_START) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);

                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;

                    if (italic) {
                        currentData = this.boldItalicChars;
                    } else {
                        currentData = this.boldChars;
                    }
                } else if (colorIndex == 20) {
                    italic = true;

                    if (bold) {
                        currentData = this.boldItalicChars;
                    } else {
                        currentData = this.italicChars;
                    }
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }

                i++;
            } else if (character < currentData.length) {
                width += currentData[character].width - 8 + this.charOffset;
            }
        }

        return width / 2;
    }

    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        setupBoldItalicIDs();
    }

    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        texBold = setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars);
        texItalic = setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars);
        texItalicBold = setupTexture(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics,
                this.boldItalicChars);
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public List<String> wrapWords(String text, double width) {
        List<String> finalWords = new ArrayList<>();

        if (getWidth(text) > width) {
            String[] words = text.split(" ");
            StringBuilder currentWord = new StringBuilder();
            char lastColorCode = 65535;

            for (String word : words) {
                for (int i = 0; i < word.length(); i++) {
                    char c = word.toCharArray()[i];

                    if ((c == COLOR_CODE_START) && (i < word.length() - 1)) {
                        lastColorCode = word.toCharArray()[(i + 1)];
                    }
                }

                if (getWidth(currentWord + word + " ") < width) {
                    currentWord.append(word).append(" ");
                } else {
                    finalWords.add(currentWord.toString());
                    currentWord = new StringBuilder(COLOR_CODE_START + lastColorCode + word + " ");
                }
            }

            if (currentWord.length() > 0) {
                if (getWidth(currentWord.toString()) < width) {
                    finalWords.add(COLOR_CODE_START + lastColorCode + currentWord.toString() + " ");
                    currentWord = new StringBuilder();
                } else {
                    for (String s : formatString(currentWord.toString(), width)) {
                        finalWords.add(s);
                    }
                }
            }
        } else {
            finalWords.add(text);
        }

        return finalWords;
    }

    public List<String> formatString(String string, double width) {
        List<String> finalWords = new ArrayList<>();
        String currentWord = "";
        char lastColorCode = 65535;
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if ((c == COLOR_CODE_START) && (i < chars.length - 1)) {
                lastColorCode = chars[(i + 1)];
            }

            if (getWidth(currentWord + c) < width) {
                currentWord = currentWord + c;
            } else {
                finalWords.add(currentWord);
                currentWord = COLOR_CODE_START + lastColorCode + String.valueOf(c);
            }
        }

        if (currentWord.length() > 0) {
            finalWords.add(currentWord);
        }

        return finalWords;
    }

    private void setupMinecraftColorCodes() {
        for (int index = 0; index < 32; index++) {
            int alpha = (index >> 3 & 0x1) * 85;
            int red = (index >> 2 & 0x1) * 170 + alpha;
            int green = (index >> 1 & 0x1) * 170 + alpha;
            int blue = (index & 0x1) * 170 + alpha;

            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.colorCode[index] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
        }
    }

}
