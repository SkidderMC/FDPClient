/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.extensions.safeDiv
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

@ElementInfo(name = "Keystrokes")
class Keystrokes : Element("Keystrokes", 2.0, 123.0) {
    private val radius by float("RectangleRound-Radius", 3F, 0F..10F)
    private val textColors = ColorSettingsInteger(this, "Text", applyMax = true)
    private val rectColors = ColorSettingsInteger(this, "Rectangle").with(a = 150)
    private val pressColors = ColorSettingsInteger(this, "Press").with(Color.BLUE)
    private val renderBorder by boolean("RenderBorder", false)
    private val borderColors = ColorSettingsInteger(this, "Border") { renderBorder }
    private val borderWidth by float("BorderWidth", 1F, 0.5F..5F) { renderBorder }
    private val shrinkOnPress by boolean("ShrinkOnPress", true)
    private val shrinkPercentage by int("ShrinkPercentage", 80, 50..100, suffix = "%") { shrinkOnPress }
    private val shrinkSpeed by int("ShrinkSpeed", 1, 0..5, suffix = "Ticks") { shrinkOnPress }

    private var shadow by boolean("Text-Shadow", true)
    private val font by font("Font", Fonts.font40)

    private val textColor
        get() = textColors.color()

    private val rectColor
        get() = rectColors.color()

    private val pressColor
        get() = pressColors.color()

    private val borderColor
        get() = borderColors.color()

    data class GridKey(
        val row: Int,
        val column: Int,
        val text: String,
        var scale: Float = 1f,
        val keystrokes: Keystrokes,
        var color: Color = keystrokes.rectColor
    ) {
        fun updateState(isPressed: Boolean) {
            val min = keystrokes.shrinkPercentage / 100f
            val targetScale = if (isPressed && keystrokes.shrinkOnPress) min else 1f
            val deltaTime = RenderUtils.deltaTimeNormalized(keystrokes.shrinkSpeed).takeIf { it != 0.0 } ?: 1F

            scale = (scale..targetScale).lerpWith(deltaTime)

            val t = 1f - ((scale - min) safeDiv (1f - min))

            val baseColor = keystrokes.rectColor
            val targetColor = keystrokes.pressColor

            val r = (baseColor.red + (targetColor.red - baseColor.red) * t).toInt()
            val g = (baseColor.green + (targetColor.green - baseColor.green) * t).toInt()
            val b = (baseColor.blue + (targetColor.blue - baseColor.blue) * t).toInt()
            val a = (baseColor.alpha + (targetColor.alpha - baseColor.alpha) * t).toInt()

            color = Color(r, g, b, a)
        }
    }

    private val GridKey.textWidth: Int
        get() = font.getStringWidth(this.text)

    // row -> column -> key
    private val gridLayout = arrayOf(
        GridKey(1, 1, "W", keystrokes = this),
        GridKey(2, 0, "A", keystrokes = this),
        GridKey(2, 1, "S",  keystrokes = this),
        GridKey(2, 2, "D", keystrokes = this),
        GridKey(3, 1, "Space", keystrokes = this)
    )

    override fun drawElement(): Border {
        val options = mc.gameSettings

        val movementKeys = mapOf(
            "Space" to options.keyBindJump,
            "W" to options.keyBindForward,
            "A" to options.keyBindLeft,
            "S" to options.keyBindBack,
            "D" to options.keyBindRight
        )

        val padding = 3F

        val fontHeight = (font as? GameFontRenderer)?.height ?: font.FONT_HEIGHT
        val maxCharWidth = gridLayout.maxOf { it.textWidth }

        val boxSize = maxOf(fontHeight, maxCharWidth)

        gridLayout.forEach { gridKey ->
            val (row, col, key, scale, _, color) = gridKey

            val currentX = col * (boxSize + padding)
            val currentY = row * (boxSize + padding)

            val (startX, endX) = if (row == 3) {
                // Fill from the first row until the last (Space button)
                0F to 2 * (boxSize + padding) + boxSize
            } else currentX to currentX + boxSize

            val isPressed = movementKeys[key]?.isKeyDown == true
            gridKey.updateState(isPressed)

            val scaledBoxSize = boxSize * scale
            val scaledPadding = (boxSize - scaledBoxSize) / 2

            val adjustedStartX = startX + scaledPadding
            val adjustedEndX = endX - scaledPadding
            val adjustedY = currentY + scaledPadding

            RenderUtils.drawRoundedRect(
                adjustedStartX, adjustedY, adjustedEndX, adjustedY + scaledBoxSize, color.rgb, radius
            )

            if (renderBorder) {
                RenderUtils.drawRoundedBorder(
                    adjustedStartX,
                    adjustedY,
                    adjustedEndX,
                    adjustedY + scaledBoxSize,
                    borderWidth,
                    borderColor.rgb,
                    radius
                )
            }

            val textX = (adjustedStartX + adjustedEndX) / 2 - (font.getStringWidth(key) / 2)
            val textY = adjustedY + (scaledBoxSize / 2) - (fontHeight / 2)

            font.drawString(key, textX, textY + if (font == mc.fontRendererObj) 0 else 2, textColor.rgb, shadow)
        }

        return Border(0F, boxSize + padding, boxSize * 3 + padding * 2, boxSize * 4 + padding * 3)
    }
}