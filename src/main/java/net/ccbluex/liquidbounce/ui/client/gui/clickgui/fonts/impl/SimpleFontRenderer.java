/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.impl;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.api.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

/**
 * @author Zywl
 */
@SuppressWarnings("MagicNumber")
final class SimpleFontRenderer implements FontRenderer {

    private static final int[] COLOR_CODES = setupMinecraftColorCodes();
    private static final String COLORS = "0123456789abcdefklmnor";
    private static final char COLOR_PREFIX = '\u00a7';

    private static final short CHARS = 256;
    private static final float IMG_SIZE = 512;
    private static final float CHAR_OFFSET = 0f;

    private final CharData[] charData = new CharData[CHARS];
    private final CharData[] boldChars = new CharData[CHARS];
    private final CharData[] italicChars = new CharData[CHARS];
    private final CharData[] boldItalicChars = new CharData[CHARS];

    private final Font awtFont;
    private final boolean antiAlias;
    private final boolean fractionalMetrics;

    private DynamicTexture texturePlain;
    private DynamicTexture textureBold;
    private DynamicTexture textureItalic;
    private DynamicTexture textureItalicBold;
    private int fontHeight = -1;

    //region instantiating
    private SimpleFontRenderer(Font awtFont, boolean antiAlias, boolean fractionalMetrics) {
        this.awtFont = awtFont;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        setupBoldItalicFonts();
    }

    static FontRenderer create(Font font, boolean antiAlias) {
        return new SimpleFontRenderer(font, antiAlias, true);
    }

    public static FontRenderer create(Font font) {
        return create(font, true);
    }

    private DynamicTexture setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        return new DynamicTexture(generateFontImage(font, antiAlias, fractionalMetrics, chars));
    }

    private BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        final int imgSize = (int) IMG_SIZE;
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, imgSize, imgSize);
        graphics.setColor(Color.WHITE);

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (this.fractionalMetrics) {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        } else {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        }

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int charHeight = 0, positionX = 0, positionY = 1;

        for (int i = 0; i < chars.length; i++) {
            char ch = (char) i;
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), graphics);

            charData.width = dimensions.getBounds().width + 8;
            charData.height = dimensions.getBounds().height;

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > fontHeight) {
                this.fontHeight = charData.height;
            }

            chars[i] = charData;

            graphics.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
        }

        return bufferedImage;
    }

    private void setupBoldItalicFonts() {
        this.texturePlain = setupTexture(awtFont, antiAlias, fractionalMetrics, charData);
        this.textureBold = setupTexture(awtFont.deriveFont(Font.BOLD), antiAlias, fractionalMetrics, boldChars);
        this.textureItalic = setupTexture(awtFont.deriveFont(Font.ITALIC), antiAlias, fractionalMetrics, italicChars);
        this.textureItalicBold = setupTexture(awtFont.deriveFont(Font.BOLD | Font.ITALIC), antiAlias, fractionalMetrics, boldItalicChars);
    }
    //endregion

    @Override
    public float drawString(CharSequence text, float x, float y, int color, boolean dropShadow) {
        if (dropShadow) {
            float shadowWidth = drawStringInternal(text, x + 0.5, y + 0.5, color, true);
            return Math.max(shadowWidth, drawStringInternal(text, x, y, color, false));
        } else {
            return drawStringInternal(text, x, y, color, false);
        }
    }
    @SuppressWarnings("OverlyComplexMethod")
    private float drawStringInternal(CharSequence text, double x, double y, int color, boolean shadow) {
        x -= 1;

        if (text == null) return 0.0F;
        if (color == 0x20FFFFFF) color = 0xFFFFFF;
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        //endregion

        if (shadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }

        CharData[] charData = this.charData;
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        final boolean randomCase = false;

        x *= 2.0D;
        y = (y - 3.0D) * 2.0D;

        //region rendering
        GL11.glPushMatrix();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GL11.glColor4f((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
        GlStateManager.color((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(texturePlain.getGlTextureId());

        GL11.glBindTexture(GL_TEXTURE_2D, texturePlain.getGlTextureId());
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        boolean underline = false;
        boolean strikethrough = false;
        boolean italic = false;
        boolean bold = false;

        for (int i = 0, size = text.length(); i < size; i++) {
            char character = text.charAt(i);

            if (character == COLOR_PREFIX && i + 1 < size) {
                // TODO: Проверить, будет ли рисовать § без отдельного символа
                int colorIndex = COLORS.indexOf(text.charAt(i + 1));

                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(texturePlain.getGlTextureId());
                    charData = this.charData;

                    if (colorIndex < 0) colorIndex = 15;
                    if (shadow) colorIndex += 16;

                    int colorCode = COLOR_CODES[colorIndex];
                    GlStateManager.color(
                            (colorCode >> 16 & 0xFF) / 255.0F,
                            (colorCode >> 8 & 0xFF) / 255.0F,
                            (colorCode & 0xFF) / 255.0F,
                            255);
                } else if (colorIndex == 17) {
                    bold = true;

                    if (italic) {
                        GlStateManager.bindTexture(textureItalicBold.getGlTextureId());
                        charData = boldItalicChars;
                    } else {
                        GlStateManager.bindTexture(textureBold.getGlTextureId());
                        charData = boldChars;
                    }
                } else if (colorIndex == 18) {
                    strikethrough = true;
                } else if (colorIndex == 19) {
                    underline = true;
                } else if (colorIndex == 20) {
                    italic = true;

                    if (bold) {
                        GlStateManager.bindTexture(textureItalicBold.getGlTextureId());
                        charData = boldItalicChars;
                    } else {
                        GlStateManager.bindTexture(textureItalic.getGlTextureId());
                        charData = italicChars;
                    }
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;

                    GlStateManager.color(
                            (color >> 16 & 0xFF) / 255.0F,
                            (color >> 8 & 0xFF) / 255.0F,
                            (color & 0xFF) / 255.0F,
                            255);
                    GlStateManager.bindTexture(texturePlain.getGlTextureId());

                    charData = this.charData;
                }

                //noinspection AssignmentToForLoopParameter
                i++;
            } else if (character < charData.length) {
                GL11.glBegin(GL11.GL_TRIANGLES);
                drawChar(charData, character, (float) x, (float) y);
                GL11.glEnd();

                if (strikethrough) {
                    drawLine(x,
                            y + charData[character].height / 2.0F,
                            x + charData[character].width - 8.0D,
                            y + charData[character].height / 2.0F
                    );
                }

                if (underline) {
                    drawLine(x,
                            y + charData[character].height - 2.0D,
                            x + charData[character].width - 8.0D,
                            y + charData[character].height - 2.0D
                    );
                }

                x += charData[character].width - (character == ' ' ? 8 : 9);
            }
        }

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glPopMatrix();
        //endregion

        return (float) x / 2.0F;
    }



    @Override
    public String trimStringToWidth(CharSequence text, int width, boolean reverse) {
        StringBuilder builder = new StringBuilder();

        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < width; k += j) {
            char c0 = text.charAt(k);
            float f1 = stringWidth(String.valueOf(c0));

            if (flag) {
                flag = false;

                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;
                if (flag1) ++f;
            }

            if (f > width) break;

            if (reverse) {
                builder.insert(0, c0);
            } else {
                builder.append(c0);
            }
        }

        return builder.toString();
    }

    @Override
    public int stringWidth(CharSequence text) {
        if (text == null) return 0;
        int width = 0;
        CharData[] currentData = charData;
        boolean bold = false;
        boolean italic = false;

        for (int i = 0, size = text.length(); i < size; i++) {
            char character = text.charAt(i);

            if (character == COLOR_PREFIX && i + 1 < size) {
                int colorIndex = COLORS.indexOf(text.charAt(i + 1));

                if (colorIndex < 16) { // color
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) { // bold
                    bold = true;
                    if (italic) currentData = boldItalicChars;
                    else currentData = boldChars;
                } else if (colorIndex == 20) { // italic
                    italic = true;
                    if (bold) currentData = boldItalicChars;
                    else currentData = italicChars;
                } else if (colorIndex == 21) { // reset
                    bold = false;
                    italic = false;
                    currentData = charData;
                }

                //noinspection AssignmentToForLoopParameter
                i++;
            } else if (character < currentData.length) {
                width += currentData[character].width - (character == ' ' ? 8 : 9);
            }
        }

        return width / 2;
    }

    public CharData[] getCharData() {
        return charData;
    }

    //region shit
    private static int[] setupMinecraftColorCodes() {
        int[] colorCodes = new int[32];

        for (int i = 0; i < 32; i++) {
            int noClue = (i >> 3 & 0x1) * 85;
            int red = (i >> 2 & 0x1) * 170 + noClue;
            int green = (i >> 1 & 0x1) * 170 + noClue;
            int blue = (i & 0x1) * 170 + noClue;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red >>= 2; // divide by 4
                green >>= 2;
                blue >>= 2;
            }

            colorCodes[i] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }

        return colorCodes;
    }

    private static class CharData {

        private int width;
        private int height;
        private int storedX;
        private int storedY;

        private CharData() {
        }
    }

    //endregion
    //region rendering
    private static void drawChar(CharData[] chars, char c, float x, float y) {
        drawQuad(x, y, chars[c].width, chars[c].height, chars[c].storedX, chars[c].storedY, chars[c].width, chars[c].height);
    }

    private static void drawQuad(float x, float y, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight) {
        float renderSRCX = srcX / IMG_SIZE;
        float renderSRCY = srcY / IMG_SIZE;
        float renderSRCWidth = srcWidth / IMG_SIZE;
        float renderSRCHeight = srcHeight / IMG_SIZE;


        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
    }

    private static void drawLine(double x, double y, double x1, double y1) {
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glLineWidth((float) 1.0);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL_TEXTURE_2D);
    }

    @Override
    public int getHeight() {
        return (fontHeight - 8) / 2;
    }

    //endregion
}