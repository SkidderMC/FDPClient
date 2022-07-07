/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue

class HypixelHop : SpeedMode("HypixelHop") {
  
    private val slowdownValue = FloatValue("${valuePrefix}SlowdownValue", 0.15f, 0.01f, 0.5f)
  
    private var watchdogMultiplier = 1.0f 
  
    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            watchdogMultiplier = 1.45
            mc.thePlayer.motionY = 0.41999998688697815
        }

        if (watchdogMultiplier > 1) {
            watchdogMultiplier -= 0.2
        } else {
            watchdogMultiplier = 1
        
    }
    
    override fun onMove(event: MoveEvent) {
         MovementUtils.strafe( 0.02 * ( 1.081237F - watchdogSlowDown.get() ).toDouble() * watchdogMultiplier)
    }
}
