/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Continuously spins your view by a fixed yaw step each tick, with an optional locked pitch.
 * A classic for-fun module — your character whirls around on its own.
 */
object SpinBot : Module("SpinBot", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val speed by float("Speed", 20f, 1f..180f)
    private val lockPitch by boolean("LockPitch", false)
    private val pitch by float("Pitch", 0f, -90f..90f) { lockPitch }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        player.rotationYaw += speed

        if (lockPitch) {
            player.rotationPitch = pitch
        }
    }
}
