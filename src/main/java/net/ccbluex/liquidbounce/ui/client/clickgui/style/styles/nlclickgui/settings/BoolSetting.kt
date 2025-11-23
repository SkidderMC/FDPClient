/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color

class BoolSetting(s: BoolValue, moduleRender: NlModule) : Downward<BoolValue>(s, moduleRender) {

    val toggleAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)
    private val hoveringAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)

    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = NeverloseGui.getInstance().x
        val mainy = NeverloseGui.getInstance().y

        val booly = (y + getScrollY()).toInt()

        Fonts.Nl.Nl_16.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x),
            (mainy + booly + 57).toFloat(),
            if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
        )

        val darkRectColor = Color(29, 29, 39, 255)
        val darkRectHover = RenderUtil.brighter(darkRectColor, .8f)
        val accentCircle = RenderUtil.darker(NeverloseGui.neverlosecolor, .5f)

        toggleAnimation.direction = if (setting.get()) Direction.FORWARDS else Direction.BACKWARDS

        hoveringAnimation.direction = if (
            RenderUtil.isHovering(
                (NeverloseGui.getInstance().x + 265 - 32 + x).toFloat(),
                (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 57).toFloat(),
                16f,
                4.5f,
                mouseX,
                mouseY
            )
        ) Direction.FORWARDS else Direction.BACKWARDS

        RoundedUtil.drawRound(
            (mainx + 265 - 32 + x).toFloat(),
            (mainy + booly + 57).toFloat(),
            16f,
            4.5f,
            2f,
            if (NeverloseGui.getInstance().light) {
                RenderUtil.interpolateColorC(
                    Color(230, 230, 230),
                    Color(0, 112, 186),
                    toggleAnimation.getOutput().toFloat()
                )
            } else {
                RenderUtil.interpolateColorC(
                    RenderUtil.applyOpacity(darkRectHover, .5f),
                    accentCircle,
                    toggleAnimation.getOutput().toFloat()
                )
            }
        )

        RenderUtil.fakeCircleGlow(
            (mainx + 265 + 3 - 32 + x + 11 * toggleAnimation.getOutput()).toFloat(),
            (mainy + booly + 59).toFloat(),
            6f,
            Color.BLACK,
            .3f
        )

        RenderUtil.resetColor()

        RoundedUtil.drawRound(
            (mainx + 265 - 32 + x + 11 * toggleAnimation.getOutput()).toFloat(),
            (mainy + booly + 56).toFloat(),
            6.5f,
            6.5f,
            3f,
            if (setting.get()) {
                NeverloseGui.neverlosecolor
            } else if (NeverloseGui.getInstance().light) {
                Color(255, 255, 255)
            } else {
                Color(
                    (68 - 28 * hoveringAnimation.getOutput()).toInt(),
                    (82 + 44 * hoveringAnimation.getOutput()).toInt(),
                    (87 + 83 * hoveringAnimation.getOutput()).toInt()
                )
            }
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            if (
                RenderUtil.isHovering(
                    (NeverloseGui.getInstance().x + 265 - 32 + x).toFloat(),
                    (NeverloseGui.getInstance().y + (y + getScrollY()).toInt() + 57).toFloat(),
                    16f,
                    4.5f,
                    mouseX,
                    mouseY
                )
            ) {
                setting.set(!setting.get())
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
    }
}