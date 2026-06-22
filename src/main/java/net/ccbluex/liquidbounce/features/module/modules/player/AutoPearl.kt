/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.block.material
import net.ccbluex.liquidbounce.utils.extensions.center
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.extensions.toDegreesF
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.utils.extensions.withY
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.raycastEntity
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AutoPearl : Module("AutoPearl", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST) {

    private val mode by choices("Mode", arrayOf("LookTarget", "NearestEnemy"), "NearestEnemy")
        .describe("How to pick the target to throw the pearl at.")
    private val range by float("Range", 20F, 4F..60F)
        .describe("Maximum distance to target with the pearl.")

    private val minPitch by float("MinPitch", -90F, -90F..0F)
        .describe("Lowest launch pitch to try when aiming.")
    private val maxPitch by float("MaxPitch", 0F, -90F..90F)
        .describe("Highest launch pitch to try when aiming.")
    private val pitchStep by float("PitchStep", 1F, 0.25F..5F)
        .describe("Pitch increment used while solving the throw.")

    private val maxLandingError by float("MaxLandingError", 3F, 0.5F..10F)
        .describe("Max allowed landing distance from the target.")
    private val yawOffset by float("YawOffset", 0F, -180F..180F)
        .describe("Yaw offset applied to the throw direction.")
    private val silent by boolean("Silent", true)
        .describe("Aim silently without moving the visible view.")

    // Ender pearl behaves as a thrown item in 1.8.9: launch speed 1.5, gravity 0.03 per tick,
    // air drag 0.99 per tick, water drag 0.6 per tick, collision size 0.25.
    private const val MOTION_FACTOR = 1.5F
    private const val GRAVITY = 0.03
    private const val AIR_SLOWDOWN = 0.99
    private const val WATER_SLOWDOWN = 0.6
    private const val PEARL_SIZE = 0.25

    private val options = RotationSettings(this).withoutKeepRotation().withRequestPriority(RotationPriority.HIGH)

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.heldItem?.item !is ItemEnderPearl)
            return@handler

        val target = findTarget(player) ?: return@handler

        // Aim slightly below center so the pearl lands at the target's feet/body.
        val targetBox: AxisAlignedBB = target.hitBox
        val aimPoint = targetBox.center.withY(targetBox.minY + (targetBox.maxY - targetBox.minY) * 0.25)

        val eyes = player.eyes

        // Yaw straight toward the target; only the launch pitch needs solving.
        val diffX = aimPoint.xCoord - eyes.xCoord
        val diffZ = aimPoint.zCoord - eyes.zCoord
        val yaw = MathHelper.wrapAngleTo180_float(atan2(diffZ, diffX).toDegreesF() - 90F + yawOffset)

        var bestPitch: Float? = null
        var bestError = Double.MAX_VALUE

        val lowPitch = minOf(minPitch, maxPitch)
        val highPitch = maxOf(minPitch, maxPitch)
        val step = pitchStep.coerceAtLeast(0.25F)

        var pitch = lowPitch
        while (pitch <= highPitch) {
            val error = simulateClosestApproach(player, yaw, pitch, aimPoint)

            if (error < bestError) {
                bestError = error
                bestPitch = pitch
            }

            pitch += step
        }

        val chosenPitch = bestPitch ?: return@handler

        // bestError is a squared distance, so compare against the squared tolerance.
        if (bestError > maxLandingError * maxLandingError)
            return@handler

        val rotation = Rotation(yaw, chosenPitch.coerceIn(-90F, 90F))

        if (silent) {
            setTargetRotation(rotation.fixedSensitivity(), options)
        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch
        }
    }

    /**
     * Find the entity to aim the pearl at, depending on the selected [mode].
     */
    private fun findTarget(player: Entity): Entity? {
        val world = mc.theWorld ?: return null

        return when (mode) {
            "LookTarget" -> raycastEntity(range.toDouble()) {
                it is EntityLivingBase && isSelected(it, true)
            }

            else -> world.loadedEntityList.toList()
                .asSequence()
                .filterIsInstance<EntityLivingBase>()
                .filter { isSelected(it, true) && player.getDistanceToEntityBox(it) <= range }
                .minByOrNull { rotationDifference(it).toDouble() }
        }
    }

    /**
     * Simulates a thrown ender pearl launched with the given [yaw]/[pitch] and returns the squared
     * distance of its closest approach to [aimPoint]. Mirrors the projectile simulation used by the
     * Projectiles visual (motionFactor 1.5, gravity 0.03, air drag 0.99, water drag 0.6).
     */
    private fun simulateClosestApproach(player: Entity, yaw: Float, pitch: Float, aimPoint: Vec3): Double {
        val world = mc.theWorld ?: return Double.MAX_VALUE

        val yawRadians = yaw.toRadians().toDouble()
        val pitchRadians = pitch.toRadians().toDouble()

        var posX = player.posX - cos(yawRadians) * 0.16
        var posY = player.entityBoundingBox.minY + player.eyeHeight - 0.10000000149011612
        var posZ = player.posZ - sin(yawRadians) * 0.16

        var motionX = -sin(yawRadians) * cos(pitchRadians) * 0.4
        var motionY = -sin(pitch.toRadians().toDouble()) * 0.4
        var motionZ = cos(yawRadians) * cos(pitchRadians) * 0.4

        val distance = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)

        if (distance == 0.0)
            return Double.MAX_VALUE

        motionX = motionX / distance * MOTION_FACTOR
        motionY = motionY / distance * MOTION_FACTOR
        motionZ = motionZ / distance * MOTION_FACTOR

        var closest = Double.MAX_VALUE

        var ticks = 0
        while (posY > 0.0 && ticks < 200) {
            val posBefore = Vec3(posX, posY, posZ)
            val posAfter = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

            // Block collision ends the flight.
            val landingPosition = world.rayTraceBlocks(posBefore, posAfter, false, true, false)

            val segmentEnd = landingPosition?.hitVec ?: posAfter

            closest = minOf(closest, distanceSqPointToSegment(aimPoint, posBefore, segmentEnd))

            if (landingPosition != null)
                break

            posX += motionX
            posY += motionY
            posZ += motionZ

            if (BlockPos(posX, posY, posZ).material === Material.water) {
                motionX *= WATER_SLOWDOWN
                motionY *= WATER_SLOWDOWN
                motionZ *= WATER_SLOWDOWN
            } else {
                motionX *= AIR_SLOWDOWN
                motionY *= AIR_SLOWDOWN
                motionZ *= AIR_SLOWDOWN
            }

            motionY -= GRAVITY

            ticks++
        }

        return closest
    }

    /**
     * Squared distance from [point] to the line segment [a]-[b].
     */
    private fun distanceSqPointToSegment(point: Vec3, a: Vec3, b: Vec3): Double {
        val abX = b.xCoord - a.xCoord
        val abY = b.yCoord - a.yCoord
        val abZ = b.zCoord - a.zCoord

        val lengthSq = abX * abX + abY * abY + abZ * abZ

        if (lengthSq == 0.0)
            return point.squareDistanceTo(a)

        var t = ((point.xCoord - a.xCoord) * abX + (point.yCoord - a.yCoord) * abY + (point.zCoord - a.zCoord) * abZ) / lengthSq
        t = t.coerceIn(0.0, 1.0)

        val projection = Vec3(a.xCoord + abX * t, a.yCoord + abY * t, a.zCoord + abZ * t)

        return point.squareDistanceTo(projection)
    }
}
