/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.utils.ClientUtils.mc
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.Block
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

/**
 * Get block by position
 */
fun BlockPos.getBlock() = BlockUtils.getBlock(this)

/**
 * Get vector of block position
 */
fun BlockPos.getVec() = Vec3(x + 0.5, y + 0.5, z + 0.5)

fun BlockPos.getMaterial() = getBlock()?.material

fun BlockPos.isFullBlock() = getBlock()?.isFullBlock

fun BlockPos.isReplaceable() = getMaterial()?.isReplaceable ?: false

fun BlockPos.getCenterDistance() = mc.thePlayer.getDistance(x + 0.5, y + 0.5, z + 0.5)

/**
 * Creates an [AxisAlignedBB] cube with values 1, 1, 1
 *
 * @param x X position
 * @param y Y position
 * @param z Z position
 */
fun AxisAlignedBB(x: Int, y: Int, z: Int): AxisAlignedBB {
    return AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)
}

fun AxisAlignedBB.down(height: Double): AxisAlignedBB {
    return AxisAlignedBB(minX, minY, minZ, maxX, maxY - height, maxZ)
}

/**
 * Returns blocks that are in that radius
 *
 * @param radius Radius
 */
fun searchBlocks(radius: Int): Map<BlockPos, Block> {
    val blocks = mutableMapOf<BlockPos, Block>()

    for (x in radius downTo -radius + 1)
        for (y in radius downTo -radius + 1)
            for (z in radius downTo -radius + 1) {
                val blockPos = BlockPos(mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y, mc.thePlayer.posZ.toInt() + z)
                val block = blockPos.getBlock() ?: continue

                blocks[blockPos] = block
            }

    return blocks
}

fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

inline fun collideBlock(aabb: AxisAlignedBB, predicate: (Block?) -> Boolean): Boolean {
    for (x in MathHelper.floor_double(aabb.minX) until MathHelper.floor_double(aabb.maxX) + 1) {
        for (z in MathHelper.floor_double(aabb.minZ) until MathHelper.floor_double(aabb.maxZ) + 1) {
            val block = BlockPos(x.toDouble(), aabb.minY, z.toDouble()).getBlock()

            if (!predicate(block)) return false
        }
    }

    return true
}

