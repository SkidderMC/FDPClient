/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

@Suppress("ControlFlowWithEmptyBody")
object CoroutineUtils {
	fun waitUntil(condition: () -> Boolean) {
		while (!condition()) {}
	}
}