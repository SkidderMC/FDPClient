/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import kotlin.random.Random

enum class FaceTargetMode {
    CENTER,
    NEAREST,
    STABILIZED,
    RANDOM
}

/** Creates a precise, edge-trimmed hit point on one face of a unit block. */
class FaceTargetPositionFactory(
    val mode: FaceTargetMode = FaceTargetMode.CENTER,
    edgeInset: Double = 0.15
) {
    val edgeInset: Double = edgeInset.also {
        require(it.isFinite() && it in 0.0..0.49) { "Face edge inset must be between 0 and 0.49" }
    }

    fun create(
        block: BlockPos,
        face: EnumFacing,
        reference: Vec3,
        previousTarget: Vec3? = null,
        random: Random = Random.Default
    ): Vec3 {
        val minX = block.x + edgeInset
        val minY = block.y + edgeInset
        val minZ = block.z + edgeInset
        val maxX = block.x + 1.0 - edgeInset
        val maxY = block.y + 1.0 - edgeInset
        val maxZ = block.z + 1.0 - edgeInset

        val source = if (mode == FaceTargetMode.STABILIZED) previousTarget ?: reference else reference
        val x = coordinate(mode, source.xCoord, minX, maxX, random)
        val y = coordinate(mode, source.yCoord, minY, maxY, random)
        val z = coordinate(mode, source.zCoord, minZ, maxZ, random)

        return when (face) {
            EnumFacing.DOWN -> Vec3(x, block.y.toDouble(), z)
            EnumFacing.UP -> Vec3(x, block.y + 1.0, z)
            EnumFacing.NORTH -> Vec3(x, y, block.z.toDouble())
            EnumFacing.SOUTH -> Vec3(x, y, block.z + 1.0)
            EnumFacing.WEST -> Vec3(block.x.toDouble(), y, z)
            EnumFacing.EAST -> Vec3(block.x + 1.0, y, z)
        }
    }

    private fun coordinate(mode: FaceTargetMode, reference: Double, lower: Double, upper: Double, random: Random): Double =
        when (mode) {
            FaceTargetMode.CENTER -> (lower + upper) * 0.5
            FaceTargetMode.NEAREST, FaceTargetMode.STABILIZED -> reference.coerceIn(lower, upper)
            FaceTargetMode.RANDOM -> random.nextDouble(lower, upper)
        }
}
