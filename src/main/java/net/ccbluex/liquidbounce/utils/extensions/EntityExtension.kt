/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(0f)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox)
    val xDist = abs(pos.xCoord - eyes.xCoord)
    val yDist = abs(pos.yCoord - eyes.yCoord)
    val zDist = abs(pos.zCoord - eyes.zCoord)
    return sqrt(xDist.pow(2) + yDist.pow(2) + zDist.pow(2))
}

fun getNearestPointBB(eye: Vec3, box: AxisAlignedBB): Vec3 {
    val origin = doubleArrayOf(eye.xCoord, eye.yCoord, eye.zCoord)
    val destMins = doubleArrayOf(box.minX, box.minY, box.minZ)
    val destMaxs = doubleArrayOf(box.maxX, box.maxY, box.maxZ)
    for (i in 0..2) {
        if (origin[i] > destMaxs[i]) origin[i] = destMaxs[i] else if (origin[i] < destMins[i]) origin[i] = destMins[i]
    }
    return Vec3(origin[0], origin[1], origin[2])
}

fun Entity.rayTrace(blockReachDistance: Double): MovingObjectPosition {
    return this.rayTrace(blockReachDistance, 1f)
}

fun Entity.rayTraceWithCustomRotation(blockReachDistance: Double, yaw: Float, pitch: Float): MovingObjectPosition {
    val vec3 = this.getPositionEyes(1f)
    val vec31 = this.getVectorForRotation(pitch, yaw)
    val vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance)
    return this.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)
}

fun Entity.rayTraceWithCustomRotation(blockReachDistance: Double, rotation: Rotation): MovingObjectPosition {
    return this.rayTraceWithCustomRotation(blockReachDistance, rotation.yaw, rotation.pitch)
}

fun Entity.rayTraceWithServerSideRotation(blockReachDistance: Double): MovingObjectPosition {
    return this.rayTraceWithCustomRotation(blockReachDistance, RotationUtils.serverRotation)
}

fun EntityPlayer.getEyeVec3(): Vec3 {
    return Vec3(this.posX, this.entityBoundingBox.minY + this.getEyeHeight(), this.posZ)
}

val EntityLivingBase.renderHurtTime: Float
    get() = this.hurtTime - if (this.hurtTime != 0) { Minecraft.getMinecraft().timer.renderPartialTicks } else { 0f }

val EntityLivingBase.hurtPercent: Float
    get() = (this.renderHurtTime) / 10

val EntityLivingBase.skin: ResourceLocation // TODO: add special skin for mobs
    get() = if (this is EntityPlayer) { Minecraft.getMinecraft().netHandler.getPlayerInfo(this.uniqueID)?.locationSkin } else { null } ?: DefaultPlayerSkin.getDefaultSkinLegacy()

val EntityLivingBase.ping: Int
    get() = if (this is EntityPlayer) { Minecraft.getMinecraft().netHandler.getPlayerInfo(this.uniqueID)?.responseTime?.coerceAtLeast(0) } else { null } ?: -1