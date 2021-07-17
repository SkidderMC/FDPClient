/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */

package net.ccbluex.liquidbounce.features.special

object AutoReconnect {
    const val MAX = 60000
    const val MIN = 1000

    var isEnabled = true
        private set
    var delay = 5000
        set(value) {
            isEnabled = delay < MAX

            field = value
        }
}