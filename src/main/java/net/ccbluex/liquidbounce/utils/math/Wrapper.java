/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.math;


import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public class Wrapper {
    public static boolean canSendMotionPacket = true;
    public static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean notificationsAllowed = false;

    public static Timer getTimer() {
        return ((Minecraft) Minecraft.getMinecraft()).timer;
    }

    public static boolean notificationsAllowed() {
        return notificationsAllowed;
    }

    public static void notificationsAllowed(boolean value) {
        notificationsAllowed = value;
    }


}
