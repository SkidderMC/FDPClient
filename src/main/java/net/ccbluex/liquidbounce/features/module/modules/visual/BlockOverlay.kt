/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.animations.AnimationUtil
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawSelectionBoundingBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.minecraft.block.Block
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object BlockOverlay : Module("BlockOverlay", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {
    private val mode by choices("Mode", arrayOf("Box", "OtherBox", "Outline"), "Box")
        .describe("Render style for the targeted block.")
    private val sideOnly by boolean("SideOnly", false)
        .describe("Only highlight the face you are looking at.")
    private val depth3D by boolean("Depth3D", false)
        .describe("Render the overlay through walls.")
    private val thickness by float("Thickness", 2F, 1F..5F)
        .describe("Line thickness of the overlay.")

    val info by boolean("Info", false)
        .describe("Show the block name and ID on screen.")

    private val color by color("Color", Color(68, 117, 255, 100))
        .describe("Fill color of the block overlay.")

    private val separateOutlineColor by boolean("SeparateOutlineColor", false)
        .describe("Use a separate color for the outline.")
    private val outlineColor by color("OutlineColor", Color(68, 117, 255, 150)) { separateOutlineColor }
        .describe("Color of the overlay outline.")

    private val slideAnim by boolean("Slide", false)
        .describe("Animate the overlay sliding between blocks.")
    private val slideEasing by choices("SlideEasing", arrayOf("Linear", "Quad", "Expo"), "Linear") { slideAnim }
        .describe("Easing curve for the slide animation.")
    private val slideTime by int("SlideTime", 150, 1..1000) { slideAnim }
        .describe("Duration of the slide animation in ms.")

    private var currentBox: AxisAlignedBB? = null
    private var previousBox: AxisAlignedBB? = null
    private var lastChange = 0L

    val currentBlock: BlockPos?
        get() {
            val world = mc.theWorld ?: return null
            val blockPos = mc.objectMouseOver?.blockPos ?: return null

            if (blockPos.block !in arrayOf(
                    Blocks.air,
                    Blocks.water,
                    Blocks.lava
                ) && world.worldBorder.contains(blockPos)
            )
                return blockPos

            return null
        }

    val onRender3D = handler<Render3DEvent> {
        val blockPos = currentBlock ?: return@handler

        val block = blockPos.block ?: return@handler

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glColor(color)
        glLineWidth(thickness)
        glDisable(GL_TEXTURE_2D)
        if (depth3D) glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

        val thePlayer = mc.thePlayer ?: return@handler

        val pos = thePlayer.interpolatedPosition(thePlayer.lastTickPos)

        val f = 0.002F.toDouble()

        var worldBox = block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(f, f, f)

        if (sideOnly) {
            val side = mc.objectMouseOver?.sideHit
            if (side != null) worldBox = flatBox(worldBox, side)
        }

        if (worldBox != currentBox) {
            previousBox = currentBox
            currentBox = worldBox
            lastChange = System.currentTimeMillis()
        }

        val renderBox = if (slideAnim && previousBox != null) {
            lerpBox(previousBox!!, worldBox, slideFactor())
        } else {
            worldBox
        }

        val axisAlignedBB = renderBox.offset(-pos)

        if (mode.lowercase() in arrayOf("box", "otherbox"))
            drawFilledBox(axisAlignedBB)
        if (mode.lowercase() in arrayOf("box", "outline")) {
            if (separateOutlineColor) glColor(outlineColor)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        if (depth3D) glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDepthMask(true)
        resetColor()
    }

    val onRender2D = handler<Render2DEvent> {
        if (!info) return@handler

        val blockPos = currentBlock ?: return@handler
        val block = blockPos.block ?: return@handler

        val info = "${block.localizedName} §7ID: ${Block.getIdFromBlock(block)}"
        val (width, height) = ScaledResolution(mc)

        drawBorderedRect(
            width / 2 - 2F,
            height / 2 + 5F,
            width / 2 + Fonts.fontSemibold40.getStringWidth(info) + 2F,
            height / 2 + 16F,
            3F, Color.BLACK.rgb, Color.BLACK.rgb
        )

        resetColor()
        Fonts.fontSemibold40.drawString(info, width / 2f, height / 2f + 7f, Color.WHITE.rgb, false)
    }

    private fun slideFactor(): Double {
        val raw = ((System.currentTimeMillis() - lastChange).toDouble() / slideTime).coerceIn(0.0, 1.0)
        return when (slideEasing.lowercase()) {
            "quad" -> AnimationUtil.easeInOutQuadX(raw)
            "expo" -> AnimationUtil.easeInOutExpo(raw)
            else -> raw
        }.coerceIn(0.0, 1.0)
    }

    private fun lerpBox(from: AxisAlignedBB, to: AxisAlignedBB, factor: Double) = AxisAlignedBB(
        from.minX + (to.minX - from.minX) * factor,
        from.minY + (to.minY - from.minY) * factor,
        from.minZ + (to.minZ - from.minZ) * factor,
        from.maxX + (to.maxX - from.maxX) * factor,
        from.maxY + (to.maxY - from.maxY) * factor,
        from.maxZ + (to.maxZ - from.maxZ) * factor
    )

    private fun flatBox(box: AxisAlignedBB, side: EnumFacing) = when (side) {
        EnumFacing.UP -> AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ)
        EnumFacing.DOWN -> AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ)
        EnumFacing.NORTH -> AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ)
        EnumFacing.SOUTH -> AxisAlignedBB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ)
        EnumFacing.WEST -> AxisAlignedBB(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ)
        EnumFacing.EAST -> AxisAlignedBB(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
    }
}