/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.isReplaceable
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

/**
 * Module HoleESP
 *
 * Highlights nearby safe spots (1x1 and 1x2 holes surrounded by solid blocks)
 * so you can quickly find a place to stand. Render-only.
 */
object HoleESP : Module("HoleESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val horizontalRange by int("HorizontalRange", 16, 1..48)
        .describe("Horizontal scan radius for holes in blocks.")
    private val verticalRange by int("VerticalRange", 4, 1..16)
        .describe("Vertical scan radius for holes in blocks.")

    // Require the hole to be at least 2 blocks deep so the player can actually drop in.
    private val minDepth by int("MinDepth", 2, 1..3)
        .describe("Minimum hole depth required to count as a spot.")

    // Detect 1x2 / 2x1 holes in addition to 1x1.
    private val detectDouble by boolean("Detect1x2", true)
        .describe("Also detect 1x2 and 2x1 holes.")

    private val onlyBedrock by boolean("OnlyBedrock", false)
        .describe("Only highlight holes surrounded by bedrock.")
    private val outline by boolean("Outline", true)
        .describe("Draw an outline around each hole box.")

    private val color1by1 by color("Color1x1", Color(247, 56, 27, 120))
        .describe("Color for 1x1 holes.")
    private val colorBedrock by color("ColorBedrock", Color(25, 193, 92, 120))
        .describe("Color for bedrock-surrounded holes.")
    private val color1by2 by color("Color1x2", Color(53, 186, 204, 120))
        .describe("Color for 1x2 holes.")

    private val maxHoles by int("MaxHoles", 128, 1..512)
        .describe("Maximum number of holes to render at once.")

    private val horizontalFacings = EnumFacing.values().filter { it.axis != EnumFacing.Axis.Y }

    private val holes = ArrayList<Hole>()

    override fun onDisable() {
        holes.clear()
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        holes.clear()

        val feet = BlockPos(player.posX, player.posY, player.posZ)
        val claimed = HashSet<BlockPos>()

        loop@ for (x in -horizontalRange..horizontalRange) {
            for (z in -horizontalRange..horizontalRange) {
                for (y in -verticalRange..verticalRange) {
                    if (holes.size >= maxHoles) {
                        break@loop
                    }

                    val pos = feet.add(x, y, z)

                    if (pos in claimed) {
                        continue
                    }

                    val hole = detectHole(pos, claimed) ?: continue

                    holes.add(hole)
                    hole.positions.forEach { claimed.add(it) }
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        for (hole in holes) {
            for (pos in hole.positions) {
                drawBlockBox(pos, hole.color, outline)
            }
        }
    }

    /**
     * Returns a [Hole] starting at [pos] if it qualifies as a standable safe spot, otherwise null.
     * A qualifying spot is an open column (deep enough) whose horizontal sides are solid.
     */
    private fun detectHole(pos: BlockPos, claimed: Set<BlockPos>): Hole? {
        if (!isOpenColumn(pos)) {
            return null
        }

        val sideBedrock = horizontalFacings.map { pos.offset(it) }

        // All four horizontal sides solid -> 1x1 hole.
        if (sideBedrock.all { !it.isReplaceable }) {
            val bedrockOnly = sideBedrock.all { it.block == Blocks.bedrock } &&
                pos.down().block == Blocks.bedrock

            if (onlyBedrock && !bedrockOnly) {
                return null
            }

            return Hole(listOf(pos), if (bedrockOnly) colorBedrock else color1by1)
        }

        if (!detectDouble || onlyBedrock) {
            return null
        }

        // Try to extend into a 1x2 / 2x1 hole: exactly one open horizontal neighbour,
        // and that neighbour must itself be a deep open column with the rest of its sides solid.
        val openSides = horizontalFacings.filter { pos.offset(it).isReplaceable }

        if (openSides.size != 1) {
            return null
        }

        val facing = openSides.first()
        val other = pos.offset(facing)

        if (other in claimed || !isOpenColumn(other)) {
            return null
        }

        val solidForPos = horizontalFacings.all { it == facing || !pos.offset(it).isReplaceable }
        val solidForOther = horizontalFacings.all { it == facing.opposite || !other.offset(it).isReplaceable }

        if (!solidForPos || !solidForOther) {
            return null
        }

        return Hole(listOf(pos, other), color1by2)
    }

    /**
     * The column at [pos] is open if the spot and the block above (player height) are replaceable
     * and it is at least [minDepth] blocks deep downward, so it is a real hole, not flat ground.
     */
    private fun isOpenColumn(pos: BlockPos): Boolean {
        if (!pos.isReplaceable || !pos.up().isReplaceable) {
            return false
        }

        // pos itself counts as the first depth level; check the rest below it.
        for (depth in 1 until minDepth) {
            if (!pos.down(depth).isReplaceable) {
                return false
            }
        }

        return true
    }

    private class Hole(val positions: List<BlockPos>, val color: Color)
}
