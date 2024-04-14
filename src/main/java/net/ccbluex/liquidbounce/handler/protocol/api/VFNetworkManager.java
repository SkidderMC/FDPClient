/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol.api;

import net.raphimc.vialoader.util.VersionEnum;

public interface VFNetworkManager {

    VersionEnum viaForge$getTrackedVersion();

    void viaForge$setTrackedVersion(final VersionEnum version);

}