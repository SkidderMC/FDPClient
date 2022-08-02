package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer

class SpartanNofall : NoFallMode("Spartan") {
    private val spartanTimer = TickTimer()
    override fun onEnable() {
        spartanTimer.reset()
    }
    override fun onNoFall(event: UpdateEvent) {
        spartanTimer.update()
        if (mc.thePlayer.fallDistance > 1.5 && spartanTimer.hasTimePassed(10)) {
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 10, mc.thePlayer.posZ, true))
            spartanTimer.reset()
        }
    }
}