/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.material
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.toDegreesF
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object EasyPearl : Module("EasyPearl", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val onlyOnUseKey by boolean("OnlyOnUseKey", true)
        .describe("Only aim while you hold right click with a pearl in hand.")
    private val reachableCheck by boolean("ReachableCheck", true)
        .describe("Skip aiming when the looked at spot is out of pearl range.")
    private val maxDistance by float("MaxDistance", 60F, 8F..256F)
        .describe("How far ahead to search for the spot you are looking at.")

    private val minPitch by float("MinPitch", -90F, -90F..0F)
        .describe("Lowest launch pitch to try when solving the throw.")
    private val maxPitch by float("MaxPitch", 0F, -90F..90F)
        .describe("Highest launch pitch to try when solving the throw.")
    private val pitchStep by float("PitchStep", 1F, 0.25F..5F)
        .describe("Pitch increment used while solving the throw.")

    private val maxLandingError by float("MaxLandingError", 2F, 0.5F..10F)
        .describe("Max allowed landing distance from the spot you look at.")
    private val silent by boolean("Silent", true)
        .describe("Aim silently without moving the visible view.")

    private const val MOTION_FACTOR = 1.5F
    private const val GRAVITY = 0.03
    private const val AIR_SLOWDOWN = 0.99
    private const val WATER_SLOWDOWN = 0.8

    private val options = RotationSettings(this).withoutKeepRotation().withRequestPriority(RotationPriority.HIGH)

    init {
        group("Target", "OnlyOnUseKey", "ReachableCheck", "MaxDistance")
        group("Pitch", "MinPitch", "MaxPitch", "PitchStep")
        group("Throw", "MaxLandingError", "Silent")
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.heldItem?.item !is ItemEnderPearl)
            return@handler

        if (onlyOnUseKey && !mc.gameSettings.keyBindUseItem.isKeyDown)
            return@handler

        val targetPosition = getLookedAtPosition(player) ?: return@handler

        val rotation = solveThrow(player, targetPosition) ?: return@handler

        if (silent) {
            setTargetRotation(rotation.fixedSensitivity(), options)
        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch
        }
    }

    private fun getLookedAtPosition(player: Entity): Vec3? {
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val look = player.getLook(1F)
        val reach = maxDistance.toDouble()
        val end = Vec3(
            eyes.xCoord + look.xCoord * reach,
            eyes.yCoord + look.yCoord * reach,
            eyes.zCoord + look.zCoord * reach
        )

        val result = world.rayTraceBlocks(eyes, end, false, true, false) ?: return null
        return result.hitVec
    }

    private fun solveThrow(player: Entity, aimPoint: Vec3): Rotation? {
        val eyes = player.eyes

        val diffX = aimPoint.xCoord - eyes.xCoord
        val diffZ = aimPoint.zCoord - eyes.zCoord
        val yaw = MathHelper.wrapAngleTo180_float(atan2(diffZ, diffX).toDegreesF() - 90F)

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

        val chosenPitch = bestPitch ?: return null

        if (reachableCheck && bestError > maxLandingError * maxLandingError)
            return null

        return Rotation(yaw, chosenPitch.coerceIn(-90F, 90F))
    }

    private fun simulateClosestApproach(player: Entity, yaw: Float, pitch: Float, aimPoint: Vec3): Double {
        val world = mc.theWorld ?: return Double.MAX_VALUE

        val yawRadians = yaw.toRadians().toDouble()
        val pitchRadians = pitch.toRadians().toDouble()

        var posX = player.posX - cos(yawRadians) * 0.16
        var posY = player.entityBoundingBox.minY + player.eyeHeight - 0.10000000149011612
        var posZ = player.posZ - sin(yawRadians) * 0.16

        var motionX = -sin(yawRadians) * cos(pitchRadians) * 0.4
        var motionY = -sin(pitchRadians) * 0.4
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
