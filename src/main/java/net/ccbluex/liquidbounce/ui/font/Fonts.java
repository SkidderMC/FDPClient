/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font;

import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.gui.FontRenderer;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import java.awt.Font;

public class Fonts {

    @FontDetails(fontName = "Minecraft Font")
    public static final FontRenderer minecraftFont = MinecraftInstance.mc.fontRendererObj;
    private static final List<GameFontRenderer> CUSTOM_FONT_RENDERERS = new ArrayList<>();

    // ROBOTO
    @FontDetails(fontName = "regular", fontSize = 28)
    public static GameFontRenderer font28;
    @FontDetails(fontName = "regular", fontSize = 32)
    public static GameFontRenderer font32;
    @FontDetails(fontName = "regular", fontSize = 35)
    public static GameFontRenderer font35;
    @FontDetails(fontName = "regular", fontSize = 40)
    public static GameFontRenderer font40;
    @FontDetails(fontName = "regular", fontSize = 72)
    public static GameFontRenderer font72;
    @FontDetails(fontName = "regular", fontSize = 30)
    public static GameFontRenderer fontSmall;
    @FontDetails(fontName = "regular", fontSize = 24)
    public static GameFontRenderer fontTiny;
    @FontDetails(fontName = "regular", fontSize = 52)
    public static GameFontRenderer fontLarge;

    // BOLD
    @FontDetails(fontName = "Roboto Bold", fontSize = 26)
    public static GameFontRenderer fontBold26;
    @FontDetails(fontName = "Roboto Bold", fontSize = 32)
    public static GameFontRenderer fontBold32;
    @FontDetails(fontName = "Roboto Bold", fontSize = 35)
    public static GameFontRenderer fontBold35;
    @FontDetails(fontName = "Roboto Bold", fontSize = 40)
    public static GameFontRenderer fontBold40;

    // SFUI BOLD
    @FontDetails(fontName = "SFUI Bold", fontSize = 16)
    public static GameFontRenderer fontSFUIBOLD16;
    @FontDetails(fontName = "SFUI Bold", fontSize = 18)
    public static GameFontRenderer fontSFUIBOLD18;
    @FontDetails(fontName = "SFUI Bold", fontSize = 26)
    public static GameFontRenderer fontSFUIBOLD26;

    // SFUI
    @FontDetails(fontName = "SFUI Regular", fontSize = 16)
    public static GameFontRenderer fontSFUI16;
    @FontDetails(fontName = "SFUI Regular", fontSize = 18)
    public static GameFontRenderer fontSFUI18;
    @FontDetails(fontName = "SFUI Regular", fontSize = 32)
    public static GameFontRenderer fontSFUI32;
    @FontDetails(fontName = "SFUI Regular", fontSize = 35)
    public static GameFontRenderer fontSFUI35;
    @FontDetails(fontName = "SFUI Regular", fontSize = 37)
    public static GameFontRenderer fontSFUI37;
    @FontDetails(fontName = "SFUI Regular", fontSize = 40)
    public static GameFontRenderer fontSFUI40;

    // TAHOMA
    @FontDetails(fontName = "Tahoma Bold", fontSize = 35)
    public static GameFontRenderer fontTahoma;
    @FontDetails(fontName = "Tahoma Bold", fontSize = 30)
    public static GameFontRenderer fontTahoma30;
    public static TTFFontRenderer fontTahomaSmall;

    // ICONS
    @FontDetails(fontName = "Check", fontSize = 42)
    public static GameFontRenderer fontCheck42;
    @FontDetails(fontName = "ICONFONT", fontSize = 50)
    public static GameFontRenderer ICONFONT_50;

    // SFAPPLE
    @FontDetails(fontName = "SFApple", fontSize = 40)
    public static GameFontRenderer SFApple40;
    @FontDetails(fontName = "SFApple", fontSize = 50)
    public static GameFontRenderer SFApple50;

    public static void loadFonts() {
        // ROBOTO
        font28 = new GameFontRenderer(getRobotoMedium(28));
        font32 = new GameFontRenderer(getRobotoMedium(32));
        font35 = new GameFontRenderer(getRobotoMedium(35));
        font40 = new GameFontRenderer(getRobotoMedium(40));
        font72 = new GameFontRenderer(getRobotoMedium(72));

        fontSmall = new GameFontRenderer(getRobotoMedium(30));
        fontTiny = new GameFontRenderer(getRobotoMedium(24));
        fontLarge = new GameFontRenderer(getRobotoMedium(60));

        // ROBOTO BOLD
        fontBold26 = new GameFontRenderer(getRobotoBold(26));
        fontBold32 = new GameFontRenderer(getRobotoBold(32));
        fontBold35 = new GameFontRenderer(getRobotoBold(35));
        fontBold40 = new GameFontRenderer(getRobotoBold(40));

        // SFUI
        fontSFUIBOLD16 = new GameFontRenderer(getSFUIBOLD(16));
        fontSFUIBOLD18 = new GameFontRenderer(getSFUIBOLD(18));
        fontSFUIBOLD26 = new GameFontRenderer(getSFUIBOLD(26));

        // SFUI
        fontSFUI16 = new GameFontRenderer(getSFUI(16));
        fontSFUI18 = new GameFontRenderer(getSFUI(18));
        fontSFUI32 = new GameFontRenderer(getSFUI(32));
        fontSFUI35 = new GameFontRenderer(getSFUI(35));
        fontSFUI37 = new GameFontRenderer(getSFUI(37));
        fontSFUI40 = new GameFontRenderer(getSFUI(40));

        // TAHOMA
        fontTahoma = new GameFontRenderer(getTahomaBold(35));
        fontTahoma30 = new GameFontRenderer(getTahomaBold(30));
        fontTahomaSmall = new TTFFontRenderer(getTahoma(11));

        // ICONS
        ICONFONT_50 = new GameFontRenderer(getIcons(50));
        fontCheck42 = new GameFontRenderer(getCheck(42));

        // SFAPPLE
        SFApple40 = new GameFontRenderer(getSFApple(40));
        SFApple50 = new GameFontRenderer(getSFApple(50));

        ClientUtils.getLogger().info("Loaded Fonts");
    }

    public static FontRenderer getFontRenderer(final String name, final int size) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o instanceof FontRenderer) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    if (fontDetails.fontName().equals(name) && fontDetails.fontSize() == size)
                        return (FontRenderer) o;
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (final GameFontRenderer liquidFontRenderer : CUSTOM_FONT_RENDERERS) {
            final Font font = liquidFontRenderer.getDefaultFont().getFont();

            if (font.getName().equals(name) && font.getSize() == size)
                return liquidFontRenderer;
        }

        return minecraftFont;
    }

    public static Object[] getFontDetails(final FontRenderer fontRenderer) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o.equals(fontRenderer)) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    return new Object[]{fontDetails.fontName(), fontDetails.fontSize()};
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (fontRenderer instanceof GameFontRenderer) {
            final Font font = ((GameFontRenderer) fontRenderer).getDefaultFont().getFont();

            return new Object[]{font.getName(), font.getSize()};
        }

        return null;
    }

    public static List<FontRenderer> getFonts() {
        final List<FontRenderer> fonts = new ArrayList<>();

        for (final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if (fontObj instanceof FontRenderer) fonts.add((FontRenderer) fontObj);
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(CUSTOM_FONT_RENDERERS);

        return fonts;
    }

    private static Font getSFUIBOLD(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/sfuibold.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "sfuibold.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("sfuibold", Font.PLAIN, size);
        }
    }

    private static Font getSFUI(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/sfui.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "sfui.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("sfui", Font.PLAIN, size);
        }
    }

    private static Font getRobotoMedium(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/regular.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "regular.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("regular", Font.PLAIN, size);
        }
    }

    private static Font getSFApple(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/SFApple.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "SFApple");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("SFApple", Font.PLAIN, size);
        }
    }

    private static Font getRobotoBold(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/Roboto-Bold.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "Roboto-Bold.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("Roboto-Bold", Font.PLAIN, size);
        }
    }

    private static Font getTahomaBold(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/TahomaBold.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "TahomaBold.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("TahomaBold", Font.PLAIN, size);
        }
    }

    private static Font getTahoma(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/Tahoma.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "Tahoma.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("Tahoma", Font.PLAIN, size);
        }
    }


    private static Font getIcons(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/stylesicons.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "stylesicons.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("stylesicons", Font.PLAIN, size);
        }
    }

    private static Font getCheck(final int size) {
        try {
            InputStream inputStream = Fonts.class.getResourceAsStream("/assets/minecraft/fdpclient/font/check.ttf");

            if (inputStream == null) {
                throw new FileNotFoundException("Font file not found: " + "check.ttf");
            }

            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        } catch (final Exception e) {
            e.printStackTrace();
            return new Font("check", Font.PLAIN, size);
        }
    }
}