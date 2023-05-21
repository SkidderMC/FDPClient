/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class LNCPHopSpeed : SpeedMode("LNCPHop") {

    private var mspeed = 0.0
    private var justJumped = false

    override fun onEnable() {
        mc.timer.timerSpeed = 1.0865f
        super.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mspeed = MovementUtils.defaultSpeed().toDouble() * 1.73

                justJumped = true
            } else {
                if (justJumped) {
                    mspeed *= 0.72150289018
                    justJumped = false
                } else {
                    mspeed -= mspeed / 159
                }
            }
            if (mspeed < MovementUtils.defaultSpeed().toDouble()) 
                mspeed = MovementUtils.defaultSpeed().toDouble()

            MovementUtils.strafe(mspeed.toFloat())
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
