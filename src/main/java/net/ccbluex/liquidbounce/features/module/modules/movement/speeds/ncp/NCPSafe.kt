/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class NCPSafe : SpeedMode("NCPSafe") {
  
    private var speed = 0.0
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
                mc.thePlayer.motionY = 0.42
              
                speed += 0.2
                if (speed < 0.4873) {
                  speed = 0.4873
                }
                
                if(mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                  speed += 0.02 + mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() * 0.02
                }
                
                justJumped = true
            } else {
                if (justJumped) {
                  speed *= 0.66
                  justJumped = false
                } else {
                  speed -= speed / 159
                }
            }
            MovementUtils.strafe(speed)
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}	

