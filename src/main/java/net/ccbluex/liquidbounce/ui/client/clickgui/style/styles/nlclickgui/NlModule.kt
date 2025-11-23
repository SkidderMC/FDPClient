/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.Companion.getInstance
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.applyOpacity
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.brighter
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.darker
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.fakeCircleGlow
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.interpolateColorC
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.isHovering
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.resetColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.BoolSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.ColorSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.FontSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.RangeSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.StringsSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.TextSetting
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color
import java.util.function.Consumer
import java.util.stream.Collectors

class NlModule(var NlSub: NlSub, var module: Module, var lef: Boolean) {

    var x: Int = 0
    var y: Int = 0
    var w: Int = 0
    var h: Int = 0

    var leftAdd: Int = 0
    var rightAdd: Int = 0


    var posx: Int
    var posy: Int = 0

    private var layoutY: Int = 0
    private var cardWidth: Float = 160f

    var height: Int = 0

    var downwards: MutableList<Downward<*>> = ArrayList<Downward<*>>()

    var scrollY: Int = 0

    private var toggleXPosition = 0f
    private var toggleYPosition = 0f

    var toggleAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)

    var HoveringAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)


    init {
        this.posx = if (lef) 0 else 170
        for (setting in module.values) {
            if (setting is BoolValue) {
                this.downwards.add(BoolSetting(setting, this))
            }
            if (setting is FloatValue || setting is IntValue || setting is BlockValue) {
                this.downwards.add(Numbersetting(setting, this))
            }
            if (setting is FloatRangeValue || setting is IntRangeValue) {
                this.downwards.add(RangeSetting(setting, this))
            }
            if (setting is ListValue) {
                this.downwards.add(StringsSetting(setting, this))
            }
            if (setting is ColorValue) {
                this.downwards.add(ColorSetting(setting, this))
            }
            if (setting is TextValue) {
                this.downwards.add(TextSetting(setting, this))
            }
            if (setting is FontValue) {
                this.downwards.add(FontSetting(setting, this))
            }
        }
    }


    fun calcHeight(): Int {
        var h = 20
        for (s in module.values.stream().filter { obj: Value<*>? -> obj!!.shouldRender() }
            .collect(Collectors.toList())) {
            h += 20
        }
        if (module.values.isEmpty()) {
            h += 20
        }
        return h
    }


    fun setLayout(cardStartX: Float, layoutY: Int, cardWidth: Float, panelX: Int) {
        this.cardWidth = cardWidth
        this.layoutY = layoutY
        this.posx = (cardStartX - (panelX + 95)).toInt()
    }


    fun calcY(): Int {
        return layoutY
    }

    fun draw(mx: Int, my: Int) {
        posy = calcY()
        height = calcHeight()

        val cardStartX = (x + 95 + posx).toFloat()
        toggleXPosition = cardStartX + cardWidth - 22f
        toggleYPosition = (y + posy + scrollY + 56).toFloat()

        drawRound(
            cardStartX,
            (y + 50 + posy + scrollY).toFloat(),
            cardWidth,
            calcHeight().toFloat(),
            2f,
            if (getInstance().light) Color(245, 245, 245) else Color(3, 13, 26)
        )

        Fonts.Nl.Nl_18.Nl_18.drawString(
            module.name,
            (cardStartX + 5f),
            (y + posy + 55 + scrollY).toFloat(),
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        drawRound(
            (cardStartX + 5f),
            (y + 65 + posy + scrollY).toFloat(),
            cardWidth - 10f,
            0.7f,
            0f,
            if (getInstance().light) Color(213, 213, 213) else Color(9, 21, 34)
        )


        val toggleX = toggleXPosition
        val toggleY = toggleYPosition

        HoveringAnimation.direction = if (isHovering(
                toggleX,
                toggleY,
                16f,
                4.5f,
                mx,
                my
            )
        ) Direction.FORWARDS else Direction.BACKWARDS


        var cheigt = 20
        for (downward in downwards.stream().filter { s: Downward<*>? -> s!!.setting.shouldRender() }
            .collect(Collectors.toList())) {
            downward.setX(posx)
            downward.setY(calcY() + cheigt)
            cheigt += 20

            downward.draw(mx, my)
        }
        rendertoggle()

        if (module.values.isEmpty()) {
            Fonts.Nl.Nl_22.Nl_22!!.drawString(
                "No settings.",
                x + 100 + posx,
                y + posy + scrollY + 72,
                if (getInstance().light) Color(95, 95, 95).rgb else -1
            )
        }
    }

    fun rendertoggle() {
        val darkRectColor = Color(29, 29, 39, 255)

        val darkRectHover = brighter(darkRectColor, .8f)

        val accentCircle = darker(NeverloseGui.neverlosecolor, .5f)


        toggleAnimation.direction = if (module.state) Direction.FORWARDS else Direction.BACKWARDS

        drawRound(
            toggleXPosition, toggleYPosition, 16f, 4.5f,
            2f, interpolateColorC(applyOpacity(darkRectHover, .5f), accentCircle, toggleAnimation.getOutput().toFloat())
        )

        fakeCircleGlow(
            toggleXPosition + 3 + ((11) * toggleAnimation.getOutput()).toFloat(),
            toggleYPosition + 2, 6f, Color.BLACK, .3f
        )

        resetColor()

        drawRound(
            toggleXPosition + ((11) * toggleAnimation.getOutput()).toFloat(),
            toggleYPosition - 1,
            6.5f,
            6.5f,
            3f,
            if (module.state) NeverloseGui.neverlosecolor else if (getInstance().light) Color(
                255,
                255,
                255
            ) else Color(
                (68 - (28 * HoveringAnimation.getOutput())).toInt(),
                (82 + (44 * HoveringAnimation.getOutput())).toInt(),
                (87 + (83 * HoveringAnimation.getOutput())).toInt()
            )
        )
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        downwards.forEach(Consumer { e: Downward<*>? -> e!!.keyTyped(typedChar, keyCode) })
    }

    fun released(mx: Int, my: Int, mb: Int) {
        downwards.stream().filter { e: Downward<*>? -> e!!.setting.shouldRender() }
            .forEach { e: Downward<*>? -> e!!.mouseReleased(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        downwards.stream().filter { e: Downward<*>? -> e!!.setting.shouldRender() }
            .forEach { e: Downward<*>? -> e!!.mouseClicked(mx, my, mb) }

        if (isHovering(
                toggleXPosition,
                toggleYPosition,
                16f,
                4.5f,
                mx,
                my
            ) && mb == 0
        ) {
            module.toggle()
        }
    }
}