/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.util.AxisAlignedBB

object BlockWalk : Module("BlockWalk", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    private val water by boolean("Water", true)
    private val lava by boolean("Lava", true)

    private fun matches(block: Block?): Boolean {
        if (block !is BlockLiquid) return false
        val material = block.material
        return (water && material == Material.water) || (lava && material == Material.lava)
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (thePlayer.isSneaking || !(water || lava)) return@handler

        // Only treat the liquid as solid while the player is not submerged, so the
        // motion is zeroed at the surface instead of while sinking.
        if (!thePlayer.isInsideOfMaterial(Material.air)) return@handler

        val bb = thePlayer.entityBoundingBox
        val feetBox = AxisAlignedBB(
            bb.minX,
            bb.minY - 0.01,
            bb.minZ,
            bb.maxX,
            bb.minY,
            bb.maxZ
        )

        if (collideBlock(feetBox) { matches(it) }) {
            if (thePlayer.motionY < 0.0) {
                thePlayer.motionY = 0.0
            }
            thePlayer.onGround = true
            thePlayer.fallDistance = 0f
        }
    }
}
