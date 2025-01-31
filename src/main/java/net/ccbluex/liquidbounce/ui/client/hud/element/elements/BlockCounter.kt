/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.visual.BlockOverlay
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.blocksAmount
import net.ccbluex.liquidbounce.utils.render.ColorSettingsFloat
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GradientShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowFontShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.RainbowShader
import net.ccbluex.liquidbounce.utils.render.toColorArray
import org.lwjgl.opengl.GL11

// TODO: Should it be removed? Text element does the same thing.
@ElementInfo(name = "BlockCounter")
class BlockCounter(x: Double = 520.0, y: Double = 245.0) : Element("BlockCounter", x = x, y = y) {

    private val onScaffold by boolean("ScaffoldOnly", true)

    private val textColorMode by choices("Text-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")
    private val textColors =
        ColorSettingsInteger(this, "Text", applyMax = true) { textColorMode == "Custom" }

    private val gradientTextSpeed by float("Text-Gradient-Speed", 1f, 0.5f..10f) { textColorMode == "Gradient" }

    private val maxTextGradientColors by int(
        "Max-Text-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS
    ) { textColorMode == "Gradient" }
    private val textGradColors =
        ColorSettingsFloat.create(this, "Text-Gradient") { textColorMode == "Gradient" && it <= maxTextGradientColors }

    private val roundedRectRadius by float("Rounded-Radius", 2F, 0F..5F)

    private val backgroundMode by choices("Background-Color", arrayOf("Custom", "Rainbow", "Gradient"), "Custom")

    private val bgColors = ColorSettingsInteger(this, "Background") { backgroundMode == "Custom" }

    private val gradientBackgroundSpeed by float(
        "Background-Gradient-Speed", 1f, 0.5f..10f
    ) { backgroundMode == "Gradient" }

    private val maxBackgroundGradientColors by int(
        "Max-Background-Gradient-Colors", 4, 1..MAX_GRADIENT_COLORS
    ) { backgroundMode == "Gradient" }
    private val bgGradColors = ColorSettingsFloat.create(
        this, "Background-Gradient"
    ) { backgroundMode == "Gradient" && it <= maxBackgroundGradientColors }

    private val borderColors = ColorSettingsInteger(this, "Border")

    private val font by font("Font", Fonts.fontSemibold40)
    private val textShadow by boolean("ShadowText", true)

    private val rainbowX by float("Rainbow-X", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }
    private val rainbowY by float("Rainbow-Y", -1000F, -2000F..2000F) { backgroundMode == "Rainbow" }

    private val gradientX by float(
        "Gradient-X", -1000F, -2000F..2000F
    ) { textColorMode == "Gradient" || backgroundMode == "Gradient" }
    private val gradientY by float(
        "Gradient-Y", -1000F, -2000F..2000F
    ) { textColorMode == "Gradient" || backgroundMode == "Gradient" }

    override fun drawElement(): Border {
        val info = "Blocks: §7${blocksAmount()}"

        // Calculate width only once + padding
        val width = font.getStringWidth(info) + 4F
        val heightPadding = if (font == mc.fontRendererObj) 1 else 0
        val height = ((font as? GameFontRenderer)?.height ?: (font.FONT_HEIGHT + heightPadding)).toFloat()

        if (Scaffold.handleEvents() && onScaffold || !onScaffold) {
            GL11.glPushMatrix()

            if (BlockOverlay.handleEvents() && BlockOverlay.info && BlockOverlay.currentBlock != null)
                GL11.glTranslatef(0f, 15f, 0f)

            val textCustomColor = textColors.color(1).rgb
            val backgroundCustomColor = bgColors.color().rgb
            val borderCustomColor = borderColors.color().rgb

            val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
            val rainbowX = if (rainbowX == 0f) 0f else 1f / rainbowX
            val rainbowY = if (rainbowY == 0f) 0f else 1f / rainbowY

            val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
            val gradientX = if (gradientX == 0f) 0f else 1f / gradientX
            val gradientY = if (gradientY == 0f) 0f else 1f / gradientY

            GradientShader.begin(
                backgroundMode == "Gradient",
                gradientX,
                gradientY,
                bgGradColors.toColorArray(maxBackgroundGradientColors),
                gradientBackgroundSpeed,
                gradientOffset
            ).use {
                RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                    RenderUtils.drawRoundedBorderRect(
                        0F, 0F, width, height, 3F, when (backgroundMode) {
                            "Gradient" -> 0
                            "Rainbow" -> 0
                            else -> backgroundCustomColor
                        }, borderCustomColor, roundedRectRadius
                    )
                }
            }

            GradientFontShader.begin(
                textColorMode == "Gradient",
                gradientX,
                gradientY,
                textGradColors.toColorArray(maxTextGradientColors),
                gradientTextSpeed,
                gradientOffset
            ).use {
                RainbowFontShader.begin(textColorMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                    font.drawString(
                        info, 2F, 2F - heightPadding, when (textColorMode) {
                            "Gradient" -> 0
                            "Rainbow" -> 0
                            else -> textCustomColor
                        }, textShadow
                    )
                }
            }

            GL11.glPopMatrix()
        }

        return Border(-1F, -1F, width + 1, height + 1)
    }
}