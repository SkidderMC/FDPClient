/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.intave

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump

object IntaveHop14 : SpeedMode("IntaveHop14") {

    private const val BOOST_CONSTANT = 0.003

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!player.isMoving || player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.onGround) {
            player.tryJump()

            if (player.isSprinting) strafe(strength = Speed.strafeStrength.toDouble())

            mc.timer.timerSpeed = Speed.groundTimer
        } else {
            mc.timer.timerSpeed = Speed.airTimer
        }

        if (Speed.boost && player.motionY > 0.003 && player.isSprinting) {
            player.motionX *= 1f + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
            player.motionZ *= 1f + (BOOST_CONSTANT * Speed.initialBoostMultiplier)
        }
    }

    override fun onJump(event: JumpEvent) {
        if (Speed.intaveLowHop) {
            event.motion = 0.42f - 1.7E-14f
        }
    }
}
