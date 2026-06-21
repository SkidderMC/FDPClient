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
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

/**
 * Lets you keep a steady, faster pace across frictionless ice instead of sliding,
 * by driving your horizontal motion while standing on an ice block.
 */
object IceSpeed : Module("IceSpeed", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    private val speed by float("Speed", 0.34f, 0.2f..0.6f)

    val onMove = handler<MoveEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        if (!player.isMoving || !player.onGround) return@handler

        val below = BlockPos(player.posX, player.posY - 1.0, player.posZ).block
        if (below == Blocks.ice || below == Blocks.packed_ice) {
            MovementUtils.strafe(speed = speed, moveEvent = event)
        }
    }
}
