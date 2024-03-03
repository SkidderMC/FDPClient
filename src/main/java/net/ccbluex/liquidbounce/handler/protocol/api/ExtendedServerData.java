/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.protocol.api;

import net.raphimc.vialoader.util.VersionEnum;

/**
 * This interface is used to store the target version for a specific server in the server list.
 */
public interface ExtendedServerData {

    VersionEnum viaForge$getVersion();

    void viaForge$setVersion(final VersionEnum version);

}