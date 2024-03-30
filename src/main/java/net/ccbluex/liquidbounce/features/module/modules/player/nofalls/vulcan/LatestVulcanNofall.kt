package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.vulcan

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer

class LatestVulcanNofall : NoFallMode("LatestVulcan") {

    var count = 0
    var isFixed = false

    override fun onEnable() {
        isFixed = false
        count = 0
    }


    override fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.onGround && isFixed) {
            isFixed = false
            count = 0
            mc.timer.timerSpeed = 1f
        }

        if (mc.thePlayer.fallDistance > 2.0F) {
            isFixed = true
            mc.timer.timerSpeed = 0.9F
        }
        if (mc.thePlayer.fallDistance > 2.9) {
            PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))

            mc.thePlayer.motionY = (-0.1F).toDouble()
            mc.thePlayer.fallDistance = 0F
            mc.thePlayer.motionY *= (1.1F).toDouble()

            if (count++ > 5)
                count = 0

        }
    }

}