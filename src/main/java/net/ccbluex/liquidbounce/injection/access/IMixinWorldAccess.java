/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.access;

public interface IMixinWorldAccess {

    void markBlockForUpdate(int var1, int var2, int var3);

    void notifyLightSet(int var1, int var2, int var3);
}
