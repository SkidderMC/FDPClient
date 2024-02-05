/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font;

import com.google.gson.*;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Fonts {


    @FontDetails(fontName = "Medium", fontSize = 40, fileName = "regular.ttf")
    public static GameFontRenderer font40;

    @FontDetails(fontName = "Small", fontSize = 35, fileName = "regular.ttf")
    public static GameFontRenderer font35;
    @FontDetails(fontName = "Light", fontSize = 32, fileName = "regular.ttf")
    public static GameFontRenderer font32;
    @FontDetails(fontName = "superLight", fontSize = 28, fileName = "regular.ttf")
    public static GameFontRenderer font28;
    @FontDetails(fontName = "Roboto Medium", fontSize = 72)
    public static GameFontRenderer font72;

    @FontDetails(fontName = "Roboto Medium", fontSize = 30)
    public static GameFontRenderer fontSmall;

    @FontDetails(fontName = "Roboto Medium", fontSize = 24)
    public static GameFontRenderer fontTiny;

    @FontDetails(fontName = "Roboto Medium", fontSize = 52)
    public static GameFontRenderer fontLarge;

    @FontDetails(fontName = "SFUI Regular", fontSize = 32)
    public static GameFontRenderer fontSFUI32;
    @FontDetails(fontName = "SFUI Regular", fontSize = 35)
    public static GameFontRenderer fontSFUI35;

    @FontDetails(fontName = "SFUI Regular", fontSize = 40)
    public static GameFontRenderer fontSFUI40;

    @FontDetails(fontName = "Tahoma Bold", fontSize = 35)
    public static GameFontRenderer fontTahoma;

    @FontDetails(fontName = "Tahoma Bold", fontSize = 30)
    public static GameFontRenderer fontTahoma30;
    @FontDetails(fontName = "mainmenu", fontSize = 60)
    public static GameFontRenderer fontMainMenu60;

    @FontDetails(fontName = "Check", fontSize = 42)
    public static GameFontRenderer fontCheck42;
    public static TTFFontRenderer fontTahomaSmall;
    @FontDetails(fontName = "ICONFONT", fontSize = 50)
    public static GameFontRenderer ICONFONT_50;

    @FontDetails(fontName = "Tenacity", fontSize = 35)
    public static GameFontRenderer fontTenacity35;

    @FontDetails(fontName = "Tenacity", fontSize = 40)
    public static GameFontRenderer fontTenacity40;

    @FontDetails(fontName = "Tenacity Bold", fontSize = 35)
    public static GameFontRenderer fontTenacityBold35;

    @FontDetails(fontName = "Tenacity Bold", fontSize = 40)
    public static GameFontRenderer fontTenacityBold40;

    @FontDetails(fontName = "SFApple24", fontSize = 24)
    public static GameFontRenderer SFApple24;
    @FontDetails(fontName = "SFApple30", fontSize = 30)
    public static GameFontRenderer SFApple30;
    @FontDetails(fontName = "SFApple35", fontSize = 35)
    public static GameFontRenderer SFApple35;
    @FontDetails(fontName = "SFApple40", fontSize = 40)
    public static GameFontRenderer SFApple40;
    @FontDetails(fontName = "SFApple50", fontSize = 50)
    public static GameFontRenderer SFApple50;


    @FontDetails(fontName = "Minecraft Font")
    public static final FontRenderer minecraftFont = Minecraft.getMinecraft().fontRendererObj;

    private static final List<GameFontRenderer> CUSTOM_FONT_RENDERERS = new ArrayList<>();

    public static void loadFonts() {
        long l = System.currentTimeMillis();

        ClientUtils.INSTANCE.logInfo("Loading Fonts.");

        font40 = new GameFontRenderer(getFont("regular.ttf", 40));
        font35 = new GameFontRenderer(getFont("regular.ttf", 35));
        font32 = new GameFontRenderer(getFont("regular.ttf", 32));
        font28 = new GameFontRenderer(getFont("regular.ttf", 28));
        fontSmall = new GameFontRenderer(getFont("Roboto-Medium.ttf", 30));
        font72 = new GameFontRenderer(getFont("Roboto-Medium.ttf", 72));
        fontTiny = new GameFontRenderer(getFont("Roboto-Medium.ttf", 24));
        fontLarge = new GameFontRenderer(getFont("Roboto-Medium.ttf", 60));
        fontSFUI32 = new GameFontRenderer(getFont("sfui.ttf", 32));
        fontSFUI35 = new GameFontRenderer(getFont("sfui.ttf", 35));
        fontSFUI40 = new GameFontRenderer(getFont("sfui.ttf", 40));
        fontTahoma = new GameFontRenderer(getFont("TahomaBold.ttf", 35));
        fontTahoma30 = new GameFontRenderer(getFont("TahomaBold.ttf", 30));
        fontTahomaSmall = new TTFFontRenderer(getFont("Tahoma.ttf", 11));
        ICONFONT_50 = new GameFontRenderer(getFont("stylesicons.ttf", 50));
        fontTenacityBold35 = new GameFontRenderer(getFont("tenacity-bold.ttf", 35));
        fontTenacityBold40 = new GameFontRenderer(getFont("tenacity-bold.ttf", 40));
        fontTenacity35 = new GameFontRenderer(getFont("tenacity.ttf", 35));
        fontTenacity40 = new GameFontRenderer(getFont("tenacity.ttf", 40));
        fontMainMenu60 = new GameFontRenderer(getFont("mainmenu.ttf", 60));
        fontCheck42 = new GameFontRenderer(getFont("check.ttf", 42));
        SFApple40 = new GameFontRenderer(getFont("SFApple", 40));
        SFApple30 = new GameFontRenderer(getFont("SFApple", 30));
        SFApple35 = new GameFontRenderer(getFont("SFApple", 35));
        SFApple50 = new GameFontRenderer(getFont("SFApple", 50));
        SFApple24 = new GameFontRenderer(getFont("SFApple", 24));

        initFonts();

        for(final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                if(fontDetails!=null) {
                    if(!fontDetails.fileName().isEmpty())
                        field.set(null,new GameFontRenderer(getFont(fontDetails.fileName(), fontDetails.fontSize())));
                }
            }catch(final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            CUSTOM_FONT_RENDERERS.clear();

            final File fontsFile = new File(FDPClient.fileManager.getFontsDir(), "fonts.json");

            if(fontsFile.exists()) {
                final JsonElement jsonElement = new JsonParser().parse(new BufferedReader(new FileReader(fontsFile)));

                if(jsonElement instanceof JsonNull)
                    return;

                final JsonArray jsonArray = (JsonArray) jsonElement;

                for(final JsonElement element : jsonArray) {
                    if(element instanceof JsonNull)
                        return;

                    final JsonObject fontObject = (JsonObject) element;

                    CUSTOM_FONT_RENDERERS.add(new GameFontRenderer(getFont(fontObject.get("fontFile").getAsString(), fontObject.get("fontSize").getAsInt())));
                }
            }else{
                fontsFile.createNewFile();

                final PrintWriter printWriter = new PrintWriter(new FileWriter(fontsFile));
                printWriter.println(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonArray()));
                printWriter.close();
            }
        }catch(final Exception e) {
            e.printStackTrace();
        }

        ClientUtils.INSTANCE.logInfo("Loaded Fonts. (" + (System.currentTimeMillis() - l) + "ms)");
    }

    private static void initFonts() {
        try {
            initSingleFont();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void initSingleFont() throws IOException {
        File file=new File(FDPClient.fileManager.getFontsDir(), "regular.ttf");
        if(!file.exists())
            FileUtils.INSTANCE.unpackFile(file, "assets/minecraft/fdpclient/font/regular.ttf");
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

                    return new Object[] {fontDetails.fontName(), fontDetails.fontSize()};
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (fontRenderer instanceof GameFontRenderer) {
            final Font font = ((GameFontRenderer) fontRenderer).getDefaultFont().getFont();

            return new Object[] {font.getName(), font.getSize()};
        }

        return null;
    }

    public static List<FontRenderer> getFonts() {
        final List<FontRenderer> fonts = new ArrayList<>();

        for(final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if(fontObj instanceof FontRenderer) fonts.add((FontRenderer) fontObj);
            }catch(final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(Fonts.CUSTOM_FONT_RENDERERS);

        return fonts;
    }

    private static Font getFont(final String fontName, final int size) {
        try {
            final InputStream inputStream = Files.newInputStream(new File(FDPClient.fileManager.getFontsDir(), fontName).toPath());
            Font awtClientFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtClientFont = awtClientFont.deriveFont(Font.PLAIN, size);
            inputStream.close();
            return awtClientFont;
        }catch(final Exception e) {
            e.printStackTrace();

            return new Font("default", Font.PLAIN, size);
        }
    }
}
