/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.font
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

@ElementInfo(name = "Keystrokes")
class Keystrokes : Element(2.0, 123.0) {
    private val radius by float("RectangleRound-Radius", 3F, 0F..10F)
    private val textRainbow by boolean("Text-Rainbow", false)
    private val textColors = ColorSettingsInteger(this, "Text", zeroAlphaCheck = true, applyMax = true)
    private val rectRainbow by boolean("Rectangle-Rainbow", false)
    private val rectColors = ColorSettingsInteger(this, "Rectangle", zeroAlphaCheck = true).with(a = 150)
    private val pressRainbow by boolean("Press-Rainbow", false)
    private val pressColors = ColorSettingsInteger(this, "Press", zeroAlphaCheck = true).with(Color.BLUE)

    private var shadow by boolean("Text-Shadow", true)
    private val font by font("Font", Fonts.font40)

    // row -> column -> key
    private val gridLayout = listOf(
        Triple(1, 1, "W"),
        Triple(2, 0, "A"),
        Triple(2, 1, "S"),
        Triple(2, 2, "D"),
        Triple(3, 1, "Space")
    )

    private val textColor
        get() = if (textRainbow) rainbow().withAlpha(textColors.color().alpha) else textColors.color()

    private val rectColor
        get() = if (rectRainbow) rainbow().withAlpha(rectColors.color().alpha) else rectColors.color()

    private val pressColor
        get() = if (pressRainbow) rainbow().withAlpha(pressColors.color().alpha) else pressColors.color()

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
        val maxCharWidth = gridLayout.maxOf { (_, _, key) -> font.getStringWidth(key) }

        val boxSize = maxOf(fontHeight, maxCharWidth)

        gridLayout.forEach { (row, col, key) ->
            val currentX = col * (boxSize + padding)
            val currentY = row * (boxSize + padding)

            val (startX, endX) = if (row == 3) {
                // Fill from the first row until the last (Space button)
                0F to 2 * (boxSize + padding) + boxSize
            } else currentX to currentX + boxSize

            val color = if (movementKeys[key]?.isKeyDown == true) pressColor else rectColor

            RenderUtils.drawRoundedRect(startX, currentY, endX, currentY + boxSize, color.rgb, radius)

            val textX = (startX + endX) / 2 - (font.getStringWidth(key) / 2)
            val textY = currentY + (boxSize / 2) - (fontHeight / 2)

            font.drawString(key, textX, textY, textColor.rgb, shadow)
        }

        return Border(0F, boxSize + padding, boxSize * 3 + padding * 2, boxSize * 4 + padding * 3)
    }

}