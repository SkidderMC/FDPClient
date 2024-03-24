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

public abstract class FontLoaders {

    public static final CFontRenderer F18 = new CFontRenderer(FontLoaders.getFont(18), true, true);
    public static final CFontRenderer F30 = new CFontRenderer(FontLoaders.getFont(30), true, true);
    public static final CFontRenderer F40 = new CFontRenderer(FontLoaders.getFont(40), true, true);
    public static final CFontRenderer C12 = new CFontRenderer(FontLoaders.getComfortaa(12), true, true);
    public static final CFontRenderer C16 = new CFontRenderer(FontLoaders.getComfortaa(16), true, true);
    public static final CFontRenderer C18 = new CFontRenderer(FontLoaders.getComfortaa(18), true, true);
    public static final CFontRenderer R12 = new CFontRenderer(FontLoaders.getRoboto(), true, true);
    public static final CFontRenderer R15 = new CFontRenderer(FontLoaders.getRoboto(), true, true);
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

    private static Font getRoboto() {
        Font font;
        try {
            InputStream is = MinecraftInstance.mc.getResourceManager()
                    .getResource(new ResourceLocation("fdpclient/font/roboto-regular.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(Font.PLAIN, 12);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, 12);
        }
        return font;
    }

}