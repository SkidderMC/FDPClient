/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockBed
import net.minecraft.block.BlockSlime
import net.minecraft.util.AxisAlignedBB

/**
 * BlockBounce
 *
 * Gives an extra upward boost when jumping off bouncy blocks.
 */
object BlockBounce : Module("BlockBounce", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    private val motion by float("Motion", 0.42f, 0.2f..2f)
        .describe("Extra upward motion added when bouncing off a block.")

    private val slime by boolean("Slime", true)
        .describe("Bounce when standing on slime blocks.")
    private val bed by boolean("Bed", true)
        .describe("Bounce when standing on beds.")

    val onJump = handler<JumpEvent> { event ->
        if (standingOnBouncyBlock())
            event.motion += motion
    }

    private fun standingOnBouncyBlock(): Boolean {
        val thePlayer = mc.thePlayer ?: return false

        val bb = thePlayer.entityBoundingBox
        val feetBox = AxisAlignedBB(
            bb.minX,
            bb.minY - 0.01,
            bb.minZ,
            bb.maxX,
            bb.minY,
            bb.maxZ
        )

        return collideBlock(feetBox) { matches(it) }
    }

    private fun matches(block: Block?): Boolean = when (block) {
        is BlockSlime -> slime
        is BlockBed -> bed
        else -> false
    }
}
