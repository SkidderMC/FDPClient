package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

open class NoWebMode(val modeName: String): MinecraftInstance() {
	open fun onUpdate() {}
}
