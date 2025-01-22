/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.other

import net.ccbluex.liquidbounce.handler.payload.ClientFixes

object AutoReconnect {
    const val MAX = 60000
    const val MIN = 1000

    var isEnabled = true
        internal set

    var delay by ClientFixes.autoReconnectDelayValue
}