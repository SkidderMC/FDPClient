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
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.ColorSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.StringsSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
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

    var height: Int = 0

    var downwards: MutableList<Downward<*>> = ArrayList<Downward<*>>()

    var scrollY: Int = 0

    var toggleAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)

    var HoveringAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)


    init {
        this.posx = if (lef) 0 else 170
        for (setting in module.values) {
            if (setting is BoolValue) {
                this.downwards.add(BoolSetting(setting, this))
            }
            if (setting is FloatValue || setting is IntValue) {
                this.downwards.add(Numbersetting(setting, this))
            }
            if (setting is ListValue) {
                this.downwards.add(StringsSetting(setting, this))
            }
            if (setting is ColorValue) {
                this.downwards.add(ColorSetting(setting, this))
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


    fun calcY(): Int {
        leftAdd = 0
        rightAdd = 0

        for (tabModule in NlSub.layoutModules!!) {
            if (tabModule === this) {
                break
            } else {
                if (tabModule!!.lef) {
                    leftAdd += tabModule.calcHeight() + 10
                } else {
                    rightAdd += tabModule.calcHeight() + 10
                }
            }
        }

        return if (lef) leftAdd else rightAdd
    }

    fun draw(mx: Int, my: Int) {
        posy = calcY()

        drawRound(
            (x + 95 + posx).toFloat(),
            (y + 50 + posy + scrollY).toFloat(),
            160f,
            calcHeight().toFloat(),
            2f,
            if (getInstance().light) Color(245, 245, 245) else Color(3, 13, 26)
        )

        Fonts.Nl.Nl_18.Nl_18.drawString(
            module.name,
            x + 100 + posx,
            y + posy + 55 + scrollY,
            if (getInstance().light) Color(95, 95, 95).getRGB() else -1
        )

        drawRound(
            (x + 100 + posx).toFloat(),
            (y + 65 + posy + scrollY).toFloat(),
            150f,
            0.7f,
            0f,
            if (getInstance().light) Color(213, 213, 213) else Color(9, 21, 34)
        )


        HoveringAnimation.direction = if (isHovering(
                (x + 265 - 32 + posx).toFloat(),
                (y + posy + scrollY + 56).toFloat(),
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
                "No Settings.",
                x + 100 + posx,
                y + posy + scrollY + 72,
                if (getInstance().light) Color(95, 95, 95).getRGB() else -1
            )
        }
    }

    fun rendertoggle() {
        val darkRectColor = Color(29, 29, 39, 255)

        val darkRectHover = brighter(darkRectColor, .8f)

        val accentCircle = darker(NeverloseGui.Companion.neverlosecolor, .5f)


        toggleAnimation.direction = if (module.state) Direction.FORWARDS else Direction.BACKWARDS

        drawRound(
            (x + 265 - 32 + posx).toFloat(), (y + posy + scrollY + 56).toFloat(), 16f, 4.5f,
            2f, interpolateColorC(applyOpacity(darkRectHover, .5f), accentCircle, toggleAnimation.getOutput().toFloat())
        )

        fakeCircleGlow(
            (x + 265 + 3 - 32 + posx + ((11) * toggleAnimation.getOutput())).toFloat(),
            (y + posy + scrollY + 56 + 2).toFloat(), 6f, Color.BLACK, .3f
        )

        resetColor()

        drawRound(
            (x + 265 - 32 + posx + ((11) * toggleAnimation.getOutput())).toFloat(),
            (y + posy + scrollY + 56 - 1).toFloat(),
            6.5f,
            6.5f,
            3f,
            if (module.state) NeverloseGui.Companion.neverlosecolor else if (getInstance().light) Color(
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
                (x + 265 - 32 + posx).toFloat(),
                (y + posy + scrollY + 56).toFloat(),
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
