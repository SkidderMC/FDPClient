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
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lets you freely steer and fly the entity you are currently riding (boats, minecarts,
 * animals). Hold jump to rise, sneak to descend, and use your normal movement keys to
 * drive it in the direction you are looking.
 */
object VehicleControl : Module("VehicleControl", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val horizontalSpeed by float("HorizontalSpeed", 0.5f, 0.1f..5f)
    private val verticalSpeed by float("VerticalSpeed", 0.5f, 0.1f..5f)
    private val glide by float("Glide", 0f, -0.2f..0f)

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val vehicle = player.ridingEntity ?: return@handler
        val settings = mc.gameSettings

        vehicle.motionY = when {
            settings.keyBindJump.isKeyDown -> verticalSpeed.toDouble()
            settings.keyBindSneak.isKeyDown -> -verticalSpeed.toDouble()
            else -> glide.toDouble()
        }

        val input = player.movementInput
        if (input.moveForward == 0f && input.moveStrafe == 0f) {
            vehicle.motionX = 0.0
            vehicle.motionZ = 0.0
        } else {
            val yaw = MovementUtils.direction
            vehicle.motionX = -sin(yaw) * horizontalSpeed
            vehicle.motionZ = cos(yaw) * horizontalSpeed
        }
    }
}
