package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.vulcan

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object LatestVulcan : NoFallMode("LatestVulcan") {
    private var count = 0
    private var isFixed = false

    override fun onEnable() {
        isFixed = false
        count = 0
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (player.onGround && isFixed) {
            isFixed = false
            count = 0
            mc.timer.timerSpeed = 1f
        }

        if (player.fallDistance > 2.0f) {
            isFixed = true
            mc.timer.timerSpeed = 0.9f
        }

        if (player.fallDistance > 2.9f) {
            sendPacket(C03PacketPlayer(true), false)

            player.motionY = -0.1
            player.fallDistance = 0f
            player.motionY *= 1.1

            if (count++ > 5) {
                count = 0
            }
        }
    }
}
