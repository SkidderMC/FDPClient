package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.redesky

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity

class Redesky1Velocity : VelocityMode("Redesky1") {
    private val rspAlwaysValue = BoolValue("${valuePrefix}AlwaysReduce", true)
    private val rspDengerValue = BoolValue("${valuePrefix}OnlyDanger", false)
    private val MSTimer = MSTimer()
    override fun onEnable() {
        MSTimer.reset()
    }
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                return
            }

            if (rspDengerValue.get()) {
                val pos = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.motionX / 8000.0, packet.motionY / 8000.0, packet.motionZ / 8000.0, 0f, 0f, 0f, 0f).findCollision(60)
                if (pos != null && pos.y> (mc.thePlayer.posY - 7)) {
                    return
                }
            }

            val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get()) ?: return
            if (rspAlwaysValue.get()) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                packet.motionX = 0
                packet.motionZ = 0
                // event.cancelEvent() better stuff
            }

            if (MSTimer.hasTimePassed(500)) {
                if (!rspAlwaysValue.get()) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                    packet.motionX = 0
                    packet.motionZ = 0
                }
                val count = if (!MSTimer.hasTimePassed(800)) {
                    8
                } else if (!MSTimer.hasTimePassed(1200)) {
                    12
                } else {
                    25
                }
                for (i in 0..count) {
                    mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                }
                MSTimer.reset()
            } else {
                packet.motionX = (packet.motionX * 0.6).toInt()
                packet.motionZ = (packet.motionZ * 0.6).toInt()
                for (i in 0..4) {
                    mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                }
            }
        }
    }
}