/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils.BEDWARS_BLOCKS
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockTexture
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.getAllInBoxMutable
import net.ccbluex.liquidbounce.utils.block.set
import net.ccbluex.liquidbounce.utils.extensions.immutableCopy
import net.ccbluex.liquidbounce.utils.extensions.manhattanDistance
import net.ccbluex.liquidbounce.utils.extensions.offset
import net.ccbluex.liquidbounce.utils.render.ColorSettingsFloat
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.utils.render.toColorArray
import net.minecraft.block.Block
import net.minecraft.block.BlockBed
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

object BedPlates : Module("BedPlates", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {
    private val renderYOffset by float("RenderYOffset", 1f, -5f..5f)
        .describe("Vertical offset of the plate above the bed.")

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }
        .describe("Maximum distance at which plates are drawn.")

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2) else value
        }

    private val maxLayers by int("MaxLayers", 5, 1..10)
        .describe("How many surrounding block layers to count.")
    private val maxCount by int("MaxCount", 64, 1..64)
        .describe("Maximum number of beds to render at once.")
    private val scale by float("Scale", 3F, 1F..5F)
        .describe("Overall size of the rendered plate.")

    private val compact by boolean("Compact", false)
        .describe("Use a smaller, more compact plate layout.")
    private val showBed by boolean("ShowBed", true)
        .describe("Show the bed icon on the plate.")
    private val highlightUnbreakable by boolean("HighlightUnbreakable", false)
        .describe("Tint obsidian and end stone icons red.")
    private val ignoreAdjacent by boolean("IgnoreAdjacent", false)
        .describe("Skip beds that are directly next to another.")
    private val preventOverlap by boolean("PreventOverlap", false)
        .describe("Avoid drawing overlapping plates.")
    private val outline by boolean("Outline", false)
        .describe("Draw an outline around the plate.")

    private val textMode by choices("Text-ColorMode", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")
        .describe("Coloring style for the plate text.")
    private val textColors = ColorSettingsInteger(this, "TextColor", applyMax = true) { textMode == "Custom" }

    private val gradientTextSpeed by float("Text-Gradient-Speed", 1f, 0.5f..10f) { textMode == "Gradient" }
        .describe("Animation speed of the text gradient.")

    private val maxTextGradientColors by int("Max-Text-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { textMode == "Gradient" }
        .describe("Number of colors used in the text gradient.")
    private val textGradColors = ColorSettingsFloat.create(this, "Text-Gradient")
    { textMode == "Gradient" && it <= maxTextGradientColors }

    private val roundedRectRadius by float("Rounded-Radius", 3F, 0F..5F)
        .describe("Corner radius of the plate background.")

    private val backgroundMode by choices("Background-Mode", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")
        .describe("Coloring style for the plate background.")
    private val backgroundColor by color("BackgroundColor", java.awt.Color(255, 255, 255, 100)) { backgroundMode == "Custom" }
        .describe("Color of the plate background in Custom mode.")

    private val gradientBackgroundSpeed by float("Background-Gradient-Speed", 1f, 0.5f..10f)
    { backgroundMode == "Gradient" }
        .describe("Animation speed of the background gradient.")

    private val maxBackgroundGradientColors by int("Max-Background-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { backgroundMode == "Gradient" }
        .describe("Number of colors used in the background gradient.")
    private val bgGradColors = ColorSettingsFloat.create(this, "Background-Gradient")
    { backgroundMode == "Gradient" && it <= maxBackgroundGradientColors }

    private val textFont by font("Font", Fonts.fontSemibold35)
        .describe("Font used for the plate text.")
    private val textShadow by boolean("ShadowText", true)
        .describe("Draw a shadow behind the plate text.")

    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
        .describe("Horizontal scale of the rainbow effect.")
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
        .describe("Vertical scale of the rainbow effect.")
    private val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { backgroundMode == "Gradient" }
        .describe("Horizontal scale of the gradient effect.")
    private val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { backgroundMode == "Gradient" }
        .describe("Vertical scale of the gradient effect.")

    private val renderGroup = Configurable("Render")
    private val textGroup = Configurable("Text")
    private val backgroundGroup = Configurable("Background")

    init {
        moveValues(renderGroup,
            "RenderYOffset", "MaxRenderDistance", "MaxLayers", "MaxCount", "Scale", "Compact",
            "ShowBed", "HighlightUnbreakable", "IgnoreAdjacent", "PreventOverlap", "Outline",
            "Rounded-Radius")

        moveValues(textGroup, "Text-ColorMode", "TextColor", "Text-Gradient-Speed",
            "Max-Text-Gradient-Colors",
            "Text-Gradient1", "Text-Gradient2", "Text-Gradient3", "Text-Gradient4", "Text-Gradient5",
            "Text-Gradient6", "Text-Gradient7", "Text-Gradient8", "Text-Gradient9",
            "Font", "ShadowText")

        moveValues(backgroundGroup, "Background-Mode", "BackgroundColor", "Background-Gradient-Speed",
            "Max-Background-Gradient-Colors",
            "Background-Gradient1", "Background-Gradient2", "Background-Gradient3", "Background-Gradient4",
            "Background-Gradient5", "Background-Gradient6", "Background-Gradient7", "Background-Gradient8",
            "Background-Gradient9",
            "Rainbow-X", "Rainbow-Y", "Gradient-X", "Gradient-Y")

        addValues(listOf(renderGroup, textGroup, backgroundGroup))
    }
    private val bedStates = ConcurrentHashMap<BlockPos, BedState>()

    private data class SurroundingBlock(
        val block: Block,
        val count: Int,
        val layer: Int,
    ) : Comparable<SurroundingBlock> {
        val itemStack = ItemStack(block, count)

        override fun compareTo(other: SurroundingBlock): Int = compareValuesBy(
            this, other,
            { it.layer }, { -it.count }, { it.block.unlocalizedName })
    }

    private data class BedState(
        val pos: Vec3,
        val surrounding: Collection<SurroundingBlock>,
    )

    val onUpdate = loopSequence(dispatcher = Dispatchers.Default) {
        val world = mc.theWorld ?: return@loopSequence

        val searchCenter = mc.thePlayer?.position ?: return@loopSequence

        val radius = maxRenderDistance
        val radiusSq = radius * radius

        // Invalidate blocks
        bedStates.keys.removeIf {
            it.block != Blocks.bed || searchCenter.distanceSq(it) > radiusSq
        }

        val maxLayers = maxLayers

        val from = BlockPos.MutableBlockPos()
        val to = BlockPos.MutableBlockPos()

        searchCenter.getAllInBoxMutable(radius).forEach {
            if (searchCenter.distanceSq(it) > radiusSq)
                return@forEach

            val blockState = world.getBlockState(it)
            if (blockState.block != Blocks.bed || blockState.getValue(BlockBed.PART) != BlockBed.EnumPartType.FOOT)
                return@forEach

            val facing = blockState.getValue(BlockBed.FACING)
            val headPos = it.offset(facing)

            // Invalid Bed
            if (world.getBlockState(headPos).block != Blocks.bed) {
                return@forEach
            }

            val layers = Array(maxLayers) { IdentityHashMap<Block, Int>() }

            from.set(it, -maxLayers - 1, 0, -maxLayers - 1)
            to.set(it, maxLayers + 1, maxLayers + 1, maxLayers + 1)

            for (pos in BlockPos.getAllInBoxMutable(from, to)) {
                val layer = minOf(pos.manhattanDistance(it), pos.manhattanDistance(headPos))
                if (layer > maxLayers) continue

                val block = world.getBlockState(pos).block

                if (block !in BEDWARS_BLOCKS) continue

                val blockCount = layers[layer - 1]
                blockCount[block] = blockCount.getOrDefault(block, 0) + 1
            }

            val surrounding = ArrayList<SurroundingBlock>()
            surrounding += SurroundingBlock(Blocks.bed, layer = 0, count = 1)
            layers.forEachIndexed { index, blockCount ->
                blockCount.forEach { (block, count) ->
                    surrounding += SurroundingBlock(block, layer = index + 1, count = count)
                }
            }
            surrounding.sort()

            bedStates[it.immutableCopy()] = BedState(it.center.offset(facing, 0.5), surrounding)
        }

        delay(1000L)
    }

    val onWorld = handler<WorldEvent> {
        bedStates.clear()
    }

    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler
        if (mc.theWorld == null || bedStates.isEmpty()) return@handler

        val positions = bedStates.keys.toList()

        // Closest beds first so MaxCount keeps the nearest ones
        val ordered = positions.sortedBy { bedStates[it]?.pos?.squareDistanceTo(player.positionVector) ?: Double.MAX_VALUE }

        val drawn = ArrayList<Vec3>()
        var count = 0

        for (pos in ordered) {
            if (count >= maxCount) break

            val state = bedStates[pos] ?: continue

            if (ignoreAdjacent && positions.any { it != pos && it.distanceSq(pos) <= 1.0 }) continue

            if (preventOverlap && drawn.any { it.squareDistanceTo(state.pos) <= 1.0 }) continue

            drawPlate(state)
            drawn += state.pos
            count++
        }
    }

    private fun drawPlate(bedState: BedState) {
        val player = mc.thePlayer ?: return
        val renderManager = mc.renderManager ?: return
        val rotateX = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f

        val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
        val rainbowX = if (rainbowX == 0f) 0f else 1f / rainbowX
        val rainbowY = if (rainbowY == 0f) 0f else 1f / rainbowY

        val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
        val gradientX = if (gradientX == 0f) 0f else 1f / gradientX
        val gradientY = if (gradientY == 0f) 0f else 1f / gradientY

        val distance = bedState.pos.distanceTo(player.positionVector)
        val scale = ((distance / 4F).coerceAtLeast(1.0) / 150F) * scale

        glPushMatrix()

        glEnable(GL_LINE_SMOOTH)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glTranslated(
            bedState.pos.xCoord - renderManager.viewerPosX,
            bedState.pos.yCoord - renderManager.viewerPosY + renderYOffset + 1,
            bedState.pos.zCoord - renderManager.viewerPosZ
        )

        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX * rotateX, 1F, 0F, 0F)
        glScalef(-scale.toFloat(), -scale.toFloat(), scale.toFloat())

        val blocks = bedState.surrounding
            .asSequence()
            .map { it.block }
            .filter { showBed || it != Blocks.bed }
            .distinct()
            .toCollection(linkedSetOf())
        val text = "Bed (${distance.roundToInt()}m)"

        val iconSize = if (compact) 10 else 12
        val iconStep = if (compact) 11 else 13

        var offset = (blocks.size * -iconStep) / 2

        val textWidth = textFont.getStringWidth(text)
        val textHeight = textFont.FONT_HEIGHT

        val rectWidth = max(30.0, textWidth.toDouble() + offset / 2)
        val rectHeight = max(26.5, textHeight.toDouble())

        val rectLeft = (-rectWidth / 1.5 + scale + (offset / 2)).toFloat()
        val rectTop = (-rectHeight / 3 - scale).toFloat()
        val rectRight = (rectWidth / 1.5 - scale - (offset / 2)).toFloat()
        val rectBottom = (rectHeight / 1.05 + scale).toFloat()

        // Render rect background
        GradientShader.begin(
            backgroundMode == "Gradient",
            gradientX,
            gradientY,
            bgGradColors.toColorArray(maxBackgroundGradientColors),
            gradientBackgroundSpeed,
            gradientOffset
        ).use {
            RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                drawRoundedRect(
                    rectLeft,
                    rectTop,
                    rectRight,
                    rectBottom,
                    when (backgroundMode) {
                        "Gradient" -> 0
                        "Rainbow" -> 0
                        else -> backgroundColor.rgb
                    },
                    roundedRectRadius
                )
            }
        }

        if (outline) {
            glLineWidth(1.5F)
            glBegin(GL_LINE_LOOP)
            glColor4f(1F, 1F, 1F, 1F)
            glVertex2f(rectLeft, rectTop)
            glVertex2f(rectRight, rectTop)
            glVertex2f(rectRight, rectBottom)
            glVertex2f(rectLeft, rectBottom)
            glEnd()
            resetColor()
        }

        // Render distance text
        GradientFontShader.begin(
            textMode == "Gradient",
            gradientX,
            gradientY,
            textGradColors.toColorArray(maxTextGradientColors),
            gradientTextSpeed,
            gradientOffset
        ).use {
            RainbowFontShader.begin(textMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                textFont.drawString(
                    text,
                    (-textWidth / 2.15F),
                    (1F - textHeight / 2.15F),
                    when (textMode) {
                        "Gradient" -> 0
                        "Rainbow" -> 0
                        else -> textColors.color(1).rgb
                    },
                    textShadow
                )
            }
        }

        blocks.forEach { block ->
            val texture = getBlockTexture(block)
            mc.textureManager.bindTexture(texture)

            if (highlightUnbreakable && (block == Blocks.obsidian || block == Blocks.end_stone)) {
                glColor4f(1F, 0.25F, 0.25F, 1F)
            } else {
                glColor4f(1F, 1F, 1F, 1F)
            }

            Gui.drawModalRectWithCustomSizedTexture(
                offset,
                10,
                0f, 0f,
                iconSize, iconSize,
                iconSize.toFloat(), iconSize.toFloat()
            )
            offset += iconStep + scale.toInt()
        }
        resetColor()

        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glDepthMask(true)
        resetColor()

        glPopMatrix()
    }
}