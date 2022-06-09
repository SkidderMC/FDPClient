package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.TickTimer
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