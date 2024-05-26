/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import me.zywl.fdpclient.event.EventState
import me.zywl.fdpclient.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.MathHelper
import net.minecraft.util.Timer
import kotlin.math.cos
import kotlin.math.sin

class AACBHopSpeed : SpeedMode("AACBHop") {
    
    private val bypassMode = ListValue("BhopMode", arrayOf("AAC", "AAC3.5.0", "AAC2", "AAC4", "AAC6", "AAC7", "LowHop2", "LowHop3"), "AAC")
                                                           
    private var legitHop = true
    private var waitForGround = false

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer
                                                           
    override fun onEnable() {
        legitHop = true
        if (bypassMode.equals("AAC3.5.0") && player.onGround) {
            player.motionZ = 0.0
            player.motionX = player.motionZ
        }
    }
    
    override fun onPreMotion() {
        if (player.isInWater) return
        when (bypassMode.get().lowercase()) {
            "aac" -> {

                if (MovementUtils.isMoving()) {
                    timer.timerSpeed = 1.08f
                    if (player.onGround) {
                        player.motionY = 0.399
                        val f = player.rotationYaw * 0.017453292f
                        player.motionX -= (MathHelper.sin(f) * 0.2f).toDouble()
                        player.motionZ += (MathHelper.cos(f) * 0.2f).toDouble()
                        timer.timerSpeed = 2f
                    } else {
                        player.motionY *= 0.97
                        player.motionX *= 1.008
                        player.motionZ *= 1.008
                    }
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    timer.timerSpeed = 1f
                }
            }
            "aac2" -> {

                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        player.jump()
                        player.motionX *= 1.02
                        player.motionZ *= 1.02
                    } else if (player.motionY > -0.2) {
                        player.jumpMovementFactor = 0.08f
                        player.motionY += 0.01431
                        player.jumpMovementFactor = 0.07f
                    }
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "aac4" -> {
                if (MovementUtils.isMoving()) {
                    if (legitHop) {
                        if (player.onGround) {
                            player.jump()
                            player.onGround = false
                            legitHop = false
                        }
                        return
                    }
                    if (player.onGround) {
                        player.onGround = false
                        MovementUtils.strafe(0.375f)
                        player.jump()
                        player.motionY = 0.41
                    } else player.speedInAir = 0.0211f
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    legitHop = true
                }
            }
            "aac6" -> {
                timer.timerSpeed = 1f

                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        if (legitHop) {
                            player.motionY = 0.4
                            MovementUtils.strafe(0.15f)
                            player.onGround = false
                            legitHop = false
                            return
                        }
                        player.motionY = 0.41
                        MovementUtils.strafe(0.47458485f)
                    }

                    if (player.motionY < 0 && player.motionY > -0.2) timer.timerSpeed =
                        (1.2 + player.motionY).toFloat()

                    player.speedInAir = 0.022151f
                } else {
                    legitHop = true
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "aac7" -> {
                if (player.onGround) {
                    player.jump()
                    player.motionY = 0.405
                    player.motionX *= 1.004
                    player.motionZ *= 1.004
                    return
                }

                val speed = MovementUtils.getSpeed() * 1.0072
                val yaw = Math.toRadians(player.rotationYaw.toDouble())
                player.motionX = -sin(yaw) * speed
                player.motionZ = cos(yaw) * speed
            }
            "lowhop2" -> {
                timer.timerSpeed = 1f

                if (MovementUtils.isMoving()) {
                    timer.timerSpeed = 1.09f
                    if (player.onGround) {
                        if (legitHop) {
                            player.jump()
                            legitHop = false
                            return
                        }
                        player.motionY = 0.343
                        MovementUtils.strafe(0.534f)
                    }
                } else {
                    legitHop = true
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "lowhop3" -> {
                if (MovementUtils.isMoving()) {
                    if (player.hurtTime <= 0) {
                        if (player.onGround) {
                            waitForGround = false
                            if (!legitHop) legitHop = true
                            player.jump()
                            player.motionY = 0.41
                        } else {
                            if (waitForGround) return
                            if (player.isCollidedHorizontally) return
                            legitHop = false
                            player.motionY -= 0.0149
                        }
                        if (!player.isCollidedHorizontally) MovementUtils.forward(if (legitHop) 0.0016 else 0.001799)
                    } else {
                        legitHop = true
                        waitForGround = true
                    }
                } else {
                    player.motionZ = 0.0
                    player.motionX = 0.0
                }

                val speed = MovementUtils.getSpeed().toDouble()
                player.motionX = -(sin(MovementUtils.direction) * speed)
                player.motionZ = cos(MovementUtils.direction) * speed
            }
        }
    }

    override fun onMotion(event: MotionEvent) {
        when (bypassMode.get()) {
            "AAC3.5.0" -> {
                if (event.eventState === EventState.POST && MovementUtils.isMoving() && !player.isInWater && !player.isInLava) {
                    player.jumpMovementFactor += 0.00208f
                    if (player.fallDistance <= 1f) {
                        if (player.onGround) {
                            player.jump()
                            player.motionX *= 1.0118
                            player.motionZ *= 1.0118
                        } else {
                            player.motionY -= 0.0147
                            player.motionX *= 1.00138
                            player.motionZ *= 1.00138
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        timer.timerSpeed = 1f
        player.speedInAir = 0.02f
        player.jumpMovementFactor = 0.02f
    }
}
