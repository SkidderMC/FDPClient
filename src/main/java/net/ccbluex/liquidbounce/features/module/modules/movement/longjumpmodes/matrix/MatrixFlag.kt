/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.matrix

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object MatrixFlag : LongJumpMode("MatrixFlag") {
    private var tryFlag = true

    override fun onEnable() {
        tryFlag = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.onGround) {
            tryFlag = true
        }

        if (tryFlag) {
            player.motionY = 0.42
            MovementUtils.strafe(1f)
        } else {
            player.motionX *= 1.03
            player.motionZ *= 1.03
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            tryFlag = false
        }
    }

    override fun onAttemptJump() {
        MovementUtils.strafe()
        tryFlag = true
    }

    override fun onJump(event: JumpEvent) {
        tryFlag = true
        event.cancelEvent()
    }

    override fun onAttemptDisable() {
        LongJump.state = false
    }
}
