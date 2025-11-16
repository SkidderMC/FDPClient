/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.Vec3
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance, Listenable {

    fun resetMotion(y: Boolean) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if(y) mc.thePlayer.motionY = 0.0
    }

    var affectSprintOnAttack: Boolean? = null

    var speed
        get() = mc.thePlayer?.run { sqrt(motionX * motionX + motionZ * motionZ).toFloat() } ?: .0f
        set(value) {
            strafe(value)
        }

    val hasMotion
        get() = mc.thePlayer?.run { motionX != .0 || motionY != .0 || motionZ != .0 } == true

    var airTicks = 0
    var groundTicks = 0

    fun hasTheMotion(): Boolean {
        return mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0 && mc.thePlayer.motionY != 0.0
    }

    var bps = 0.0
        private set
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0

    @JvmOverloads
    fun strafe(
        speed: Float = MovementUtils.speed, stopWhenNoInput: Boolean = false, moveEvent: MoveEvent? = null,
        strength: Double = 1.0,
    ) =
        mc.thePlayer?.run {
            if (!mc.thePlayer.isMoving) {
                if (stopWhenNoInput) {
                    moveEvent?.zeroXZ()
                    stopXZ()
                }

                return@run
            }

            val prevX = motionX * (1.0 - strength)
            val prevZ = motionZ * (1.0 - strength)
            val useSpeed = speed * strength

            val yaw = direction
            val x = (-sin(yaw) * useSpeed) + prevX
            val z = (cos(yaw) * useSpeed) + prevZ

            if (moveEvent != null) {
                moveEvent.x = x
                moveEvent.z = z
            }

            motionX = x
            motionZ = z
        }

    fun Vec3.strafe(
        yaw: Float = direction.toDegreesF(), speed: Double = sqrt(xCoord * xCoord + zCoord * zCoord),
        strength: Double = 1.0,
        moveCheck: Boolean = false,
    ): Vec3 {
        if (moveCheck) {
            xCoord = 0.0
            zCoord = 0.0
            return this
        }

        val prevX = xCoord * (1.0 - strength)
        val prevZ = zCoord * (1.0 - strength)
        val useSpeed = speed * strength

        val angle = Math.toRadians(yaw.toDouble())
        xCoord = (-sin(angle) * useSpeed) + prevX
        zCoord = (cos(angle) * useSpeed) + prevZ
        return this
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

            if (movementInput.moveForward < 0f) {
                yaw += 180f
                forward = -0.5f
            } else if (movementInput.moveForward > 0f)
                forward = 0.5f

            if (movementInput.moveStrafe < 0f) yaw += 90f * forward
            else if (movementInput.moveStrafe > 0f) yaw -= 90f * forward

            yaw.toRadiansD()
        } ?: 0.0

    fun isOnGround(height: Double) =
        mc.theWorld != null && mc.thePlayer != null &&
                mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                    mc.thePlayer.entityBoundingBox.offset(Vec3_ZERO.withY(-height))
                ).isNotEmpty()

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

    fun defaultSpeed(): Double {
        var baseSpeed = 0.2873
        if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.moveSpeed)) {
            val amplifier = Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.moveSpeed)
                .amplifier
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1)
        }
        return baseSpeed
    }

    var serverOnGround = false

    var serverX = .0
    var serverY = .0
    var serverZ = .0

    val onPacket = handler<PacketEvent> { event ->
        if (event.isCancelled)
            return@handler

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

    fun distance(
        srcX: Double, srcY: Double, srcZ: Double,
        dstX: Double, dstY: Double, dstZ: Double
    ): Double {
        val xDist = dstX - srcX
        val yDist = dstY - srcY
        val zDist = dstZ - srcZ
        return sqrt(xDist * xDist + yDist * yDist + zDist * zDist)
    }

    fun distance(
        srcX: Double, srcZ: Double,
        dstX: Double, dstZ: Double
    ): Double {
        val xDist = dstX - srcX
        val zDist = dstZ - srcZ
        return sqrt(xDist * xDist + zDist * zDist)
    }

    fun setSpeed(moveEvent: MoveEvent, speed: Double, forward: Float, strafing: Float, yaw: Float) {
        var yaw = yaw
        if (forward == 0.0f && strafing == 0.0f) return
        yaw = getMovementDirection(forward, strafing, yaw)
        val movementDirectionRads = Math.toRadians(yaw.toDouble())
        val x = -sin(movementDirectionRads) * speed
        val z = cos(movementDirectionRads) * speed
        moveEvent.x = x
        moveEvent.z = z
    }

    fun getMovementDirection(forward: Float, strafing: Float, yaw: Float): Float {
        var yaw = yaw
        if (forward == 0.0f && strafing == 0.0f) return yaw
        val reversed = forward < 0.0f
        val strafingYaw = 90.0f *
                if (forward > 0.0f) 0.5f else if (reversed) -0.5f else 1.0f
        if (reversed) yaw += 180.0f
        if (strafing > 0.0f) yaw -= strafingYaw else if (strafing < 0.0f) yaw += strafingYaw
        return yaw
    }

    fun doTargetStrafe(target: EntityLivingBase, direction: Float, radius: Float, moveEvent: MoveEvent, mode: Int = 0) {
        val player = mc.thePlayer ?: return
        if (!player.isMoving) return

        val speed = sqrt(moveEvent.x * moveEvent.x + moveEvent.z * moveEvent.z)
        if (speed <= 0.0001) return

        val dir = when {
            direction > 0.001 -> 1.0
            direction < -0.001 -> -1.0
            else -> 0.0
        }

        val distance = when (mode) {
            1 -> player.getDistanceToEntity(target)
            else -> sqrt((player.posX - target.posX).pow(2) + (player.posZ - target.posZ).pow(2)).toFloat()
        }

        val forward = when {
            distance < radius - speed -> -1.0
            distance > radius + speed ->  1.0
            else -> (distance - radius) / speed
        }

        var strafe = if (distance in (radius - speed * 2)..(radius + speed * 2)) 1.0 else 0.0
        strafe *= dir

        val norm = sqrt(forward.pow(2) + strafe.pow(2))
        val f = forward / norm
        val s = strafe / norm

        var angle = Math.toDegrees(asin(s))
        if (angle > 0) {
            if (f < 0) angle = 180 - angle
        } else {
            if (f < 0) angle = -180 - angle
        }

        val baseYaw = RotationUtils.getRotationsEntity(target).yaw + angle
        val rad = Math.toRadians(baseYaw)

        moveEvent.x = -sin(rad) * speed
        moveEvent.z =  cos(rad) * speed

        player.motionX = moveEvent.x
        player.motionZ = moveEvent.z
    }
}