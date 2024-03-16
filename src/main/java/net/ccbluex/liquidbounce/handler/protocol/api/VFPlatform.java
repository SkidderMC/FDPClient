/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol.api;

/**
 * This interface is used to access platform specific fields.
 */
public interface VFPlatform {

    /**
     * @return the native version of the platform
     */
    int getGameVersion();
}