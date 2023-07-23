package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

object OldIntave : FlyMode("OldIntave") {
    private var serverPosX = 0.0
    private var serverPosY = 0.0
    private var serverPosZ = 0.0
    private var teleported = false

    override fun onEnable() {
        serverPosX = mc.thePlayer.posX
        serverPosY = mc.thePlayer.posY
        serverPosZ = mc.thePlayer.posZ
        teleported = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) return
        val thePlayer = mc.thePlayer ?: return
        if (!teleported) {
            val yaw = thePlayer.rotationYaw * 3.1415927f / 180f
            val speed = 6.0
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                mc.thePlayer.setPosition(serverPosX, serverPosY, serverPosZ)
        } else {
            mc.timer.timerSpeed = 0.3f
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet= event.packet

        if (packet is S08PacketPlayerPosLook && !teleported) {
            event.cancelEvent()
        } else if (packet is S12PacketEntityVelocity) {
            if (packet.entityID == mc.thePlayer.entityId && packet.motionY / 8000.0 > 0.5) {
                teleported = true
            }
        }
    }
}
