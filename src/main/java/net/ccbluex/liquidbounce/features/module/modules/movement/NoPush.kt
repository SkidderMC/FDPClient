/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.BlockPushEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoPush : Module("NoPush", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val blocks by boolean("Blocks", true)
    private val sinking by boolean("Sinking", true)

    val onBlockPush = handler<BlockPushEvent> { event ->
        if (blocks) {
            event.cancelEvent()
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!sinking) {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler

        if (mc.gameSettings.keyBindJump.isKeyDown || mc.gameSettings.keyBindSneak.isKeyDown) {
            return@handler
        }

        if ((player.isInWater || player.isInLava) && player.motionY < 0.0) {
            player.motionY = 0.0
        }
    }
}
