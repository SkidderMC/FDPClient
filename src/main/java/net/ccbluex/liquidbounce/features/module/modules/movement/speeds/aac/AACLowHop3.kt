/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import kotlin.math.cos
import kotlin.math.sin

class AACLowHop3 : SpeedMode("AACLowHop3") {
    private var firstJump = false
    private var waitForGround = false

    override fun onEnable() {
        firstJump = true
    }

    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.hurtTime <= 0) {
                if (mc.thePlayer.onGround) {
                    waitForGround = false
                    if (!firstJump) firstJump = true
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.41
                } else {
                    if (waitForGround) return
                    if (mc.thePlayer.isCollidedHorizontally) return
                    firstJump = false
                    mc.thePlayer.motionY -= 0.0149
                }
                if (!mc.thePlayer.isCollidedHorizontally) MovementUtils.forward(if (firstJump) 0.0016 else 0.001799)
            } else {
                firstJump = true
                waitForGround = true
            }
        } else {
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = 0.0
        }

        val speed = MovementUtils.getSpeed().toDouble()
        mc.thePlayer.motionX = -(sin(MovementUtils.direction) * speed)
        mc.thePlayer.motionZ = cos(MovementUtils.direction) * speed
    }
}