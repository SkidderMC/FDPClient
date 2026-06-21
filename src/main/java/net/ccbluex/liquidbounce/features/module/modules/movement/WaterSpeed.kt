/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils

/**
 * Drives your horizontal motion while in water so you swim at a chosen pace instead of
 * being dragged down by liquid friction. Optionally lets you ascend by holding jump.
 */
object WaterSpeed : Module("WaterSpeed", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val speed by float("Speed", 0.28f, 0.15f..0.6f)
    private val ascend by boolean("Ascend", false)

    val onMove = handler<MoveEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        if (!player.isMoving || !player.isInWater) return@handler

        MovementUtils.strafe(speed = speed, moveEvent = event)

        if (ascend && mc.gameSettings.keyBindJump.isKeyDown) {
            event.y = 0.08
        }
    }
}
