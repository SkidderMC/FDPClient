package net.ccbluex.liquidbounce.font;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;

/* loaded from: LiquidBounce-b73.jar:net/ccbluex/liquidbounce/TTFFontRender.class */
public class TTFFontRender extends TTFCFont {
    protected TTFCFont.CharData[] boldChars = new TTFCFont.CharData[256];
    protected TTFCFont.CharData[] italicChars = new TTFCFont.CharData[256];
    protected TTFCFont.CharData[] boldItalicChars = new TTFCFont.CharData[256];
    private final int[] colorCode = new int[32];
    private final String colorcodeIdentifiers = "0123456789abcdefklmnor";
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;

    public TTFFontRender(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        setupMinecraftColorcodes();
        setupBoldItalicIDs();
    }

    public float drawStringWithShadowNew(String text, double x, double y, int color) {
        return Math.max(drawString(text, x + 1.0d, y + 1.0d, color, true), drawString(text, x, y, color, false));
    }

    public float drawString(String text, float x, float y, int color) {
        return drawString(text, (double) x, (double) y, color, false);
    }

    public float drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, x - ((float) (getStringWidth(text) / 2)), y, color);
    }

    /* JADX WARN: Type inference failed for: r0v21, types: [double] */
    /* JADX WARN: Type inference failed for: r0v55, types: [double] */
    public float drawString(String text, double x, double y, int color, boolean shadow) {
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
            color = ((color & 16579836) >> 2) | (color & -16777216);
        }
        TTFCFont.CharData[] currentData = this.charData;
        float alpha = ((float) ((color >> 24) & 255)) / 255.0f;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        char c = (char) (x2 * 2.0d);
        double y2 = (y - 3.0d) * 2.0d;
        if (1 != 0) {
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
                if (character == '\u00a7' && i < size) {
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
                        if (colorIndex < 0 || colorIndex > 15) {
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
                        } else if (colorIndex == 21) {
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
                } else if (character < currentData.length && character >= 0) {
                    GL11.glBegin(4);
                    drawChar(currentData, character, (float) c, (float) y2);
                    GL11.glEnd();
                    if (strikethrough) {
                        drawLine(c, y2 + ((double) (currentData[character].height / 2)), (c + ((double) currentData[character].width)) - 8.0d, y2 + ((double) (currentData[character].height / 2)), 1.0f);
                    }
                    if (underline) {
                        drawLine(c, (y2 + ((double) currentData[character].height)) - 2.0d, (c + ((double) currentData[character].width)) - 8.0d, (y2 + ((double) currentData[character].height)) - 2.0d, 1.0f);
                    }
                    c += (double) ((currentData[character].width - 8) + this.charOffset);
                }
                i++;
            }
            GL11.glHint(3155, 4352);
            GL11.glPopMatrix();
        }
        return ((float) c) / 2.0f;
    }

    @Override // net.ccbluex.liquidbounce.TTFCFont
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        TTFCFont.CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();
        int i = 0;
        while (i < size) {
            char character = text.charAt(i);
            if (character == '\u00a7' && i < size) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;
                    currentData = italic ? this.boldItalicChars : this.boldChars;
                } else if (colorIndex == 20) {
                    italic = true;
                    currentData = bold ? this.boldItalicChars : this.italicChars;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }
                i++;
            } else if (character < currentData.length && character >= 0) {
                width += (currentData[character].width - 8) + this.charOffset;
            }
            i++;
        }
        return width / 2;
    }

    @Override // net.ccbluex.liquidbounce.TTFCFont
    public void setFont(Font font) {
        setFont(font);
        setupBoldItalicIDs();
    }

    @Override // net.ccbluex.liquidbounce.TTFCFont
    public void setAntiAlias(boolean antiAlias) {
        setAntiAlias(antiAlias);
        setupBoldItalicIDs();
    }

    @Override // net.ccbluex.liquidbounce.TTFCFont
    public void setFractionalMetrics(boolean fractionalMetrics) {
        setFractionalMetrics(fractionalMetrics);
        setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        this.texBold = setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars);
        this.texItalic = setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars);
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
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
                if (((double) getStringWidth(String.valueOf(currentWord) + word + " ")) < width) {
                    currentWord = String.valueOf(currentWord) + word + " ";
                } else {
                    finalWords.add(currentWord);
                    currentWord = String.valueOf('\u00a7' + c) + word + " ";
                }
            }
            if (currentWord.length() > 0) {
                if (((double) getStringWidth(currentWord)) < width) {
                    finalWords.add(String.valueOf('\u00a7' + c) + currentWord + " ");
                } else {
                    for (String s : formatString(currentWord, width)) {
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
        ArrayList<String> finalWords = new ArrayList<>();
        String currentWord = "";
        int lastColorCode = 65535;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\u00a7' && i < chars.length - 1) {
                lastColorCode = chars[i + 1];
            }
            if (((double) getStringWidth(String.valueOf(currentWord) + c)) < width) {
                currentWord = String.valueOf(currentWord) + c;
            } else {
                finalWords.add(currentWord);
                currentWord = String.valueOf(167 + lastColorCode) + String.valueOf(c);
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
            int blue = (((index >> 0) & 1) * Opcodes.TABLESWITCH) + noClue;
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
