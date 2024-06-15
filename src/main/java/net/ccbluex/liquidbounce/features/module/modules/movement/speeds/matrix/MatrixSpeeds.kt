/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.Timer
import kotlin.math.sqrt

class MatrixSpeeds : SpeedMode("Matrix") {

    private val speeds = ListValue("Matrix-Mode", arrayOf("MatrixHop2", "Matrix6.6.1", "Matrix6.9.2"), "MatrixHop2")


    private val groundStrafe = BoolValue("GroundStrafe", false).displayable{speeds.equals("MatrixHop2")}
    private val veloBoostValue = BoolValue("VelocBoost", true).displayable { speeds.equals("Matrix6.6.1") }
    private val timerBoostValue = BoolValue("TimerBoost", false).displayable { speeds.equals("Matrix6.6.1") }
    private val usePreMotion = BoolValue("UsePreMotion", false).displayable { speeds.equals("Matrix6.6.1") }

    // Variables
    private var recX = 0.0
    private var recZ = 0.0
    private var noVelocityY = 0
    private var wasTimer = false

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer
    val settings: GameSettings
        get() = mc.gameSettings


    override fun onEnable() {
        timer.timerSpeed = 1f
    }

    override fun onUpdate() {
        when (speeds.get()) {
            "MatrixHop2" -> {
                settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        settings.keyBindJump.pressed = false
                        timer.timerSpeed = 1.0f
                        if (groundStrafe.get()) MovementUtils.strafe()
                        player.jump()
                    }

                    if (player.motionY > 0.003) {
                        player.motionX *= 1.0012
                        player.motionZ *= 1.0012
                        timer.timerSpeed = 1.05f
                    }
                }
            }
            "Matrix6.6.1" -> {
                if (usePreMotion.get()) return
                player.jumpMovementFactor = 0.0266f
                if (!player.onGround) {
                    settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                    if (MovementUtils.getSpeed() < 0.217) {
                        MovementUtils.strafe(0.217f)
                        player.jumpMovementFactor = 0.0269f
                    }
                }
                if (player.motionY < 0) {
                    timer(1.09f)
                    if (player.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (player.onGround && MovementUtils.isMoving()) {
                    settings.keyBindJump.pressed = false
                    timer(1.03f)
                    player.jump()
                    if (player.movementInput.moveStrafe <= 0.01 && player.movementInput.moveStrafe >= -0.01) {
                        MovementUtils.strafe((MovementUtils.getSpeed() * 1.0071).toFloat())
                    }
                } else if (!MovementUtils.isMoving()) {
                    timer(1.0f)
                }
                if (MovementUtils.getSpeed() < 0.22)
                    MovementUtils.strafe()
            }
            "Matrix6.9.2" -> {
                if (wasTimer) {
                    wasTimer = false
                    timer.timerSpeed = 1.0f
                }
                player.motionY -= 0.00348
                player.jumpMovementFactor = 0.026f
                settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                if (MovementUtils.isMoving() && player.onGround) {
                    settings.keyBindJump.pressed = false
                    timer.timerSpeed = 1.35f
                    wasTimer = true
                    player.jump()
                    MovementUtils.strafe()
                }else if (MovementUtils.getSpeed() < 0.215) {
                    MovementUtils.strafe(0.215f)
                }
            }
        }
    }

    override fun onPreMotion() {
        when (speeds.get()) {
            "Matrix6.6.1" -> {
                if (!usePreMotion.get()) return
                player.jumpMovementFactor = 0.0266f
                if (!player.onGround) {
                    settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                    if (MovementUtils.getSpeed() < 0.217) {
                        MovementUtils.strafe(0.217f)
                        player.jumpMovementFactor = 0.0269f
                    }
                }
                if (player.motionY < 0) {
                    timer(1.09f)
                    if (player.fallDistance > 1.4)
                        timer(1.0f)
                } else {
                    timer(0.95f)
                }
                if (player.onGround && MovementUtils.isMoving()) {
                    settings.keyBindJump.pressed = false
                    timer(1.03f)
                    player.jump()
                    if (player.movementInput.moveStrafe <= 0.01 && player.movementInput.moveStrafe >= -0.01) {
                        MovementUtils.strafe((MovementUtils.getSpeed() * 1.0071).toFloat())
                    }
                } else if (!MovementUtils.isMoving()) {
                    timer(1.0f)
                }
                if (MovementUtils.getSpeed() < 0.22)
                    MovementUtils.strafe()
            }
        }
    }

    override fun onPacket(event: PacketEvent) {

        val packet = event.packet

        when (speeds.get()) {
            "Matrix6.6.1" -> {
                if (packet is S12PacketEntityVelocity && veloBoostValue.get()) {
                    if (player == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != player) {
                        return
                    }
                    event.cancelEvent()

                    recX = packet.motionX / 8000.0
                    recZ = packet.motionZ / 8000.0
                    if (sqrt(recX * recX + recZ * recZ) > MovementUtils.getSpeed()) {
                        MovementUtils.strafe(sqrt(recX * recX + recZ * recZ).toFloat())
                        player.motionY = packet.motionY / 8000.0
                    }

                    MovementUtils.strafe((MovementUtils.getSpeed() * 1.1).toFloat())
                }
            }
            "Matrix 6.7.0" -> {
                if (packet is S12PacketEntityVelocity) {
                    if (player == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != player) {
                        return
                    }
                    noVelocityY = 10
                }
            }
        }
    }

    override fun onDisable() {
        when (speeds.get()) {
            "Matrix6.6.1" -> {
                timer.timerSpeed = 1f
            }
            "Matrix6.9.2" -> {
                wasTimer = false
                timer.timerSpeed = 1.0f
            }
        }
    }

    private fun timer(value: Float) {
        if(timerBoostValue.get()) {
            timer.timerSpeed = value
        }
    }

}