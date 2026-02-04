/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2) else value
        }

    private val maxLayers by int("MaxLayers", 5, 1..10)
    private val scale by float("Scale", 3F, 1F..5F)

    private val textMode by choices("Text-ColorMode", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")
    private val textColors = ColorSettingsInteger(this, "TextColor", applyMax = true) { textMode == "Custom" }

    private val gradientTextSpeed by float("Text-Gradient-Speed", 1f, 0.5f..10f) { textMode == "Gradient" }

    private val maxTextGradientColors by int("Max-Text-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { textMode == "Gradient" }
    private val textGradColors = ColorSettingsFloat.create(this, "Text-Gradient")
    { textMode == "Gradient" && it <= maxTextGradientColors }

    private val roundedRectRadius by float("Rounded-Radius", 3F, 0F..5F)

    private val backgroundMode by choices("Background-Mode", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")
    private val bgColors = ColorSettingsInteger(this, "BackgroundColor") { backgroundMode == "Custom" }.with(a = 100)

    private val gradientBackgroundSpeed by float("Background-Gradient-Speed", 1f, 0.5f..10f)
    { backgroundMode == "Gradient" }

    private val maxBackgroundGradientColors by int("Max-Background-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS)
    { backgroundMode == "Gradient" }
    private val bgGradColors = ColorSettingsFloat.create(this, "Background-Gradient")
    { backgroundMode == "Gradient" && it <= maxBackgroundGradientColors }

    private val textFont by font("Font", Fonts.fontSemibold35)
    private val textShadow by boolean("ShadowText", true)

    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
    private val gradientX by float("Gradient-X", -1000F, -2000F..2000F) { backgroundMode == "Gradient" }
    private val gradientY by float("Gradient-Y", -1000F, -2000F..2000F) { backgroundMode == "Gradient" }

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
        if (mc.thePlayer == null || mc.theWorld == null || bedStates.isEmpty()) return@handler

        bedStates.values.forEach(::drawPlate)
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

        // TODO: replace with layer blocks
        val blocks = bedState.surrounding.mapTo(linkedSetOf()) { it.block }
        val text = "Bed (${distance.roundToInt()}m)"

        var offset = (blocks.size * -13) / 2

        val textWidth = textFont.getStringWidth(text)
        val textHeight = textFont.FONT_HEIGHT

        val rectWidth = max(30.0, textWidth.toDouble() + offset / 2)
        val rectHeight = max(26.5, textHeight.toDouble())

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
                    (-rectWidth / 1.5 + scale + (offset / 2)).toFloat(),
                    (-rectHeight / 3 - scale).toFloat(),
                    (rectWidth / 1.5 - scale - (offset / 2)).toFloat(),
                    (rectHeight / 1.05 + scale).toFloat(),
                    when (backgroundMode) {
                        "Gradient" -> 0
                        "Rainbow" -> 0
                        else -> bgColors.color().rgb
                    },
                    roundedRectRadius
                )
            }
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

        // TODO: replace with item stack rendering
        blocks.forEach { block ->
            val texture = getBlockTexture(block)
            mc.textureManager.bindTexture(texture)

            Gui.drawModalRectWithCustomSizedTexture(
                offset,
                10,
                0f, 0f,
                12, 12,
                12F, 12F
            )
            offset += 13 + scale.toInt()
        }

        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glDepthMask(true)
        resetColor()

        glPopMatrix()
    }
}