/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.matrixGroundStrafe
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.matrixSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.matrixTimerBoostValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.matrixUsePreMotion
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.matrixVeloBoostValue
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.sqrt

object MatrixSpeeds : SpeedMode("MatrixSpeeds") {

    // Variables
    private var recX = 0.0
    private var recZ = 0.0
    private var noVelocityY = 0
    private var wasTimer = false


    override fun onEnable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        when (matrixSpeed) {
            "MatrixHop2" -> {
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                if (isMoving) {
                    if (mc.thePlayer.onGround) {
                        mc.gameSettings.keyBindJump.pressed = false
                        mc.timer.timerSpeed = 1.0f
                        if (matrixGroundStrafe) strafe()
                        mc.thePlayer.jump()
                    }

                    if (mc.thePlayer.motionY > 0.003) {
                        mc.thePlayer.motionX *= 1.0012
                        mc.thePlayer.motionZ *= 1.0012
                        mc.timer.timerSpeed = 1.05f
                    }
                }
            }
            "Matrix6.6.1" -> {
                if (matrixUsePreMotion) return
                mc.thePlayer.jumpMovementFactor = 0.0266f
                if (!mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (speed < 0.217) {
                        strafe(0.217f)
                        mc.thePlayer.jumpMovementFactor = 0.0269f
                    }
                }
                if (mc.thePlayer.motionY < 0) {
                    timer(1.09f)
                    if (mc.thePlayer.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (mc.thePlayer.onGround && isMoving) {
                    mc.gameSettings.keyBindJump.pressed = false
                    timer(1.03f)
                    mc.thePlayer.jump()
                    if (mc.thePlayer.movementInput.moveStrafe <= 0.01 && mc.thePlayer.movementInput.moveStrafe >= -0.01) {
                        strafe((speed * 1.0071).toFloat())
                    }
                } else if (!isMoving) {
                    timer(1.0f)
                }
                if (speed < 0.22)
                    strafe()
            }
            "Matrix6.9.2" -> {
                if (wasTimer) {
                    wasTimer = false
                    mc.timer.timerSpeed = 1.0f
                }
                mc.thePlayer.motionY -= 0.00348
                mc.thePlayer.jumpMovementFactor = 0.026f
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                if (isMoving && mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    mc.timer.timerSpeed = 1.35f
                    wasTimer = true
                    mc.thePlayer.jump()
                    strafe()
                }else if (speed < 0.215) {
                    strafe(0.215f)
                }
            }
        }
    }

    fun onPreMotion() {
        when (matrixSpeed) {
            "Matrix6.6.1" -> {
                if (!matrixUsePreMotion) return
                mc.thePlayer.jumpMovementFactor = 0.0266f
                if (!mc.thePlayer.onGround) {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (speed < 0.217) {
                        strafe(0.217f)
                        mc.thePlayer.jumpMovementFactor = 0.0269f
                    }
                }
                if (mc.thePlayer.motionY < 0) {
                    timer(1.09f)
                    if (mc.thePlayer.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (mc.thePlayer.onGround && isMoving) {
                    mc.gameSettings.keyBindJump.pressed = false
                    timer(1.03f)
                    mc.thePlayer.jump()
                    if (mc.thePlayer.movementInput.moveStrafe <= 0.01 && mc.thePlayer.movementInput.moveStrafe >= -0.01) {
                        strafe((speed * 1.0071).toFloat())
                    }
                } else if (!isMoving) {
                    timer(1.0f)
                }
                if (speed < 0.22)
                    strafe()
            }
        }
    }

    override fun onPacket(event: PacketEvent) {

        val packet = event.packet

        when (matrixSpeed) {
            "Matrix6.6.1" -> {
                if (packet is S12PacketEntityVelocity && matrixVeloBoostValue) {
                    if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                        return
                    }
                    event.cancelEvent()

                    recX = packet.motionX / 8000.0
                    recZ = packet.motionZ / 8000.0
                    if (sqrt(recX * recX + recZ * recZ) > speed) {
                        strafe(sqrt(recX * recX + recZ * recZ).toFloat())
                        mc.thePlayer.motionY = packet.motionY / 8000.0
                    }

                    strafe((speed * 1.1).toFloat())
                }
            }
            "Matrix 6.7.0" -> {
                if (packet is S12PacketEntityVelocity) {
                    if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                        return
                    }
                    noVelocityY = 10
                }
            }
        }
    }

    override fun onDisable() {
        when (matrixSpeed) {
            "Matrix6.6.1" -> {
                mc.timer.timerSpeed = 1f
            }
            "Matrix6.9.2" -> {
                wasTimer = false
                mc.timer.timerSpeed = 1.0f
            }
        }
    }

    private fun timer(value: Float) {
        if(matrixTimerBoostValue) {
            mc.timer.timerSpeed = value
        }
    }

}