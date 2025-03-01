/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.managers

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.neverLoseBgColor
import net.ccbluex.liquidbounce.utils.client.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedOutline
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

fun Color.withAlpha(alpha: Int): Color = Color(red, green, blue, alpha)

object SideGuiBackgroundManager {

    var bgHexInput = "#"
    var bgHexFocused = false

    var bgAlpha: Float = 100f
    private val backgroundModes = arrayOf("none", "dark", "synced", "neverlose", "custom")
    data class Quad(val x: Float, val y: Float, val w: Float, val h: Float)
    fun drawBackgroundCategory(mouseX: Int, mouseY: Int, alpha: Int, drag: Drag, animScroll: Float, rectHeight: Float) {
        val bgXStart = drag.x + 25
        val bgYStart = drag.y + 60 + animScroll
        val cardWidth = 80f
        val cardHeight = 40f
        val cardsPerRow = 4
        var xPos = bgXStart
        var yPos = bgYStart
        var index = 0
        val maxVisibleY = drag.y + rectHeight - 60
        for (mode in backgroundModes) {
            if (yPos + cardHeight <= maxVisibleY) {
                val hovered = DrRenderUtils.isHovering(xPos, yPos, cardWidth, cardHeight, mouseX, mouseY)
                if (hovered && Mouse.isButtonDown(0)) {
                    if (mode == "custom") {
                        openBgColorPalette()
                    } else {
                        ClientThemesUtils.BackgroundMode = mode
                    }
                }
                val cardColor = getBgPreviewColor(mode, bgAlpha.toInt())
                DrRenderUtils.drawRect2(xPos.toDouble(), yPos.toDouble(), cardWidth.toDouble(), cardHeight.toDouble(), cardColor)
                if (ClientThemesUtils.BackgroundMode.equals(mode, ignoreCase = true)) {
                    drawRoundedOutline(xPos, yPos, xPos + cardWidth, yPos + cardHeight, 6f, 2f, Color.WHITE.rgb)
                }
                Fonts.InterBold_26.drawCenteredStringShadow(
                    mode.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    xPos + cardWidth / 2f,
                    yPos + cardHeight / 2f - Fonts.InterBold_26.height / 2,
                    DrRenderUtils.applyOpacity(-1, alpha / 255f)
                )
            }
            xPos += cardWidth + 10
            index++
            if (index % cardsPerRow == 0) {
                xPos = bgXStart
                yPos += cardHeight + 10
            }
        }
        drawBackgroundAlphaSlider(mouseX, mouseY, alpha, drag)
        drawBackgroundHexField(alpha, drag)
    }
    fun getBgHexFieldArea(drag: Drag): Quad {
        val sideBgX = drag.x + 25 + (80f + 10) * 5
        val sideBgY = drag.y + 60 + 45
        val sideBgW = 50f
        val sideBgH = 15f
        val textW = 80f
        val textH = 14f
        val x = sideBgX + (sideBgW - textW) / 2f
        val y = sideBgY - textH - 20
        return Quad(x, y, textW, textH)
    }
    fun drawBackgroundAlphaSlider(mouseX: Int, mouseY: Int, alpha: Int, drag: Drag) {
        val sliderX = drag.x + 25
        val sliderY = drag.y + 20
        val sliderW = 80f
        val sliderH = 10f
        DrRenderUtils.drawRect2(sliderX.toDouble(), sliderY.toDouble(), sliderW.toDouble(), sliderH.toDouble(), Color(60, 60, 60, alpha).rgb)
        val fraction = (bgAlpha - 1f) / (255f - 1f)
        val fill = sliderW * fraction
        DrRenderUtils.drawRect2(sliderX.toDouble(), sliderY.toDouble(), fill.toDouble(), sliderH.toDouble(), Color(100, 150, 100, alpha).rgb)
        Fonts.InterBold_26.drawString("BG Alpha: ${bgAlpha.toInt()}", sliderX + 2, sliderY - 12, DrRenderUtils.applyOpacity(-1, alpha / 255f))
        val hovered = DrRenderUtils.isHovering(sliderX, sliderY, sliderW, sliderH, mouseX, mouseY)
        if (hovered && Mouse.isButtonDown(0)) {
            bgAlpha = max(1f, min(255f, ((mouseX - sliderX) / sliderW) * (255f - 1f) + 1f))
        }
    }
    fun drawBackgroundHexField(alpha: Int, drag: Drag) {
        val quad = getBgHexFieldArea(drag)
        DrRenderUtils.drawRect2(quad.x.toDouble(), quad.y.toDouble(), quad.w.toDouble(), quad.h.toDouble(), Color(40, 40, 40, alpha).rgb)
        Fonts.InterBold_26.drawString("Hex:", quad.x, quad.y - 12, DrRenderUtils.applyOpacity(-1, alpha / 255f))
    }
    fun getBgPreviewColor(mode: String, alpha: Int): Int {
        val customBgColorValue = ColorValue("CustomBG", Color(32, 32, 64), false)
        return when (mode.lowercase()) {
            "none" -> Color(0, 0, 0, 0).rgb
            "dark" -> Color(21, 21, 21, alpha).rgb
            "synced" -> ClientThemesUtils.getColorWithAlpha(0, alpha).darker().darker().rgb
            "neverlose" -> neverLoseBgColor.withAlpha(alpha).rgb
            "custom" -> customBgColorValue.get().withAlpha(alpha).rgb
            else -> Color(21, 21, 21, alpha).rgb
        }
    }

    fun openBgColorPalette() {
        ClientThemesUtils.BackgroundMode = "custom"
        displayChatMessage("Opening BG color palette for 'Custom' mode...")
    }
    fun checkBackgroundInteractions(mouseX: Int, mouseY: Int) {

    }
}