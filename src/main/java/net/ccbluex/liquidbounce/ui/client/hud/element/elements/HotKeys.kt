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
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

@ElementInfo(name = "HotKeys")
class HotKeys(
    x: Double = 0.60,
    y: Double = 268.23
) : Element("HotKeys", x, y) {

    private val font by font("Font", Fonts.font35)
    private val titleText by text("Title", "HotKeys")

    private val backgroundMode by choices(
        "Background-Mode", arrayOf("Custom", "Theme"), "Custom"
    )

    private val bgColors = ColorSettingsInteger(this, "BackgroundColor") { backgroundMode == "Custom" }.with(a = 150)

    private val accentColor by color("AccentColor", Color(255, 255, 255))
    private val textColor by color("TextColor", Color.WHITE)
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
                "Custom" -> {
                    drawCustomShapeWithRadius(
                        posX,
                        posY,
                        currentWidth,
                        currentHeight,
                        roundedRadius,
                        bgColors.color()
                    )
                }
                "Theme" -> {
                    val themeBackground = getBackgroundColor(0)
                    drawCustomShapeWithRadius(
                        posX,
                        posY,
                        currentWidth,
                        currentHeight,
                        roundedRadius,
                        themeBackground
                    )
                }
            }

            val titleWidth = font.getStringWidth(titleText)
            Fonts.InterMedium_13.drawCenteredStringShadow(
                titleText,
                posX + (currentWidth / 2f),
                posY + padding,
                textColor.rgb
            )

            val iconSize = 10f
            Fonts.Nursultan13.drawString(
                "C",
                posX + currentWidth - iconSize - padding,
                posY + padding + 2f,
                accentColor.rgb
            )

            posY += font.FONT_HEIGHT + (padding * 2f)

            var maxWidth = titleWidth + (padding * 2f)
            var localHeight = font.FONT_HEIGHT + (padding * 2f)

            drawCustomShapeWithRadius(
                posX + 0.5f,
                posY,
                currentWidth - 1f,
                1.25f,
                3f,
                accentColor.darker(0.4f)
            )
            posY += 3f
            localHeight += 3f

            for (module in moduleManager) {
                if (module.keyBind == Keyboard.KEY_NONE) continue

                val nameText = module.name
                val bindText = "[${Keyboard.getKeyName(module.keyBind)}]"
                val nameWidth = Fonts.InterMedium_13.stringWidth(nameText)
                val bindWidth = Fonts.InterMedium_13.stringWidth(bindText)

                val totalWidth = nameWidth + bindWidth + (padding * 3f)
                if (totalWidth > maxWidth) {
                    maxWidth = totalWidth
                }

                val moduleColor = Color(255, 255, 255, fadeAlpha).rgb

                Fonts.InterMedium_13.drawString(
                    nameText,
                    posX + padding,
                    posY + 2f,
                    moduleColor
                )

                Fonts.InterMedium_13.drawString(
                    bindText,
                    posX + currentWidth - bindWidth - padding,
                    posY + 2f,
                    moduleColor
                )

                val lineHeight = Fonts.InterMedium_13.height + padding
                posY += lineHeight
                localHeight += lineHeight
            }

            currentWidth = max(maxWidth, minWidth)
            currentHeight = localHeight + 2.5f
        }

        return Border(0f, 0f, currentWidth, currentHeight)
    }
}