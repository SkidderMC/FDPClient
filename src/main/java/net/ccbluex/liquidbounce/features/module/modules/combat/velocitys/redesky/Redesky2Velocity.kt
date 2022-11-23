package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.redesky

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity

class Redesky2Velocity : VelocityMode("Redesky2") {
    private var redeCount = 24
    override fun onEnable() {
        redeCount = 24
    }

    override fun onUpdate(event: UpdateEvent) {
        if (redeCount <24) redeCount++
    }

    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                return
            }

            val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get() + 1) ?: return
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            packet.motionX = 0
            packet.motionZ = 0
            for (i in 0..redeCount) {
                mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
            }
            if (redeCount> 12) redeCount -= 5
        }
    }
}