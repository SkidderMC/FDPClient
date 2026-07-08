/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.implementations

interface IMixinEntityPlayerSP {
    /**
     * Overwrites the client's record of the last yaw/pitch it reported to the server. Used when a
     * rotation is dispatched outside the normal movement-packet path (e.g. a scaffold on-tick send)
     * so the follow-up movement packet does not re-send an identical look (GrimAC AimDuplicateLook).
     */
    fun syncLastReportedRotation(yaw: Float, pitch: Float)
}
