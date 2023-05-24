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
import net.minecraft.util.ResourceLocation;

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

    @FontDetails(fontName = "ICONFONT_50", fontSize = 50)
    public static GameFontRenderer ICONFONT_50;

    @FontDetails(fontName = "SF", fontSize = 40)
    public static GameFontRenderer SF;

    @FontDetails(fontName = "SFUI40", fontSize = 20)
    public static GameFontRenderer SFUI40;

    @FontDetails(fontName = "SFUI35", fontSize = 18)
    public static GameFontRenderer SFUI35;

    @FontDetails(fontName = "SFUI24", fontSize = 10)
    public static GameFontRenderer SFUI24;

    @FontDetails(fontName = "Icon",fontSize = 18)
    public static GameFontRenderer icon18;

    @FontDetails(fontName = "Icon",fontSize = 15)
    public static GameFontRenderer icon15;

    @FontDetails(fontName = "Icon",fontSize = 10)
    public static GameFontRenderer icon10;

    @FontDetails(fontName = "Minecraft Font")
    public static final FontRenderer minecraftFont = Minecraft.getMinecraft().fontRendererObj;
    
    @FontDetails(fontName = "jello40", fontSize = 40)
    public static GameFontRenderer fontJello40;

    @FontDetails(fontName = "Jello30", fontSize = 30)
    public static GameFontRenderer fontJello30;

    @FontDetails(fontName = "Tenacity35", fontSize = 35)
    public static GameFontRenderer fontTenacity35;

    @FontDetails(fontName = "TenacityBold35", fontSize = 35)
    public static GameFontRenderer fontTenacityBold35;

    @FontDetails(fontName = "tenacity40", fontSize = 40)
    public static GameFontRenderer fontTenacity40;

    @FontDetails(fontName = "tenacityBold40", fontSize = 40)
    public static GameFontRenderer fontTenacityBold40;

    @FontDetails(fontName = "TenacityIcon30", fontSize = 30)
    public static GameFontRenderer fontTenacityIcon30;

    //fontTenacity35  fontTenacityBold35

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
        fontSFUI35 = new GameFontRenderer(getFont("SF.ttf", 35));
        fontSFUI40 = new GameFontRenderer(getFont("SF.ttf", 40));
        ICONFONT_50 = new GameFontRenderer(getFont("stylesicons.ttf", 50));
        SF = new GameFontRenderer(getFont("SF.ttf", 20));
        SFUI40 = new GameFontRenderer(getFont("SF.ttf", 20));
        SFUI35 = new GameFontRenderer(getFont("SF.ttf", 18));
        SFUI24 = new GameFontRenderer(getFont("SF.ttf", 10));
        fontSFUI35 = new GameFontRenderer(getFont("SF.ttf", 35));
        fontSFUI40 = new GameFontRenderer(getFont("SF.ttf", 40));
        fontBold180 = new GameFontRenderer(getFont("Roboto-Bold.ttf", 180));
        fontTahomaSmall = new TTFFontRenderer(getFont("Tahoma.ttf", 11));
        fontVerdana = new TTFFontRenderer(getFont("Verdana.ttf", 7));
        // fonts above here may not work as this is a test
        fontBangers = new GameFontRenderer(getFontcustom(45, "Bangers"));
        icon18 = new GameFontRenderer(getFontcustom(18,"Icon"));
        icon15 = new GameFontRenderer(getFontcustom(15,"Icon"));
        icon10 = new GameFontRenderer(getFontcustom(10,"Icon"));
        fontTahoma = new GameFontRenderer(getFontcustom(35,"Tahoma"));
        fontTahoma30 = new GameFontRenderer(getFontcustom(30,"Tahoma"));
        fontJello30 = new GameFontRenderer(getFontcustom(30,"jello"));
        fontJello40 = new GameFontRenderer(getFontcustom(40,"jello"));
        fontTenacity35 = new GameFontRenderer(getFontcustom(35, "tenacity"));
        fontTenacityBold35 = new GameFontRenderer(getFontcustom(35, "tenacity-bold"));
        fontTenacityIcon30 = new GameFontRenderer(getFontcustom(30, "Tenacityicon"));
        fontTenacity40 = new GameFontRenderer(getFontcustom(40,"tenacity"));
        fontTenacityBold40 = new GameFontRenderer(getFontcustom(40,"tenacity-bold"));


        getCustomFonts();

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
            initSingleFont("regular.ttf", "assets/minecraft/fdpclient/font/regular.ttf");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static Font getFontcustom(int size,String fontname) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/"+fontname+".ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }
    private static Font getFont1(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/icon.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }
    private static Font getFont2(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/regular.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("defualt", Font.PLAIN, size);
        }
        return font;
    }
    private static Font getFont3(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/SFBOLD.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("SFBold", Font.PLAIN, size);
        }
        return font;
    }
    private static Font getFont4(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/tenacity.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("SFBold", Font.PLAIN, size);
        }
        return font;
    }
    private static Font getFont5(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/tenacity-bold.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("SFBold", Font.PLAIN, size);
        }
        return font;
    }

    private static void initSingleFont(String name, String resourcePath) throws IOException {
        File file=new File(FDPClient.fileManager.getFontsDir(), name);
        if(!file.exists())
            FileUtils.INSTANCE.unpackFile(file, resourcePath);
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
            final InputStream inputStream = new FileInputStream(new File(FDPClient.fileManager.getFontsDir(), fontName));
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