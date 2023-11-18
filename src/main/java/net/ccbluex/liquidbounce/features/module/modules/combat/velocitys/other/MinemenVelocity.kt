package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.ccbluex.liquidbounce.utils.MovementUtils

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
            mc.thePlayer.motionX *= 0.99
            mc.thePlayer.motionZ *= 0.99
        } else if (ticks == 5 && !lastCancel) {
            MovementUtils.strafe()
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
            } else {
                mc.thePlayer.jump()
                lastCancel = false
            }
        }
    }
}
