/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.currPos
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.utils.extensions.prevPos
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.point.exempts.ExemptBestHitVector
import net.ccbluex.liquidbounce.utils.rotation.point.exempts.ExemptBoxPart
import net.ccbluex.liquidbounce.utils.rotation.point.exempts.ExemptContext
import net.ccbluex.liquidbounce.utils.rotation.point.features.PointProcessorDelay
import net.ccbluex.liquidbounce.utils.rotation.point.features.PointProcessorGaussian
import net.ccbluex.liquidbounce.utils.rotation.point.features.PointProcessorLazy
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import kotlin.math.abs

class PointTracker(name: String = "AimPoint") : Configurable(name), MinecraftInstance {

    private val exemptBoxParts = multiSelect("ExemptBoxParts", ExemptBoxPart.tags)
    private val exemptBestHitVector = ExemptBestHitVector()

    private val gaussian = PointProcessorGaussian()
    private val lazy = PointProcessorLazy()
    private val delay = PointProcessorDelay()

    private val extrapolation by float("Extrapolation", 0f, 0f..5f, "ticks")

    private val resolution by int("Resolution", 4, 1..8)

    private val processors = arrayOf(delay, lazy, gaussian)

    init {
        +exemptBestHitVector
        +delay
        +lazy
        +gaussian
    }

    fun findPoint(eyes: Vec3, entity: Entity, ticks: Double = extrapolation.toDouble()): PointInsideBox {
        val box = extrapolatedBox(entity, ticks)
        val points = box.surfacePoints(resolution)

        val bestHitVector = points.minByOrNull { it.squareDistanceTo(eyes) } ?: getNearestPointBB(eyes, box)
        val worstHitVector = points.maxByOrNull { it.squareDistanceTo(eyes) } ?: pseudoFurthest(eyes, box)

        val context = ExemptContext(box, bestHitVector, worstHitVector)
        val selectedParts = ExemptBoxPart.tags
            .filter { exemptBoxParts.isSelected(it) }
            .mapNotNull { ExemptBoxPart.fromTag(it) }

        val allowed = points.filter { point ->
            selectedParts.none { it.predicate(context, point) } &&
                !exemptBestHitVector.predicate(context, point)
        }

        val chosen = allowed.minByOrNull { it.squareDistanceTo(eyes) } ?: bestHitVector

        var point = PointInsideBox.snapped(chosen, box)
        for (processor in processors) {
            if (processor.enabled) {
                point = processor.process(point)
            }
        }
        return point
    }

    fun findBestPoint(entity: Entity, eyes: Vec3, currentRotation: Rotation? = null): Vec3? {
        if (mc.theWorld == null || mc.thePlayer == null) {
            return null
        }

        val box = extrapolatedBox(entity, extrapolation.toDouble())
        if (box.isVecInside(eyes)) {
            return null
        }

        val point = findPoint(eyes, entity).pos

        if (currentRotation != null && !RotationUtils.isVisible(point)) {
            val fallback = getNearestPointBB(eyes, box)
            if (RotationUtils.isVisible(fallback)) {
                return fallback
            }
        }

        return point
    }

    private fun extrapolatedBox(entity: Entity, ticks: Double): AxisAlignedBB {
        val box = entity.entityBoundingBox
        if (ticks <= 0.0) {
            return box
        }

        val motion = entity.currPos.subtract(entity.prevPos)
        return box.offset(motion.xCoord * ticks, motion.yCoord * ticks, motion.zCoord * ticks)
    }

    private fun pseudoFurthest(eyes: Vec3, box: AxisAlignedBB): Vec3 = Vec3(
        farthestAxis(eyes.xCoord, box.minX, box.maxX),
        farthestAxis(eyes.yCoord, box.minY, box.maxY),
        farthestAxis(eyes.zCoord, box.minZ, box.maxZ)
    )

    private fun farthestAxis(value: Double, min: Double, max: Double): Double {
        val distToMin = abs(value - min)
        val distToMax = abs(value - max)
        return if (distToMin > distToMax) min else max
    }

    private fun AxisAlignedBB.surfacePoints(steps: Int): List<Vec3> {
        val points = ArrayList<Vec3>()
        val divisions = steps.coerceAtLeast(1)

        for (ix in 0..divisions) {
            val fx = ix.toDouble() / divisions
            val x = minX + (maxX - minX) * fx
            for (iy in 0..divisions) {
                val fy = iy.toDouble() / divisions
                val y = minY + (maxY - minY) * fy
                for (iz in 0..divisions) {
                    val fz = iz.toDouble() / divisions
                    val onShellX = ix == 0 || ix == divisions
                    val onShellY = iy == 0 || iy == divisions
                    val onShellZ = iz == 0 || iz == divisions
                    if (!onShellX && !onShellY && !onShellZ) {
                        continue
                    }

                    val z = minZ + (maxZ - minZ) * fz
                    points.add(Vec3(x, y, z))
                }
            }
        }

        return points
    }
}
