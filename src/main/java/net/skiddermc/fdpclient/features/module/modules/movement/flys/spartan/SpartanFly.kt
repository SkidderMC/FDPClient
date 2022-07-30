package net.skiddermc.fdpclient.features.module.modules.movement.flys.spartan

import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.timer.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class SpartanFly : FlyMode("Spartan") {
    private val timer = TickTimer()

    override fun onUpdate(event: UpdateEvent) {
        fly.antiDesync = true
        mc.thePlayer.motionY = 0.0
        timer.update()
        if (timer.hasTimePassed(12)) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true))
            timer.reset()
        }
    }
}