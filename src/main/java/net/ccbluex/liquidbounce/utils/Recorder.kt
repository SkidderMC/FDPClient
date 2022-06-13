/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S45PacketTitle

object Recorder : Listenable{
    var syncEntity: EntityLivingBase? = null
    var killCounts = 0
    var totalPlayed = 0
    var win = 0
    var ban = 0
    var startTime = System.currentTimeMillis()

    @EventTarget
    private fun onAttack(event: AttackEvent) { syncEntity = event.targetEntity as EntityLivingBase?
    }
    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        if(syncEntity != null && syncEntity!!.isDead) {
            ++killCounts
            syncEntity = null
        }
    }
    @EventTarget
    private fun onPacket(event: PacketEvent) {
        if (event.packet is C00Handshake) startTime = System.currentTimeMillis()
        val message = (event.packet as S02PacketChat).chatComponent.unformattedText
        val packet = event.packet
        if (packet is S45PacketTitle) {
            val title = (packet.message ?: return@onPacket).formattedText
            if (title.startsWith("§6§l") && title.endsWith("§r") || title.startsWith("§c§lYOU") && title.endsWith(
                    "§r"
                ) || title.startsWith("§c§lGame") && title.endsWith("§r") || title.startsWith("§c§lWITH") && title.endsWith(
                    "§r"
                ) || title.startsWith("§c§lYARR") && title.endsWith("§r")
            ) totalPlayed++
            if (title.startsWith("§6§l") && title.endsWith("§r")) win++
        }
 if(message.contains("Reason")){
ban++
 }
    }

    override fun handleEvents(): Boolean {
        return true
    }
}