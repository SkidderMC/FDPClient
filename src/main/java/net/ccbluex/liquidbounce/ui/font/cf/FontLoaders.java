/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
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
    public static CFontRenderer SF20 = new CFontRenderer(getSF(20), true, true);
    public static CFontRenderer SF30 = new CFontRenderer(getSF(30), true, true);
    public static CFontRenderer SF40 = new CFontRenderer(getSF(40), true, true);
    public static final ArrayList<CFontRenderer> fonts = new ArrayList<>();

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

    public static Font getSF(int size) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/sfui.ttf")).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }


}