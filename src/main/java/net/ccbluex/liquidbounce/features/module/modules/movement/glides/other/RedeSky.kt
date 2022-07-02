package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class RedeSkyGlide : GlideMode("RedeSky") {

    private var jumped = 0
    var rrr = 1f


        override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.ticksExisted / 1 == 0) {
            mc.thePlayer.sendQueue.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX + 2,
                    mc.thePlayer.posY + 2,
                    mc.thePlayer.posZ + 2,
                    true
                )
            )
            mc.thePlayer.sendQueue.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX + 2,
                    mc.thePlayer.posY + 2,
                    mc.thePlayer.posZ + 2,
                    true
                )
            )
        }
        if (mc.thePlayer.ticksExisted / 1 == 0) {
            mc.thePlayer.sendQueue.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX + 3,
                    mc.thePlayer.posY + 3,
                    mc.thePlayer.posZ + 3,
                    true
                )
            )
            mc.thePlayer.sendQueue.addToSendQueue(
                C04PacketPlayerPosition(
                    mc.thePlayer.posX + 3,
                    mc.thePlayer.posY + 3,
                    mc.thePlayer.posZ + 3,
                    true
                )
            )
        }
        if (mc.thePlayer.ticksExisted / 1 == 0) {
            mc.thePlayer.motionY = -0.103019482
        }
        mc.timer.timerSpeed = 0.3f
        mc.thePlayer.jumpMovementFactor = 0.09f
        if (mc.thePlayer.ticksExisted < 10) {
            mc.timer.timerSpeed = rrr
            mc.timer.timerSpeed = 0.3f
        } else {
            mc.timer.timerSpeed = 0.3f
            if (mc.thePlayer != null) {
                mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer())
                mc.thePlayer.sendQueue.addToSendQueue(S08PacketPlayerPosLook())
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    if (jumped <= 25) {
                        jumped++
                        if (jumped < 22);
                        mc.thePlayer.posX = jumped++.toDouble()
                        mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction())
                        mc.thePlayer.posY = jumped++.toDouble()
                        mc.thePlayer.posZ = jumped++.toDouble()
                        if (jumped < 10) ; else if (jumped < 20);
                        if (jumped < 30) ; else if (jumped < 40);
                        if (jumped < 50) ; else if (jumped < 60);
                        if (jumped < 70) ; else if (jumped < 80);
                        if (jumped < 90) ; else if (jumped < 100);
                        if (jumped < 110) ; else if (jumped < 120);
                        if (jumped < 130) ; else if (jumped < 140);
                        if (jumped < 150) ; else if (jumped < 160);
                    }
                }
            }
        }
    }
}