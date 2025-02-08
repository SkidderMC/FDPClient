/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getBackgroundColor
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

@ElementInfo(name = "HotKeys")
class HotKeys(
    x: Double = 94.60,
    y: Double = 274.23
) : Element("HotKeys", x, y) {

    private val font by font("Font", Fonts.fontSemibold35)
    private val titleText by text("Title", "HotKeys")

    private val backgroundMode by choices("Background-Mode", arrayOf("Custom", "Theme"), "Custom")
    private val bgColors = ColorSettingsInteger(this, "BackgroundColor") { backgroundMode == "Custom" }.with(a = 150)

    private val iconEnabled by boolean("Icon", true)
    private val iconText by text("IconText", "C")
    private val iconColorMode by choices("IconColorMode", arrayOf("Custom", "Theme"), "Theme")
    private val iconColor by color("IconColor", Color(255, 255, 255))

    private val textColorMode by choices("TextColorMode", arrayOf("Custom", "Theme"), "Theme")
    private val textColor by color("TextColor", Color(255, 255, 255))

    private val keysColorMode by choices("KeysColorMode", arrayOf("Custom", "Theme"), "Theme")
    private val keysColor by color("KeysColor", Color(255, 255, 255))
    private val moduleNameColorMode by choices("ModuleNameColorMode", arrayOf("Custom", "Theme"), "Theme")
    private val moduleNameColor by color("ModuleNameColor", Color(255, 255, 255))

    private val barColorMode by choices("BarColorMode", arrayOf("Custom", "Theme"), "Theme")
    private val barColor by color("BarColor", Color(255, 255, 255))

    private val roundedRadius by float("Rounded-Radius", 4f, 0f..10f)
    private val padding by float("Padding", 5f, 0f..15f)
    private val minWidth by float("Min-Width", 80f, 50f..300f)

    private var currentWidth = 80f
    private var currentHeight = 20f

    private var animationValue = 0.0

    override fun drawElement(): Border {
        AWTFontRenderer.assumeNonVolatile {
            animationValue = AnimationUtil.base(animationValue, 1.0, 0.1)
            val fadeAlpha = (255 * animationValue).toInt().coerceIn(0, 255)
            val posX = 0f
            var posY = 0f
            when (backgroundMode) {
                "Custom" -> drawCustomShapeWithRadius(posX, posY, currentWidth, currentHeight, roundedRadius, bgColors.color())
                "Theme" -> drawCustomShapeWithRadius(posX, posY, currentWidth, currentHeight, roundedRadius, getBackgroundColor(0))
            }
            Fonts.InterMedium_13.drawCenteredStringShadow(titleText, posX + (currentWidth / 2f), posY + padding, Color.WHITE.rgb)
            if (iconEnabled) {
                val iconSize = 10f
                val iconDrawColor = if (iconColorMode == "Custom") iconColor.rgb else getTextColor()
                Fonts.Nursultan13.drawString(iconText, posX + currentWidth - iconSize - padding, posY + padding + 2f, iconDrawColor)
            }
            posY += font.FONT_HEIGHT + (padding * 2f)
            var maxWidth = font.getStringWidth(titleText) + (padding * 2f)
            var localHeight = font.FONT_HEIGHT + (padding * 2f)
            val actualBarColor = when (barColorMode) {
                "Custom" -> barColor.rgb
                "Theme" -> getTextColor()
                else -> barColor.rgb
            }
            drawCustomShapeWithRadius(posX + 0.5f, posY, currentWidth - 1f, 1.25f, 3f, Color(actualBarColor).darker(0.4f))
            posY += 3f
            localHeight += 3f
            for (module in moduleManager) {
                if (module.keyBind == Keyboard.KEY_NONE) continue
                val nameText = module.name
                val bindText = "[${Keyboard.getKeyName(module.keyBind)}]"
                val nameWidth = Fonts.InterMedium_13.stringWidth(nameText)
                val bindWidth = Fonts.InterMedium_13.stringWidth(bindText)
                val totalWidth = nameWidth + bindWidth + (padding * 3f)
                if (totalWidth > maxWidth) maxWidth = totalWidth
                val moduleBase = Color(getModuleNameColor())
                val moduleDrawColor = Color(moduleBase.red, moduleBase.green, moduleBase.blue, fadeAlpha).rgb
                val keysBase = Color(getKeysColor())
                val keysDrawColor = Color(keysBase.red, keysBase.green, keysBase.blue, fadeAlpha).rgb
                Fonts.InterMedium_13.drawString(nameText, posX + padding, posY + 2f, moduleDrawColor)
                Fonts.InterMedium_13.drawString(bindText, posX + currentWidth - bindWidth - padding, posY + 2f, keysDrawColor)
                val lineHeight = Fonts.InterMedium_13.height + padding
                posY += lineHeight
                localHeight += lineHeight
            }
            currentWidth = max(maxWidth, minWidth)
            currentHeight = localHeight + 2.5f
        }
        return Border(0f, 0f, currentWidth, currentHeight)
    }

    private fun getTextColor(): Int {
        return if (textColorMode == "Theme") getColor(0).rgb else textColor.rgb
    }

    private fun getKeysColor(): Int {
        return if (keysColorMode == "Theme") getColor(0).rgb else keysColor.rgb
    }

    private fun getModuleNameColor(): Int {
        return if (moduleNameColorMode == "Theme") getColor(0).rgb else moduleNameColor.rgb
    }
}