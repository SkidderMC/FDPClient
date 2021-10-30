package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AAC316GommeFly : FlyMode("AAC3.1.6-Gomme") {
    private var delay = 0
    private var noFlag = false

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.capabilities.isFlying = true

        if (delay == 2) {
            mc.thePlayer.motionY += 0.05
        } else if (delay > 2) {
            mc.thePlayer.motionY -= 0.05
            delay = 0
        }

        delay++

        if (!noFlag) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround))
        }
        if (mc.thePlayer.posY <= 0.0) noFlag = true
    }
}