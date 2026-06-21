/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pushes the entity you are riding (boat, minecart, animal) forward in your look direction
 * while you hold the forward key, for a quick speed kick without taking full manual control.
 */
object VehicleBoost : Module("VehicleBoost", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val strength by float("Strength", 1.5f, 0.2f..5f)

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val vehicle = player.ridingEntity ?: return@handler

        if (!mc.gameSettings.keyBindForward.isKeyDown) {
            return@handler
        }

        val yaw = Math.toRadians(player.rotationYaw.toDouble())
        vehicle.motionX += -sin(yaw) * 0.1 * strength
        vehicle.motionZ += cos(yaw) * 0.1 * strength
    }
}
