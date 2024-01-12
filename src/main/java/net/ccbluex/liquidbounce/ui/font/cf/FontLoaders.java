package net.ccbluex.liquidbounce.ui.font.cf;

import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class FontLoaders {

    public static final CFontRenderer F14 = new CFontRenderer(getFont(14), true, true);
    public static final CFontRenderer F18 = new CFontRenderer(getFont(18), true, true);
    public static final CFontRenderer F20 = new CFontRenderer(getFont(20), true, true);
    public static final CFontRenderer F30 = new CFontRenderer(getFont(30), true, true);
    public static final CFontRenderer F40 = new CFontRenderer(getFont(40), true, true);
    public static final CFontRenderer C12 = new CFontRenderer(getComfortaa(12), true, true);
    public static final CFontRenderer C16 = new CFontRenderer(getComfortaa(16), true, true);
    public static final CFontRenderer C18 = new CFontRenderer(getComfortaa(18), true, true);
    public static final CFontRenderer M16 = new CFontRenderer(getMojangles(16), true, true);
    public static final ArrayList<CFontRenderer> fonts = new ArrayList<>();

    // Tahoma2
    public static CFontRenderer Tahoma9 = new CFontRenderer(FontLoaders.getTahoma(9), true, true);
    public static CFontRenderer Tahoma11 = new CFontRenderer(FontLoaders.getTahoma(11), true, true);
    public static CFontRenderer Tahoma13 = new CFontRenderer(FontLoaders.getTahoma(13), true, true);
    public static CFontRenderer Tahoma14 = new CFontRenderer(FontLoaders.getTahoma(14), true, true);
    public static CFontRenderer Tahoma16 = new CFontRenderer(FontLoaders.getTahoma(16), true, true);
    public static CFontRenderer Tahoma18 = new CFontRenderer(FontLoaders.getTahoma(18), true, true);
    public static CFontRenderer Tahoma17 = new CFontRenderer(FontLoaders.getTahoma(17), true, true);
    public static CFontRenderer Tahoma19 = new CFontRenderer(FontLoaders.getTahoma(19), true, true);
    public static CFontRenderer Tahoma21 = new CFontRenderer(FontLoaders.getTahoma(21), true, true);
    public static CFontRenderer Tahoma22 = new CFontRenderer(FontLoaders.getTahoma(22), true, true);
    public static CFontRenderer Tahoma24 = new CFontRenderer(FontLoaders.getTahoma(24), true, true);
    public static CFontRenderer Tahoma23 = new CFontRenderer(FontLoaders.getTahoma(23), true, true);
    public static CFontRenderer Tahoma20 = new CFontRenderer(FontLoaders.getTahoma(20), true, true);

    // Tahoma3
    public static CFontRenderer TahomaBold8 = new CFontRenderer(FontLoaders.getTahomaBold(8), true, true);
    public static CFontRenderer TahomaBold9 = new CFontRenderer(FontLoaders.getTahomaBold(9), true, true);
    public static CFontRenderer TahomaBold10 = new CFontRenderer(FontLoaders.getTahomaBold(10), true, true);
    public static CFontRenderer TahomaBold11 = new CFontRenderer(FontLoaders.getTahomaBold(11), true, true);
    public static CFontRenderer TahomaBold12 = new CFontRenderer(FontLoaders.getTahomaBold(12), true, true);
    public static CFontRenderer TahomaBold13 = new CFontRenderer(FontLoaders.getTahomaBold(13), true, true);
    public static CFontRenderer TahomaBold14 = new CFontRenderer(FontLoaders.getTahomaBold(14), true, true);
    public static CFontRenderer TahomaBold16 = new CFontRenderer(FontLoaders.getTahomaBold(16), true, true);
    public static CFontRenderer TahomaBold17 = new CFontRenderer(FontLoaders.getTahomaBold(17), true, true);
    public static CFontRenderer TahomaBold18 = new CFontRenderer(FontLoaders.getTahomaBold(18), true, true);
    public static CFontRenderer TahomaBold19 = new CFontRenderer(FontLoaders.getTahomaBold(19), true, true);
    public static CFontRenderer TahomaBold20 = new CFontRenderer(FontLoaders.getTahomaBold(20), true, true);
    public static CFontRenderer TahomaBold21 = new CFontRenderer(FontLoaders.getTahomaBold(21), true, true);
    public static CFontRenderer TahomaBold22 = new CFontRenderer(FontLoaders.getTahomaBold(22), true, true);
    public static CFontRenderer TahomaBold23 = new CFontRenderer(FontLoaders.getTahomaBold(23), true, true);
    public static CFontRenderer TahomaBold24 = new CFontRenderer(FontLoaders.getTahomaBold(24), true, true);

    public static CFontRenderer SF16 = new CFontRenderer(FontLoaders.getSF(16), true, true);
    public static CFontRenderer SF17 = new CFontRenderer(FontLoaders.getSF(17), true, true);
    public static CFontRenderer SF18 = new CFontRenderer(FontLoaders.getSF(18), true, true);
    public static CFontRenderer SF19 = new CFontRenderer(FontLoaders.getSF(19), true, true);
    public static CFontRenderer SF20 = new CFontRenderer(FontLoaders.getSF(20), true, true);
    public static CFontRenderer SF21 = new CFontRenderer(FontLoaders.getSF(21), true, true);
    public static CFontRenderer SF22 = new CFontRenderer(FontLoaders.getSF(22), true, true);
    public static CFontRenderer SF23 = new CFontRenderer(FontLoaders.getSF(23), true, true);
    public static CFontRenderer SF24 = new CFontRenderer(FontLoaders.getSF(24), true, true);

    public static Font getFont(int size) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("fdpclient/font/regular.ttf")).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    private static Font getComfortaa(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/Comfortaa.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    private static Font getMojangles(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/mojangles.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    private static Font getSF(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/sfui.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    private static Font getExhibition(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/Icons.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }


    private static Font getTahoma(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/Tahoma.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

    private static Font getTahomaBold(int size) {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/TahomaBold.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }

}