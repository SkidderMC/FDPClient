/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.impl

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.backback
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.generateColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils.resetColor
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Component

class ModuleRect(val module: Module) : Component() {

    private val settingComponents = SettingComponents(module)

    private val toggleAnimation = EaseInOutQuad(300, 1.0, Direction.BACKWARDS)
    private val arrowAnimation = EaseInOutQuad(250, 1.0, Direction.BACKWARDS)
    private val hoverAnimation = DecelerateAnimation(250, 1.0, Direction.BACKWARDS)

    var settingAnimation: Animation? = null
    var openingAnimation: Animation? = null

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var panelLimitY: Float = 0f
    var alphaAnimation: Int = 0

    var clickX: Int = 0
    var clickY: Int = 0

    var settingSize: Double = 0.0
        private set

    fun initGui() {
        toggleAnimation.direction = if (module.state) Direction.FORWARDS else Direction.BACKWARDS
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        if (module.expanded) {
            settingComponents.keyTyped(typedChar, keyCode)
        }
    }

    fun drawScreen(mouseX: Int, mouseY: Int) {
        val baseRectColor = Color(43, 45, 50, alphaAnimation)
        val textColor = Color(255, 255, 255, alphaAnimation)

        val accentIndex = 0
        val accent = Color(generateColor(accentIndex).rgb)
        val accentWithAlpha = DrRenderUtils.applyOpacity(accent, alphaAnimation / 255f)

        val hoveringModule = DrRenderUtils.isHovering(x, y, width, height, mouseX, mouseY)
        hoverAnimation.direction = if (hoveringModule) Direction.FORWARDS else Direction.BACKWARDS

        val hoveredRectColorInt = DrRenderUtils.interpolateColor(
            baseRectColor.rgb,
            DrRenderUtils.brighter(baseRectColor, 0.8f).rgb,
            hoverAnimation.output.toFloat()
        )
        RenderUtils.drawGradientRect(
            x, y, x + width, y + height,
            hoveredRectColorInt,
            Color(hoveredRectColorInt).darker().rgb,
            0F
        )

        val accentOverlayColor = DrRenderUtils.applyOpacity(accentWithAlpha, toggleAnimation.output.toFloat()).rgb
        RenderUtils.drawGradientRect(
            x, y, x + width, y + height,
            accentOverlayColor,
            Color(accentOverlayColor).darker().rgb,
            0F
        )

        Fonts.InterMedium_20.drawString(
            module.name,
            x + 5,
            y + Fonts.InterMedium_20.getMiddleOfBox(height),
            textColor.rgb
        )

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && module.keyBind != 0) {
            val keyName = Keyboard.getKeyName(module.keyBind)
            Fonts.InterMedium_20.drawString(
                keyName,
                x + width - Fonts.InterMedium_20.stringWidth(keyName) - 5,
                y + Fonts.InterMedium_20.getMiddleOfBox(height),
                textColor.rgb
            )
        } else {
            val arrowSize = 6f
            arrowAnimation.direction = if (module.expanded) Direction.FORWARDS else Direction.BACKWARDS
            DrRenderUtils.setAlphaLimit(0f)
            resetColor()
            DrRenderUtils.drawClickGuiArrow(
                x + width - (arrowSize + 5),
                y + (height / 2f) - 2f,
                arrowSize,
                arrowAnimation,
                textColor.rgb
            )
        }

        val settingRectColor = Color(32, 32, 32, alphaAnimation)
        val expandedHeight = settingComponents.settingSize * (settingAnimation?.output ?: 0.0)

        if (module.expanded || (settingAnimation?.isDone == false)) {
            RenderUtils.drawGradientRect(
                x, y + height, x + width, y + height + expandedHeight * height,
                settingRectColor.rgb,
                settingRectColor.darker().rgb,
                0F
            )

            if (backback) {
                val accentAlpha = (0.85 * toggleAnimation.output).toFloat() * (alphaAnimation / 255f)
                RenderUtils.drawGradientRect(
                    x, y + height, x + width, y + height + expandedHeight * height,
                    DrRenderUtils.applyOpacity(accentWithAlpha, accentAlpha).rgb,
                    DrRenderUtils.applyOpacity(accentWithAlpha, accentAlpha / 2).rgb,
                    0F
                )
            }

            settingComponents.x = x
            settingComponents.y = y + height
            settingComponents.width = width
            settingComponents.rectHeight = height
            settingComponents.panelLimitY = panelLimitY
            settingComponents.alphaAnimation = alphaAnimation
            settingComponents.settingHeightScissor = settingAnimation

            // animations colors
            if (settingAnimation?.isDone == false) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST)
                DrRenderUtils.scissor(x.toDouble(), (y + height).toDouble(), width.toDouble(), expandedHeight * height)
                settingComponents.drawScreen(mouseX, mouseY)
                RenderUtils.drawGradientRect(
                    x, y + height, x + width, y + height + 6.0,
                    Color(0, 0, 0, 60).rgb,
                    Color(0, 0, 0, 0).rgb,
                    0F
                )
                RenderUtils.drawGradientRect(
                    x, y + 11 + expandedHeight * height, x + width, y + 17 + expandedHeight * height,
                    Color(0, 0, 0, 0).rgb,
                    Color(0, 0, 0, 60).rgb,
                    0F
                )
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
            } else {
                settingComponents.drawScreen(mouseX, mouseY)
                DrRenderUtils.drawGradientRect2(
                    x.toDouble(),
                    (y + height).toDouble(),
                    width.toDouble(),
                    6.0,
                    Color(0, 0, 0, 60).rgb,
                    Color(0, 0, 0, 0).rgb
                )
                DrRenderUtils.drawGradientRect2(
                    x.toDouble(),
                    y + 11 + (expandedHeight * height),
                    width.toDouble(),
                    6.0,
                    Color(0, 0, 0, 0).rgb,
                    Color(0, 0, 0, 60).rgb
                )
            }
        }
        settingSize = expandedHeight
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val hoveringModule = isClickable(y, panelLimitY) &&
                DrRenderUtils.isHovering(x, y, width, height, mouseX, mouseY)
        if (hoveringModule) {
            when (button) {
                0 -> {
                    clickX = mouseX
                    clickY = mouseY
                    toggleAnimation.direction = if (!module.state) Direction.FORWARDS else Direction.BACKWARDS
                    module.toggle()
                }
                1 -> module.expanded = !module.expanded
            }
        }
        if (module.expanded) {
            settingComponents.mouseClicked(mouseX, mouseY, button)
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (module.expanded) {
            settingComponents.mouseReleased(mouseX, mouseY, state)
        }
    }

    fun isClickable(currentY: Float, limitY: Float): Boolean {
        return currentY > limitY && currentY < limitY + Main.allowedClickGuiHeight + 17
    }
}
