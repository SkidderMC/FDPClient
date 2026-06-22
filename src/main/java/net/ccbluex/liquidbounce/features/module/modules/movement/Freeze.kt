/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.play.client.C03PacketPlayer

object Freeze : Module("Freeze", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val noMotion by boolean("No Motion", true)
        .describe("Zero horizontal motion while frozen.")

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer == null)
            return@handler

        if (event.packet is C03PacketPlayer)
            event.cancelEvent()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (noMotion) {
            player.motionX = 0.0
            player.motionZ = 0.0
        }
    }
}
