/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.MoveEvent
import net.minecraft.util.AxisAlignedBB
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance() {
    @JvmStatic
    fun getSpeed(): Float {
        return sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ).toFloat()
    }

    @JvmStatic
    fun strafe() {
        strafe(getSpeed())
    }

    @JvmStatic
    fun move() {
        move(getSpeed())
    }

    @JvmStatic
    fun isMoving(): Boolean {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0f || mc.thePlayer.movementInput.moveStrafe != 0f)
    }

    @JvmStatic
    fun hasMotion(): Boolean {
        return mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0 && mc.thePlayer.motionY != 0.0
    }

    @JvmStatic
    fun strafe(speed: Float) {
        if (!isMoving()) return
        val yaw = getDirection()
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    @JvmStatic
    fun move(speed: Float) {
        if (!isMoving()) return
        val yaw = getDirection()
        mc.thePlayer.motionX += -sin(yaw) * speed
        mc.thePlayer.motionZ += cos(yaw) * speed
    }

    @JvmStatic
    fun limitSpeed(speed: Float) {
        val yaw = getDirection()
        val maxXSpeed = -sin(yaw) * speed
        val maxZSpeed = cos(yaw) * speed
        if (mc.thePlayer.motionX > maxZSpeed) {
            mc.thePlayer.motionX = maxXSpeed
        }
        if (mc.thePlayer.motionZ > maxZSpeed) {
            mc.thePlayer.motionZ = maxZSpeed
        }
    }

    @JvmStatic
    fun limitSpeedByPercent(percent: Float) {
        mc.thePlayer.motionX *= percent
        mc.thePlayer.motionZ *= percent
    }

    @JvmStatic
    fun forward(length: Double) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        mc.thePlayer.setPosition(mc.thePlayer.posX + -sin(yaw) * length, mc.thePlayer.posY, mc.thePlayer.posZ + cos(yaw) * length)
    }

    @JvmStatic
    fun getDirection(): Double {
        var rotationYaw = mc.thePlayer.rotationYaw
        if (mc.thePlayer.moveForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (mc.thePlayer.moveForward < 0f) forward = -0.5f else if (mc.thePlayer.moveForward > 0f) forward = 0.5f
        if (mc.thePlayer.moveStrafing > 0f) rotationYaw -= 90f * forward
        if (mc.thePlayer.moveStrafing < 0f) rotationYaw += 90f * forward
        return Math.toRadians(rotationYaw.toDouble())
    }

    var bps = 0.0
        private set
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0

    @JvmStatic
    fun setMotion(speed: Double) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            mc.thePlayer.motionX = (forward * speed * cos
                    + strafe * speed * sin)
            mc.thePlayer.motionZ = (forward * speed * sin
                    - strafe * speed * cos)
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun setSpeed(moveEvent: MoveEvent, moveSpeed: Double, pseudoYaw: Float, pseudoStrafe: Double, pseudoForward: Double) {
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var yaw = pseudoYaw
        if (forward == 0.0 && strafe == 0.0) {
            moveEvent.z = 0.0
            moveEvent.x = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
            moveEvent.x = forward * moveSpeed * cos + strafe * moveSpeed * sin
            moveEvent.z = forward * moveSpeed * sin - strafe * moveSpeed * cos
        }
    }

    @JvmStatic
    fun calculateGround(): Double {
        val playerBoundingBox = mc.thePlayer.entityBoundingBox
        var blockHeight = 1.0
        var ground = mc.thePlayer.posY
        while (ground > 0.0) {
            val customBox = AxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ)
            if (mc.theWorld.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }
            ground -= blockHeight
        }
        return 0.0
    }
}