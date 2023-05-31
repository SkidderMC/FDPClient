/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

import java.awt.*;

public class ColorManager {

    public static int astolfoRainbow(int delay, int offset, int index) {
        double rainbowDelay = Math.ceil(System.currentTimeMillis() + (long)((long) delay * index)) / offset;
        return Color.getHSBColor((double)((float)((rainbowDelay %= 360.0) / 360.0)) < 0.5 ? -((float)(rainbowDelay / 360.0)) : (float)(rainbowDelay / 360.0), 0.5F, 1.0F).getRGB();
    }

}
