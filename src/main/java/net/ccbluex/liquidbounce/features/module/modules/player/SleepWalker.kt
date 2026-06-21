/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SLEEPING

object SleepWalker : Module("SleepWalker", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val notifyServer by boolean("NotifyServer", false)
    private val closeScreen by boolean("CloseScreen", true)

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (!player.isPlayerSleeping)
            return@handler

        player.wakeUpPlayer(true, false, false)

        if (notifyServer)
            sendPacket(C0BPacketEntityAction(player, STOP_SLEEPING))

        if (closeScreen && mc.currentScreen != null)
            mc.displayGuiScreen(null)
    }
}
