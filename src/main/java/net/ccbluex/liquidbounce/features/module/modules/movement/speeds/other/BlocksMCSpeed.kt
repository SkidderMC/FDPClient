/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class BlocksMCSpeed : SpeedMode("BlocksMC") {
  
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
                mc.timer.timerSpeed = 1.2f
                wasSlow = false
                mc.thePlayer.jump()
                MovementUtils.strafe(MovementUtils.getSpeed())
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * (1.0f + 0.15f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                }
            }
            MovementUtils.strafe(MovementUtils.getSpeed() * (1.06f + 0.1f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
            if (MovementUtils.getSpeed() < 0.277f + 0.15f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
                wasSlow = true
            if (wasSlow) 
                MovementUtils.strafe(0.277f + 0.15f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1))
               
            
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            wasSlow = true
        }
    }
}
