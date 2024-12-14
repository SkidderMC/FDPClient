/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import kotlinx.coroutines.*

object SharedScopes {

    @JvmField
    val Default = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @JvmField
    val IO = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun stop() {
        Default.cancel()
        IO.cancel()
    }
}