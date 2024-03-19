package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
class VulcanHighFly : FlyMode("VulcanHigh") {

    private val height = IntegerValue("VulcanHigh-ClipHeight", 10, 50, 100)

    private var ticks = 0
    private var sent = false

    override fun onEnable() {
        sent = false
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE && !mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 1f
            mc.thePlayer.motionY = -if(ticks % 2 == 0) {
                0.16
            } else {
                0.10
            }
            if(ticks == 0) {
                mc.thePlayer.motionY = -0.07
            }
            ticks++
        } else if (mc.thePlayer.onGround && !sent) {
            sent = true
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688697815, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.4199999868869781, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.419999986886978, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.7531999805212, mc.thePlayer.posZ, false))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ, false))
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ)
        }

        if (sent && mc.thePlayer.hurtTime == 10) {
            mc.thePlayer.posY += height.get()
        }
    }

}
