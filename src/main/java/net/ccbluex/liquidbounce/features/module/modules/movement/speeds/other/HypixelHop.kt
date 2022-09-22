/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.value.*


class HypixelHop : SpeedMode("HypixelHop") {

    private val bypassMode = ListValue("${valuePrefix}BypassMode", arrayOf("Stable", "Stable2", "Test2", "TestLowHop", "DortwareHop", "OldSafe", "OldTest"), "Stable")
    private val slowdownValue = FloatValue("${valuePrefix}SlowdownValue", 0f, -0.15f, 0.5f)
    private val yMotion = FloatValue("$valuePrefix}JumpYMotion", 0.4f, 0.395f, 0.42f)
    private val yPort = BoolValue("${valuePrefix}SlightYPort", true)
    private val damageBoost = BoolValue("${valuePrefix}DamageBoost", true)

    private var watchdogMultiplier = 1.0
    private var minSpeed = 0.0
    private var oldMotionX = 0.0
    private var oldMotionZ = 0.0
    private var pastX = 0.0
    private var pastZ = 0.0
    private var moveDist = 0.0
    private var wasOnGround = false

    override fun onUpdate() {
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        
        if (yPort.get()) {
            if (mc.thePlayer.motionY < 0.1 && mc.thePlayer.motionY > -0.21 && mc.thePlayer.motionY != 0.0) {
                mc.thePlayer.motionY -= 0.05
            }
        }
        
        if (damageBoost.get()) {
            if (mc.thePlayer.hurtTime > 0) {
                mc.thePlayer.motionX *= 1.025
                mc.thePlayer.motionZ *= 1.025
            }
        }
        
        moveDist = MathUtils.getDistance(pastX, pastZ, mc.thePlayer.posX, mc.thePlayer.posZ).toDouble()
        
        when (bypassMode.get().lowercase()) {
            
            "stable2" -> {

                if (!mc.thePlayer.onGround) {
                    
                    oldMotionX = mc.thePlayer.motionX
                    oldMotionZ = mc.thePlayer.motionZ

                    MovementUtils.strafe()
                    mc.thePlayer.motionX = (mc.thePlayer.motionX * 3 + oldMotionX) / 4
                    mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 3 + oldMotionZ) / 4

                } else if (MovementUtils.isMoving()) {
                    mc.thePlayer.jump()
                }
            }
                
            "stable"-> {


                if (!mc.thePlayer.onGround) {
                    if (!wasOnGround) {
                        oldMotionX = mc.thePlayer.motionX
                        oldMotionZ = mc.thePlayer.motionZ

                        MovementUtils.strafe()
                        mc.thePlayer.motionX = (mc.thePlayer.motionX * 3 + oldMotionX) / 4
                        mc.thePlayer.motionZ = (mc.thePlayer.motionZ * 3 + oldMotionZ) / 4
                        
                        mc.thePlayer.motionX *= 0.99
                        mc.thePlayer.motionZ *= 0.99
                    }
                    
                    oldMotionX = mc.thePlayer.motionX
                    oldMotionZ = mc.thePlayer.motionZ
                    wasOnGround = false
                } else if (MovementUtils.isMoving()) {
                    wasOnGround = true
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }
            
            "test2" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                    watchdogMultiplier = 0.560625
                    wasOnGround = true
                } else if (wasOnGround) {
                    watchdogMultiplier = moveDist - 0.66 * (moveDist - 0.2875)
                    wasOnGround = false
                } else {
                    moveDist = moveDist * 0.91
                    if (mc.thePlayer.moveStrafing > 0) {
                        watchdogMultiplier += (MovementUtils.getSpeed().toDouble() - moveDist) * 0.2875
                        watchdogMultiplier -= 0.015
                    }
                }

            }
            
            "testlowhop"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.2
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.31999998688697817
                }
            }

            "oldsafe"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.45
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }

            "oldtest"-> {
                if(MovementUtils.isMoving() && mc.thePlayer.onGround) {
                    watchdogMultiplier = 1.2
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = yMotion.get().toDouble()
                }
            }
            "dortwarehop" -> {
                if (MovementUtils.isMoving()) {
                    minSpeed = 0.2873
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = yMotion.get().toDouble()
                        watchdogMultiplier *= 1.6
                        wasOnGround = true
                    } else if (wasOnGround) {
                        watchdogMultiplier -= 0.76 * (watchdogMultiplier - minSpeed)
                        wasOnGround = false
                    } else {
                        watchdogMultiplier = moveDist - moveDist / 159
                    }

                    MovementUtils.strafe(Math.max(watchdogMultiplier, minSpeed).toFloat())
                } else {
                    watchdogMultiplier = 0.0
                }
            }
        }
        if (watchdogMultiplier > 1) {
            when (bypassMode.get().lowercase()) {
                "oldsafe" -> watchdogMultiplier -= 0.2
                "oldtest" -> watchdogMultiplier -= 0.05
                "testlowhop" -> watchdogMultiplier -= 0.05
            }
        } else {
            watchdogMultiplier = 1.0
        }
        
        pastX = mc.thePlayer.posX 
        pastZ = mc.thePlayer.posZ
    }

    override fun onMove(event: MoveEvent) {
        when (bypassMode.get().lowercase()) {
            "testlowhop" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier.toDouble() * ( 0.90151f   - slowdownValue.get()).toDouble()).toFloat())
            "test2" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier.toDouble() * ( 1.0f         - slowdownValue.get()).toDouble()).toFloat())
            "oldsafe" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier.toDouble() * ( 1.081237f    - slowdownValue.get()).toDouble()).toFloat())
            "oldtest" -> MovementUtils.strafe(( 0.2873 * watchdogMultiplier.toDouble() * ( 1.0f         - slowdownValue.get()).toDouble()).toFloat())
            
        }
    }
}
