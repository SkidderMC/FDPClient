package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer

class Spartan2Fly : FlyMode("Spartan2") {
    override fun onUpdate(event: UpdateEvent) {
        fly.antiDesync = true
        MovementUtils.strafe(0.264f)

        if (mc.thePlayer.ticksExisted % 8 == 0) {
            mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
        }
    }
}