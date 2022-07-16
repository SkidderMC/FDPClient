/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue


class HypixelHop : SpeedMode("HypixelHop") {
  
    private val bypassMode = ListValue("${valuePrefix}BypassMode", arrayOf("Safe", "Test"), "Safe")
    private val slowdownValue = FloatValue("${valuePrefix}SlowdownValue", 0.15f, 0.01f, 0.5f)
  
    private var watchdogMultiplier = 1.0
  
    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            when (bypassMode.get().lowercase()) {
                "safe" -> {
                    watchdogMultiplier = 1.45
                    mc.thePlayer.motionY = 0.41999998688697815
                }
                
                "test" -> {
                    watchdogMultiplier = 1.2
                    mc.thePlayer.motionY = 0.39999998688697815
                }
            }
        }

        if (watchdogMultiplier > 1) {
            when (bypassMode.get().lowercase()) {
                "safe" -> watchdogMultiplier -= 0.2
                "test" -> watchdogMultiplier -= 0.05
            }
        } else {
            watchdogMultiplier = 1.0
        }
        
    }
    
    override fun onMove(event: MoveEvent) {
      when (bypassMode.get().lowercase()) {
         "safe" -> MovementUtils.strafe(( 0.2875 * watchdogMultiplier.toDouble() * ( 1.081237f    - slowdownValue.get()).toDouble()).toFloat())
         "test" -> MovementUtils.strafe(( 0.2875 * watchdogMultiplier.toDouble() * ( 1.0f         - slowdownValue.get()).toDouble()).toFloat())
      }
    }
}
