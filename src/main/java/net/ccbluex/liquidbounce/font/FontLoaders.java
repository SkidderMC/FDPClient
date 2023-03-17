/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.font;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
public abstract class FontLoaders {
    public static final CFontRenderer F14 = new CFontRenderer(getFont(14), true, true);
    public static CFontRenderer F16 = new CFontRenderer(getFont(16), true, true);
    public static final CFontRenderer F18 = new CFontRenderer(getFont(18), true, true);
    public static final CFontRenderer F20 = new CFontRenderer(getFont(20), true, true);
    public static final CFontRenderer F22 = new CFontRenderer(getFont(22), true, true);
    public static CFontRenderer F23 = new CFontRenderer(getFont(23), true, true);
    public static CFontRenderer F24 = new CFontRenderer(getFont(24), true, true);
    public static final CFontRenderer F30 = new CFontRenderer(getFont(30), true, true);
    public static final CFontRenderer F40 = new CFontRenderer(getFont(40), true, true);
    public static CFontRenderer F50 = new CFontRenderer(getFont(50), true, true);
    public static final CFontRenderer C12 = new CFontRenderer(getComfortaa(12), true, true);
    public static final CFontRenderer C14 = new CFontRenderer(getComfortaa(14), true, true);
    public static final CFontRenderer C16 = new CFontRenderer(getComfortaa(16), true, true);
    public static final CFontRenderer C18 = new CFontRenderer(getComfortaa(18), true, true);
    public static CFontRenderer C20 = new CFontRenderer(getComfortaa(20), true, true);
    public static CFontRenderer C22 = new CFontRenderer(getComfortaa(22), true, true);
    public static CFontRenderer M12 = new CFontRenderer(getMojangles(12), true, true);
    public static final CFontRenderer M16 = new CFontRenderer(getMojangles(16), true, true);
    public static CFontRenderer M20 = new CFontRenderer(getMojangles(20), true, true);
    public static CFontRenderer M30 = new CFontRenderer(getMojangles(30), true, true);
    public static CFontRenderer M35 = new CFontRenderer(getMojangles(35), true, true);
    public static CFontRenderer M40 = new CFontRenderer(getMojangles(40), true, true);
    public static CFontRenderer SF15 = new CFontRenderer(getSF(15), true, true);
    public static CFontRenderer SF17 = new CFontRenderer(getSF(17), true, true);
    public static CFontRenderer SF20 = new CFontRenderer(getSF(20), true, true);
    public static CFontRenderer SF25 = new CFontRenderer(getSF(25), true, true);
    public static CFontRenderer SF30 = new CFontRenderer(getSF(30), true, true);
    public static CFontRenderer SF35 = new CFontRenderer(getSF(35), true, true);
    public static CFontRenderer SF40 = new CFontRenderer(getSF(40), true, true);
    public static CFontRenderer SF45 = new CFontRenderer(getSF(45), true, true);
    public static CFontRenderer SF50 = new CFontRenderer(getSF(50), true, true);
    public static final CFontRenderer JELLO20 = new CFontRenderer(getJELLO(20), true, true);
    public static final CFontRenderer JELLO30 = new CFontRenderer(getJELLO(30), true, true);
    public static CFontRenderer Logo = new CFontRenderer(getNovo(40), true, true);
    public static final ArrayList<CFontRenderer> fonts = new ArrayList<>();

    public static CFontRenderer getFontRender(int size) {
        return fonts.get(size - 10);
    }

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

    public static Font getComfortaa(int size) {
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

    public static Font getNovo(int size) {
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

        public static Font getMojangles(int size) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("fdpclient/font/mojangles.ttf")).getInputStream()).deriveFont(Font.PLAIN, (float) size);
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
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("fdpclient/font/SF.ttf")).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
     }
        public static Font getJELLO(int size) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("fdpclient/font/jello.ttf")).getInputStream()).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
     }
}
