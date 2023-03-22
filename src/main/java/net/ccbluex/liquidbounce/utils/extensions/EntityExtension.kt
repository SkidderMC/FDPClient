/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.script.api.global.Chat.alert
import net.ccbluex.liquidbounce.utils.ClientUtils.mc
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import org.apache.commons.io.IOExceptionWithCause
import java.io.IOException
import javax.vecmath.Vector3d
import kotlin.math.*

/**
 * Allows to get the distance between the current entity and [entity] from the nearest corner of the bounding box
 */
fun Entity.getDistanceToEntityBox(entity: Entity): Double {
    val eyes = this.getPositionEyes(0f)
    val pos = getNearestPointBB(eyes, entity.entityBoundingBox.expand(entity.collisionBorderSize.toDouble(), entity.collisionBorderSize.toDouble(), entity.collisionBorderSize.toDouble()))
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

fun Entity.rayTraceWithCustomRotation(blockReachDistance: Double, yaw: Float, pitch: Float): MovingObjectPosition? {
    val vec3 = this.getPositionEyes(1f)
    val vec31 = this.getVectorForRotation(pitch, yaw)
    val vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance)
    return this.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)
}

fun Entity.rayTraceWithCustomRotation(blockReachDistance: Double, rotation: Rotation): MovingObjectPosition? {
    return this.rayTraceWithCustomRotation(blockReachDistance, rotation.yaw, rotation.pitch)
}

fun Entity.rayTraceWithServerSideRotation(blockReachDistance: Double): MovingObjectPosition? {
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

/**
 * Render entity position
 */
val Entity.renderPos: Vector3d
    get() {
        val x = GLUtils.interpolate(lastTickPosX, posX) - mc.renderManager.viewerPosX
        val y = GLUtils.interpolate(lastTickPosY, posY) - mc.renderManager.viewerPosY
        val z = GLUtils.interpolate(lastTickPosZ, posZ) - mc.renderManager.viewerPosZ

        return Vector3d(x, y, z)
    }

val Entity.renderBoundingBox: AxisAlignedBB
    get() {
        return this.entityBoundingBox
            .offset(-this.posX, -this.posY, -this.posZ)
            .offset(this.renderPos.x, this.renderPos.y, this.renderPos.z)
    }

fun World.getEntitiesInRadius(entity: Entity, radius: Double = 16.0): List<Entity> {
    val box = entity.entityBoundingBox.expand(radius, radius, radius)
    val chunkMinX = floor(box.minX * 0.0625).toInt()
    val chunkMaxX = ceil(box.maxX * 0.0625).toInt()
    val chunkMinZ = floor(box.minZ * 0.0625).toInt()
    val chunkMaxZ = ceil(box.maxZ * 0.0625).toInt()

    val entities = mutableListOf<Entity>()
    (chunkMinX..chunkMaxX).forEach { x ->
        (chunkMinZ..chunkMaxZ)
                .asSequence()
                .map { z -> getChunkFromChunkCoords(x, z) }
                .filter(Chunk::isLoaded)
                .forEach { it.getEntitiesWithinAABBForEntity(entity, box, entities, null) }
    }
    return entities
}
