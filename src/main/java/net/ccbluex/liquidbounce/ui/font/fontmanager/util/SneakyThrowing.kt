/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client para Minecraft usando Minecraft Forge por LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.util

object SneakyThrowing {

    fun sneakyThrow(throwable: Throwable): Nothing = sneakyThrow0(throwable)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Throwable> sneakyThrow0(throwable: Throwable): T = throw throwable as T
}
