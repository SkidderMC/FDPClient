/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes

import net.ccbluex.liquidbounce.features.module.modules.NamedMode
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

open class NoWebMode(override val modeName: String) : MinecraftInstance, NamedMode {
	open fun onUpdate() {}
}
