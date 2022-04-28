package net.ccbluex.liquidbounce.font;

import java.awt.Font;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/* loaded from: LiquidBounce-b73.jar:net/ccbluex/liquidbounce/TTFFontManager.class */
public abstract class TTFFontManager {
    public static TTFFontRender Font8 = new TTFFontRender(getCorbel(8), true, true);
    public static TTFFontRender Font9 = new TTFFontRender(getCorbel(9), true, true);
    public static TTFFontRender Font10 = new TTFFontRender(getCorbel(10), true, true);
    public static TTFFontRender Font11 = new TTFFontRender(getCorbel(11), true, true);
    public static TTFFontRender Font12 = new TTFFontRender(getCorbel(12), true, true);
    public static TTFFontRender Font14 = new TTFFontRender(getCorbel(14), true, true);
    public static TTFFontRender Font15 = new TTFFontRender(getCorbel(15), true, true);
    public static TTFFontRender Font16 = new TTFFontRender(getCorbel(16), true, true);
    public static TTFFontRender Font18 = new TTFFontRender(getCorbel(18), true, true);
    public static TTFFontRender Font20 = new TTFFontRender(getCorbel(20), true, true);
    public static TTFFontRender Font22 = new TTFFontRender(getCorbel(22), true, true);
    public static TTFFontRender Font24 = new TTFFontRender(getCorbel(24), true, true);
    public static TTFFontRender Font26 = new TTFFontRender(getCorbel(26), true, true);
    public static TTFFontRender Font28 = new TTFFontRender(getCorbel(28), true, true);
    public static TTFFontRender Font35 = new TTFFontRender(getCorbel(35), true, true);
    public static TTFFontRender Font40 = new TTFFontRender(getCorbel(40), true, true);

    private static Font getCorbel(int size) {
        Font font;
        try {
            font = Font.createFont(0, Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("assets/minecraft/fdpclient/font/misans.ttf")).getInputStream()).deriveFont(0, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }
}
