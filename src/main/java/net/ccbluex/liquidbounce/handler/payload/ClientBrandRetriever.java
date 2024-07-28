/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.payload;

public class ClientBrandRetriever {

    public static String getClientModName() {
        return ClientFixes.getClientModName();
    }
}
