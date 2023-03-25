package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.BlinkUtils

class MinemenVelocity : VelocityMode("Minemen") {

    // created by dg636 $$
    
    private var ticks = 0
    private var lastCancel = false
    private var canCancel = false
    
    override fun onUpdate(event: UpdateEvent) {
        ticks ++
        if (ticks > 23) {
            canCancel = true
        }
        if (ticks >= 2 && ticks <= 4 && !lastCancel) {
            mc.thePlayer.motionX *= 0.98
            mc.thePlayer.motionZ *= 0.98
        } else if (ticks == 5 && !lastCancel) {
            MovementUtils.strafe()
        } else if (lastCancel && ticks == 3) {
            BlinkUtils.setBlinkState(packetTransaction = true, packetKeepAlive = true)
            BlinkUtils.releasePacket(onlySelected = true)
            BlinkUtils.setBlinkState(off = true)
          }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            ticks = 0
            if (canCancel) {
                event.cancelEvent()
                lastCancel = true
                canCancel = false
                BlinkUtils.setBlinkState(all = true)
            } else {
                packet.motionX = (packet.getMotionX() * 0.98f).toInt()
                packet.motionZ = (packet.getMotionZ() * 0.98f).toInt()
                mc.thePlayer.jump()
                lastCancel = false
            }
        }
    }
}
