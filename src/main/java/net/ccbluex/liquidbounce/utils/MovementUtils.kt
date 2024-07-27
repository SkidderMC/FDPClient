/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance(), Listenable {

    var speed
        get() = mc.thePlayer?.run { sqrt(motionX * motionX + motionZ * motionZ).toFloat() } ?: .0f
        set(value) { strafe(value) }

    val isMoving
        get() = mc.thePlayer?.movementInput?.run { moveForward != 0f || moveStrafe != 0f } ?: false

    val hasMotion
        get() = mc.thePlayer?.run { motionX != .0 || motionY != .0 || motionZ != .0 } ?: false

    var bps = 0.0
        private set
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0

    @JvmOverloads
    fun strafe(speed: Float = this.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null) =
        mc.thePlayer?.run {
            if (!isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val yaw = direction
            val x = -sin(yaw) * speed
            val z = cos(yaw) * speed

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            motionX = x
            motionZ = z
        }

    fun forward(distance: Double) =
        mc.thePlayer?.run {
            val yaw = rotationYaw.toRadiansD()
            setPosition(posX - sin(yaw) * distance, posY, posZ + cos(yaw) * distance)
        }

    val direction
        get() = mc.thePlayer?.run {
                var yaw = rotationYaw
                var forward = 1f

                if (moveForward < 0f) {
                    yaw += 180f
                    forward = -0.5f
                } else if (moveForward > 0f)
                    forward = 0.5f

                if (moveStrafing < 0f) yaw += 90f * forward
                else if (moveStrafing > 0f) yaw -= 90f * forward

                yaw.toRadiansD()
            } ?: 0.0

    fun isOnGround(height: Double) =
        mc.theWorld != null && mc.thePlayer != null &&
        mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -height, 0.0)).isNotEmpty()

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.isCancelled)
            return

        val packet = event.packet

        if (packet is C03PacketPlayer) {
            serverOnGround = packet.onGround

            if (packet.isMoving) {
                serverX = packet.x
                serverY = packet.y
                serverZ = packet.z
            }
        }
    }

    fun updateBlocksPerSecond() {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 1) {
            bps = 0.0
        }
        val distance = mc.thePlayer.getDistance(lastX, lastY, lastZ)
        lastX = mc.thePlayer.posX
        lastY = mc.thePlayer.posY
        lastZ = mc.thePlayer.posZ
        bps = distance * (20 * mc.timer.timerSpeed)
    }

    fun updateControls() {
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
        mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)
    }

    override fun handleEvents() = true
}