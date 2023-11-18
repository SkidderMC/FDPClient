/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class NCPLatestSpeed : SpeedMode("NCPLatest") {
  
    private var wasSlow = false


    override fun onDisable() {
        mc.thePlayer.jumpMovementFactor = 0.02f
        super.onDisable()
    }

    override fun onUpdate() {
      
        if (mc.thePlayer.ticksExisted % 20 <= 9) {
            mc.timer.timerSpeed = 1.05f
        } else {
            mc.timer.timerSpeed = 0.98f
        }
      
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                wasSlow = false
                mc.thePlayer.jump()
                MovementUtils.strafe(0.48f)
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(0.48f * (1.0f + 0.13f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                }
            }
            MovementUtils.strafe(MovementUtils.getSpeed() * 1.007f)
            if (MovementUtils.getSpeed() < 0.277)
                wasSlow = true
            if (wasSlow) 
                MovementUtils.strafe(0.277f)
               
            
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            wasSlow = true
        }
    }
}
