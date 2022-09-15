/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font;

import com.google.gson.*;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.ui.font.renderer.TTFFontRenderer;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Fonts {

    @FontDetails(fontName = "Light", fontSize = 32, fileName = "regular.ttf")
    public static GameFontRenderer font32;
        @FontDetails(fontName = "superLight", fontSize = 28, fileName = "regular.ttf")
    public static GameFontRenderer font28;

    public static TTFFontRenderer fontVerdana;

    @FontDetails(fontName = "Roboto Medium", fontSize = 35)
    public static GameFontRenderer font35;

    @FontDetails(fontName = "Roboto Medium", fontSize = 40)
    public static GameFontRenderer font40;

    @FontDetails(fontName = "Roboto Medium", fontSize = 72)
    public static GameFontRenderer font72;

    @FontDetails(fontName = "Roboto Medium", fontSize = 30)
    public static GameFontRenderer fontSmall;

    @FontDetails(fontName = "Roboto Medium", fontSize = 24)
    public static GameFontRenderer fontTiny;

    @FontDetails(fontName = "Roboto Medium", fontSize = 52)
    public static GameFontRenderer fontLarge;

    @FontDetails(fontName = "SF", fontSize = 35)
    public static GameFontRenderer fontSFUI35;

    @FontDetails(fontName = "SF", fontSize = 40)
    public static GameFontRenderer fontSFUI40;

    @FontDetails(fontName = "Roboto Bold", fontSize = 180)
    public static GameFontRenderer fontBold180;

    @FontDetails(fontName = "Tahoma", fontSize = 35)
    public static GameFontRenderer fontTahoma;

    @FontDetails(fontName = "Tahoma", fontSize = 30)
    public static GameFontRenderer fontTahoma30;

    public static TTFFontRenderer fontTahomaSmall;

    @FontDetails(fontName = "Bangers", fontSize = 45)
    public static GameFontRenderer fontBangers;

    @FontDetails(fontName = "jello40", fontSize = 40)
    public static GameFontRenderer fontJello40;

    @FontDetails(fontName = "Jello30", fontSize = 30)
    public static GameFontRenderer fontJello30;

    @FontDetails(fontName = "Minecraft Font")
    public static final FontRenderer minecraftFont = Minecraft.getMinecraft().fontRendererObj;

    private static final List<GameFontRenderer> CUSTOM_FONT_RENDERERS = new ArrayList<>();

    public static void loadFonts() {
        long l = System.currentTimeMillis();

        ClientUtils.INSTANCE.logInfo("Loading Fonts.");

        font35 = new GameFontRenderer(getFont("Roboto-Medium.ttf", 35));
        font40 = new GameFontRenderer(getFont("Roboto-Medium.ttf", 40));
        font72 = new GameFontRenderer(getFont("Roboto-Medium.ttf", 72));
        fontSmall = new GameFontRenderer(getFont("Roboto-Medium.ttf", 30));
        fontTiny = new GameFontRenderer(getFont("Roboto-Medium.ttf", 24));
        fontLarge = new GameFontRenderer(getFont("Roboto-Medium.ttf", 60));
        fontSFUI35 = new GameFontRenderer(getFont("sf.ttf", 35));
        fontSFUI40 = new GameFontRenderer(getFont("sf.ttf", 40));
        fontBold180 = new GameFontRenderer(getFont("Roboto-Bold.ttf", 180));
        fontTahomaSmall = new TTFFontRenderer(getFont("Tahoma.ttf", 11));
        fontTahoma = new GameFontRenderer(getFont("Tahoma.ttf", 35));
        fontTahoma30 = new GameFontRenderer(getFont("Tahoma.ttf", 30));
        fontBangers = new GameFontRenderer(getFont("Bangers.ttf", 45));
        fontJello30 = new GameFontRenderer(getFont("jello.ttf", 30));
        fontJello40 = new GameFontRenderer(getFont("jello.ttf", 40));

        for(GameFontRenderer it : getCustomFonts()) {
        }

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

            final File fontsFile = new File(LiquidBounce.fileManager.getFontsDir(), "fonts.json");

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
            initSingleFont("regular.ttf","assets/minecraft/fdpclient/font/regular.ttf");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void initSingleFont(String name, String resourcePath) throws IOException {
        File file=new File(LiquidBounce.fileManager.getFontsDir(), name);
        if(!file.exists())
            FileUtils.INSTANCE.unpackFile(file, resourcePath);
    }

    public static FontRenderer getFontRenderer(final String name, final int size) {
        if(name.equals("Minecraft")){
            return minecraftFont;
        }

        for (final FontRenderer fontRenderer : getFonts()) {
            if(fontRenderer instanceof GameFontRenderer){
                GameFontRenderer liquidFontRenderer=(GameFontRenderer) fontRenderer;
                final Font font = liquidFontRenderer.getDefaultFont().getFont();

                if(font.getName().equals(name) && font.getSize() == size)
                    return liquidFontRenderer;
            }
        }

        return minecraftFont;
    }

    public static Object[] getFontDetails(final FontRenderer fontRenderer) {
        if (fontRenderer instanceof GameFontRenderer) {
            final Font font = ((GameFontRenderer) fontRenderer).getDefaultFont().getFont();
            return new Object[] {font.getName(), font.getSize()};
        }

        return new Object[] {"Minecraft", -1};
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

    public static List<GameFontRenderer> getCustomFonts() {
        final List<GameFontRenderer> fonts = new ArrayList<>();

        for(final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if(fontObj instanceof GameFontRenderer) fonts.add((GameFontRenderer) fontObj);
            }catch(final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(Fonts.CUSTOM_FONT_RENDERERS);

        return fonts;
    }

    private static Font getFont(final String fontName, final int size) {
        try {
            final InputStream inputStream = new FileInputStream(new File(LiquidBounce.fileManager.getFontsDir(), fontName));
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