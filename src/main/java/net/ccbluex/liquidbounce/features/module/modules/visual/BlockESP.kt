/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.component3
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.renderPos
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderColor.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.ccbluex.liquidbounce.utils.render.RenderColor.resetColor
import net.minecraft.block.Block
import net.minecraft.init.Blocks.air
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_LINE_SMOOTH
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glDepthMask
import org.lwjgl.opengl.GL11.glLineWidth
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object BlockESP : Module("BlockESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val mode by choices("Mode", arrayOf("Box", "2D"), "Box")
        .describe("Render style for highlighted blocks.")
    private val block by block("Block", 168)
        .describe("Which block type to highlight.")
    private val radius by int("Radius", 40, 5..120)
        .describe("Search radius for matching blocks.")
    private val blockLimit by int("BlockLimit", 256, 0..2056)
        .describe("Maximum number of blocks to highlight.")

    private val outline by boolean("Outline", true) { mode == "Box" }
        .describe("Draw an outline around each box.")
    private val mergeAdjacent by boolean("MergeAdjacent", false) { mode == "Box" }
        .describe("Merge neighboring blocks into one box.")

    private val color by color("Color", Color(255, 179, 72))
        .describe("Color of the highlighted blocks.")

    private val posList = ConcurrentHashMap.newKeySet<BlockPos>()

    override fun onDisable() {
        posList.clear()
    }

    val onSearch = loopSequence(dispatcher = Dispatchers.Default) {
        val selectedBlock = Block.getBlockById(block)

        if (selectedBlock == null || selectedBlock == air) {
            delay(1000)
            return@loopSequence
        }

        val (x, y, z) = mc.thePlayer?.eyes ?: return@loopSequence
        val radiusSq = radius * radius

        posList.removeIf {
            it.distanceSqToCenter(x, y, z) >= radiusSq || it.block != selectedBlock
        }

        val listSpace = blockLimit - posList.size

        if (listSpace > 0) {
            posList += searchBlocks(radius, setOf(selectedBlock), listSpace).keys
        }

        delay(1000)
    }

    val onRender3D = handler<Render3DEvent> {
        when (mode) {
            "Box" -> if (mergeAdjacent) {
                drawMergedBoxes()
            } else {
                posList.forEach { drawBlockBox(it, color, outline) }
            }
            "2D" -> posList.forEach { draw2D(it, color.rgb, Color.BLACK.rgb) }
        }
    }

    private fun drawMergedBoxes() {
        val boxes = mergeBoxes(posList) ?: return
        val renderPos = mc.renderManager.renderPos

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)

        for (box in boxes) {
            val rendered = box.offset(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord)

            glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
            Render3D.drawFilledBox(rendered)

            if (outline) {
                glLineWidth(1f)
                enableGlCap(GL_LINE_SMOOTH)
                glColor(color)
                Render3D.drawSelectionBoundingBox(rendered)
            }
        }

        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    private fun mergeBoxes(positions: Collection<BlockPos>): List<AxisAlignedBB>? {
        if (positions.isEmpty()) {
            return null
        }

        val remaining = HashSet(positions)
        val result = ArrayList<AxisAlignedBB>()

        while (remaining.isNotEmpty()) {
            val start = remaining.first()
            var minX = start.x
            var minY = start.y
            var minZ = start.z
            var maxX = start.x
            var maxY = start.y
            var maxZ = start.z
            remaining -= start

            var grown = true
            while (grown) {
                grown = false

                if (tryGrow(remaining, minX - 1, minX - 1, minY, maxY, minZ, maxZ)) {
                    minX--; grown = true
                }
                if (tryGrow(remaining, maxX + 1, maxX + 1, minY, maxY, minZ, maxZ)) {
                    maxX++; grown = true
                }
                if (tryGrow(remaining, minX, maxX, minY - 1, minY - 1, minZ, maxZ)) {
                    minY--; grown = true
                }
                if (tryGrow(remaining, minX, maxX, maxY + 1, maxY + 1, minZ, maxZ)) {
                    maxY++; grown = true
                }
                if (tryGrow(remaining, minX, maxX, minY, maxY, minZ - 1, minZ - 1)) {
                    minZ--; grown = true
                }
                if (tryGrow(remaining, minX, maxX, minY, maxY, maxZ + 1, maxZ + 1)) {
                    maxZ++; grown = true
                }
            }

            result += AxisAlignedBB.fromBounds(
                minX.toDouble(), minY.toDouble(), minZ.toDouble(),
                (maxX + 1).toDouble(), (maxY + 1).toDouble(), (maxZ + 1).toDouble()
            )
        }

        return result
    }

    private fun tryGrow(
        remaining: HashSet<BlockPos>,
        minX: Int, maxX: Int, minY: Int, maxY: Int, minZ: Int, maxZ: Int
    ): Boolean {
        for (bx in minX..maxX) {
            for (by in minY..maxY) {
                for (bz in minZ..maxZ) {
                    if (BlockPos(bx, by, bz) !in remaining) {
                        return false
                    }
                }
            }
        }
        for (bx in minX..maxX) {
            for (by in minY..maxY) {
                for (bz in minZ..maxZ) {
                    remaining -= BlockPos(bx, by, bz)
                }
            }
        }
        return true
    }

    override val tag
        get() = getBlockName(block)
}
