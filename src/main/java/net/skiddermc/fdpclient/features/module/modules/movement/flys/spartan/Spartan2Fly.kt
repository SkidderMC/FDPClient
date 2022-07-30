package net.skiddermc.fdpclient.features.module.modules.movement.flys.spartan

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
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