/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

object SharedScopes {
    val Default = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val IO = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun stop() {
        Default.cancel()
        IO.cancel()
    }
}