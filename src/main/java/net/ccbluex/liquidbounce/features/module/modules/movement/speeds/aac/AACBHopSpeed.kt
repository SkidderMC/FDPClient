/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin

class AACBHopSpeed : SpeedMode("AACBHop") {
    
    private val bypassMode = ListValue("BhopMode", arrayOf("AAC", "AAC2", "AAC4", "AAC6", "AAC7", "LowHop2", "LowHop3"), "AAC")
                                                           
    private var legitHop = true
    private var waitForGround = false
                                                           
    override fun onEnable() {
        legitHop = true
    }
    
    override fun onPreMotion() {
        if (mc.thePlayer.isInWater) return
        when (bypassMode.get().lowercase()) {
            "aac" -> {

                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.08f
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.399
                        val f = mc.thePlayer.rotationYaw * 0.017453292f
                        mc.thePlayer.motionX -= (MathHelper.sin(f) * 0.2f).toDouble()
                        mc.thePlayer.motionZ += (MathHelper.cos(f) * 0.2f).toDouble()
                        mc.timer.timerSpeed = 2f
                    } else {
                        mc.thePlayer.motionY *= 0.97
                        mc.thePlayer.motionX *= 1.008
                        mc.thePlayer.motionZ *= 1.008
                    }
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.timer.timerSpeed = 1f
                }
            }
            "aac2" -> {

                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionX *= 1.02
                        mc.thePlayer.motionZ *= 1.02
                    } else if (mc.thePlayer.motionY > -0.2) {
                        mc.thePlayer.jumpMovementFactor = 0.08f
                        mc.thePlayer.motionY += 0.01431
                        mc.thePlayer.jumpMovementFactor = 0.07f
                    }
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "aac4" -> {
                if (MovementUtils.isMoving()) {
                    if (legitHop) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            mc.thePlayer.onGround = false
                            legitHop = false
                        }
                        return
                    }
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.onGround = false
                        MovementUtils.strafe(0.375f)
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.41
                    } else mc.thePlayer.speedInAir = 0.0211f
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    legitHop = true
                }
            }
            "aac6" -> {
                mc.timer.timerSpeed = 1f

                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        if (legitHop) {
                            mc.thePlayer.motionY = 0.4
                            MovementUtils.strafe(0.15f)
                            mc.thePlayer.onGround = false
                            legitHop = false
                            return
                        }
                        mc.thePlayer.motionY = 0.41
                        MovementUtils.strafe(0.47458485f)
                    }

                    if (mc.thePlayer.motionY < 0 && mc.thePlayer.motionY > -0.2) mc.timer.timerSpeed =
                        (1.2 + mc.thePlayer.motionY).toFloat()

                    mc.thePlayer.speedInAir = 0.022151f
                } else {
                    legitHop = true
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "aac7" -> {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.motionY = 0.405
                    mc.thePlayer.motionX *= 1.004
                    mc.thePlayer.motionZ *= 1.004
                    return
                }

                val speed = MovementUtils.getSpeed() * 1.0072
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                mc.thePlayer.motionX = -sin(yaw) * speed
                mc.thePlayer.motionZ = cos(yaw) * speed
            }
            "lowhop2" -> {
                mc.timer.timerSpeed = 1f

                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.09f
                    if (mc.thePlayer.onGround) {
                        if (legitHop) {
                            mc.thePlayer.jump()
                            legitHop = false
                            return
                        }
                        mc.thePlayer.motionY = 0.343
                        MovementUtils.strafe(0.534f)
                    }
                } else {
                    legitHop = true
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "lowhop3" -> {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.hurtTime <= 0) {
                        if (mc.thePlayer.onGround) {
                            waitForGround = false
                            if (!legitHop) legitHop = true
                            mc.thePlayer.jump()
                            mc.thePlayer.motionY = 0.41
                        } else {
                            if (waitForGround) return
                            if (mc.thePlayer.isCollidedHorizontally) return
                            legitHop = false
                            mc.thePlayer.motionY -= 0.0149
                        }
                        if (!mc.thePlayer.isCollidedHorizontally) MovementUtils.forward(if (legitHop) 0.0016 else 0.001799)
                    } else {
                        legitHop = true
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
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
    }
}
