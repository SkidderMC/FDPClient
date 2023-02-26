/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.misc

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.*

class FallingPlayer(
    private var x: Double,
    private var y: Double,
    private var z: Double,
    private var motionX: Double,
    private var motionY: Double,
    private var motionZ: Double,
    private val yaw: Float,
    private var strafe: Float,
    private var forward: Float,
    private val jumpMovementFactor: Float
) : MinecraftInstance() {
    constructor(player: EntityPlayer) : this(
        player.posX,
        player.posY,
        player.posZ,
        player.motionX,
        player.motionY,
        player.motionZ,
        player.rotationYaw,
        player.moveStrafing,
        player.moveForward,
        player.jumpMovementFactor
    )

    private fun calculateForTick() {
        var sr = strafe * 0.9800000190734863f
        var fw = forward * 0.9800000190734863f
        var v = sr * sr + fw * fw
        if (v >= 0.0001f) {
            v = MathHelper.sqrt_float(v)
            if (v < 1.0f) {
                v = 1.0f
            }
            var fixedJumpFactor = (jumpMovementFactor).toFloat()
            if (mc.thePlayer.isSprinting) {
                fixedJumpFactor = (fixedJumpFactor * 1.3).toFloat()
            }
            v = fixedJumpFactor / v
            sr *= v
            fw *= v
            val f1 = MathHelper.sin(yaw * Math.PI.toFloat() / 180.0f)
            val f2 = MathHelper.cos(yaw * Math.PI.toFloat() / 180.0f)
            motionX += (sr * f2 - fw * f1).toDouble()
            motionZ += (fw * f2 + sr * f1).toDouble()
        }
        motionY -= 0.08
        motionY *= 0.9800000190734863
        x += motionX
        y += motionY
        z += motionZ
        motionX *= 0.91
        motionZ *= 0.91
    }

    fun findCollision(ticks: Int): BlockPos? {
        for (i in 0 until ticks) {
            val start = Vec3(x, y, z)
            calculateForTick()
            val end = Vec3(x, y, z)
            var raytracedBlock: BlockPos?
            val w = mc.thePlayer.width / 2f
            if (rayTrace(start, end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(w.toDouble(), 0.0, w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(-w.toDouble(), 0.0, w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(w.toDouble(), 0.0, -w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(-w.toDouble(), 0.0, -w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(w.toDouble(), 0.0, (w / 2f).toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector(-w.toDouble(), 0.0, (w / 2f).toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector((w / 2f).toDouble(), 0.0, w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
            if (rayTrace(start.addVector((w / 2f).toDouble(), 0.0, -w.toDouble()), end).also { raytracedBlock = it } != null) return raytracedBlock
        }
        return null
    }

    private fun rayTrace(start: Vec3, end: Vec3): BlockPos? {
        val result = mc.theWorld.rayTraceBlocks(start, end, true)
        return if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.sideHit == EnumFacing.UP) {
            result.blockPos
        } else null
    }
}
