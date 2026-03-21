/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.client.realMotionY
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin

object OldMatrixHurt : LongJumpMode("OldMatrixHurt") {
    private var detected = false
    private var motionY = 0.0
    private var tick = 0

    override fun onEnable() {
        detected = false
        motionY = 0.0
        tick = 0
        LongJump.sendLegacyWarning()
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!detected) {
            return
        }

        val yaw = Math.toRadians(player.rotationYaw.toDouble())
        player.motionY = motionY
        player.motionX += -(sin(yaw) * LongJump.oldMatrixHurtBoostSpeed)
        player.motionZ += cos(yaw) * LongJump.oldMatrixHurtBoostSpeed
        tick++

        if (tick >= LongJump.oldMatrixHurtTicks) {
            tick = 0
            detected = false
            motionY = 0.0
            onAttemptDisable()
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }

            if (packet.realMotionY > 0.2) {
                detected = true
                motionY = packet.realMotionY
            }
        }
    }

    override fun onAttemptDisable() {
        LongJump.state = false
    }
}
