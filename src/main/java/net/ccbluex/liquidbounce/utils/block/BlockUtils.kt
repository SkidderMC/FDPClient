/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.immutableCopy
import net.minecraft.block.Block
import net.minecraft.block.BlockGlass
import net.minecraft.block.BlockSoulSand
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation

typealias Collidable = (Block?) -> Boolean

object BlockUtils : MinecraftInstance {

    /**
     * Get block name by [id]
     */
    fun getBlockName(id: Int): String = Block.getBlockById(id).localizedName

    /**
     * Check if block bounding box is full or partial (non-full)
     */
    fun isBlockBBValid(
        blockPos: BlockPos,
        blockState: IBlockState? = null,
        supportSlabs: Boolean = false,
        supportPartialBlocks: Boolean = false
    ): Boolean {
        val state = blockState ?: blockPos.state ?: return false

        val box = state.block.getCollisionBoundingBox(mc.theWorld, blockPos, state) ?: return false

        // Support blocks like stairs, slab (1x), dragon-eggs, glass-panes, fences, etc
        if (supportPartialBlocks && (box.maxY - box.minY < 1.0 || box.maxX - box.minX < 1.0 || box.maxZ - box.minZ < 1.0)) {
            return true
        }

        // The slab will only return true if it's placed at a level that can be placed like any normal full block
        return box.maxX - box.minX == 1.0 && (box.maxY - box.minY == 1.0 || supportSlabs && box.maxY % 1.0 == 0.0) && box.maxZ - box.minZ == 1.0
    }

    fun isFullBlock(block: Block): Boolean {
        when (block) {
            // Soul Sand is considered as full block?!
            is BlockSoulSand -> return false

            // Glass isn't considered as full block?!
            is BlockGlass, is BlockStainedGlass -> return true
        }

        // Many translucent or non-full blocks have blockBounds set to 1.0
        return block.isFullBlock && block.isBlockNormalCube &&
                block.blockBoundsMaxX == 1.0 && block.blockBoundsMaxY == 1.0 && block.blockBoundsMaxZ == 1.0
    }

    /**
     * Get distance to center of [blockPos]
     */
    fun getCenterDistance(blockPos: BlockPos) =
        mc.thePlayer.getDistance(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)

    /**
     * Search a limited amount [maxBlocksLimit] of specific blocks [targetBlocks] around the player in a specific [radius].
     * If [targetBlocks] is null it searches every block
     **/
    fun searchBlocks(
        radius: Int,
        targetBlocks: Set<Block>? = null,
        maxBlocksLimit: Int? = null,
        predicate: (BlockPos, Block) -> Boolean = { _, _ -> true }
    ): MutableMap<BlockPos, Block> {
        val thePlayer = mc.thePlayer ?: return mutableMapOf()

        val blocks = mutableMapOf<BlockPos, Block>()

        val mutable = BlockPos.MutableBlockPos(0, 0, 0)
        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    if (maxBlocksLimit != null && blocks.size >= maxBlocksLimit) {
                        return blocks
                    }

                    mutable.set(thePlayer.posX.toInt() + x, thePlayer.posY.toInt() + y, thePlayer.posZ.toInt() + z)

                    val block = mutable.block ?: continue

                    if (targetBlocks == null || targetBlocks.contains(block)) {
                        val pos = mutable.immutableCopy()
                        if (predicate(pos, block)) {
                            blocks[pos] = block
                        }
                    }
                }
            }
        }

        return blocks
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    fun collideBlock(axisAlignedBB: AxisAlignedBB, collide: Collidable): Boolean {
        val thePlayer = mc.thePlayer

        val y = axisAlignedBB.minY.toInt()
        val mutable = BlockPos.MutableBlockPos(0, 0, 0)
        for (x in thePlayer.entityBoundingBox.minX.toInt() until thePlayer.entityBoundingBox.maxX.toInt() + 1) {
            for (z in thePlayer.entityBoundingBox.minZ.toInt() until thePlayer.entityBoundingBox.maxZ.toInt() + 1) {
                val blockPos = mutable.set(x, y, z)
                val block = blockPos.block

                if (!collide(block))
                    return false
            }
        }

        return true
    }

    /**
     * Check if [axisAlignedBB] has collidable blocks using custom [collide] check
     */
    fun collideBlockIntersects(axisAlignedBB: AxisAlignedBB, collide: Collidable): Boolean {
        val thePlayer = mc.thePlayer
        val world = mc.theWorld

        val y = axisAlignedBB.minY.toInt()
        val mutable = BlockPos.MutableBlockPos(0, 0, 0)
        for (x in thePlayer.entityBoundingBox.minX.toInt() until thePlayer.entityBoundingBox.maxX.toInt() + 1) {
            for (z in thePlayer.entityBoundingBox.minZ.toInt() until thePlayer.entityBoundingBox.maxZ.toInt() + 1) {
                val blockPos = mutable.set(x, y, z)
                val block = blockPos.block

                if (collide(block)) {
                    val boundingBox = blockPos.state?.let { block?.getCollisionBoundingBox(world, blockPos, it) }
                        ?: continue

                    if (thePlayer.entityBoundingBox.intersectsWith(boundingBox))
                        return true
                }
            }
        }
        return false
    }

    /**
     * Bedwars Blocks List
     */
    val BEDWARS_BLOCKS = setOf(
        Blocks.wool,
        Blocks.stained_hardened_clay,
        Blocks.stained_glass,
        Blocks.planks,
        Blocks.log,
        Blocks.log2,
        Blocks.end_stone,
        Blocks.obsidian,
        Blocks.water
    )

    /**
     * Bedwars Blocks Texture List
     */
    fun getBlockTexture(block: Block): ResourceLocation {
        return when (block) {
            Blocks.bed -> ResourceLocation("minecraft:textures/items/bed.png")
            Blocks.obsidian -> ResourceLocation("minecraft:textures/blocks/obsidian.png")
            Blocks.end_stone -> ResourceLocation("minecraft:textures/blocks/end_stone.png")
            Blocks.stained_hardened_clay -> ResourceLocation("minecraft:textures/blocks/hardened_clay_stained_white.png")
            Blocks.stained_glass -> ResourceLocation("minecraft:textures/blocks/glass.png")
            Blocks.water -> ResourceLocation("minecraft:textures/blocks/water_still.png")
            Blocks.planks -> ResourceLocation("minecraft:textures/blocks/planks_oak.png")
            Blocks.wool -> ResourceLocation("minecraft:textures/blocks/wool_colored_white.png")
            else -> ResourceLocation("minecraft:textures/blocks/stone.png")
        }
    }
}