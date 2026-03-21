package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipDelay
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipGroundWhenClip
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipMotionX
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipMotionY
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipMotionZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipSpoofGround
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipTimer
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipX
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipY
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.clipZ
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.cos
import kotlin.math.sin

object Clip : FlyMode("Clip") {
    private val timer = MSTimer()
    private var lastClip = false

    override fun onEnable() {
        timer.reset()
        lastClip = false
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (event.eventState != EventState.POST) {
            return
        }

        mc.timer.timerSpeed = clipTimer
        player.motionX = clipMotionX.toDouble()
        player.motionY = clipMotionY.toDouble()
        player.motionZ = clipMotionZ.toDouble()

        if (timer.hasTimePassed(clipDelay.toLong())) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            player.setPosition(
                player.posX + (-sin(yaw) * clipX),
                player.posY + clipY,
                player.posZ + (cos(yaw) * clipZ)
            )
            timer.reset()
            lastClip = true
        }

        player.jumpMovementFactor = 0.0f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if (clipSpoofGround) {
                packet.onGround = true
            }

            if (clipGroundWhenClip && (timer.hasTimePassed(clipDelay.toLong()) || lastClip)) {
                packet.onGround = true
                lastClip = false
            }
        }
    }
}
