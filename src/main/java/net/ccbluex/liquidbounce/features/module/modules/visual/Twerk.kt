/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C0BPacketEntityAction

object Twerk : Module("Twerk", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val onlyOnGround by boolean("OnlyOnGround", true)
        .describe("Only twerk while standing on the ground.")

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (onlyOnGround && !player.onGround) {
            return@handler
        }

        sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SNEAKING))
        sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING))
    }
}
