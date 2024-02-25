/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;
;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class MinecraftInstance {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static FontRenderer fontRenderer;
}
