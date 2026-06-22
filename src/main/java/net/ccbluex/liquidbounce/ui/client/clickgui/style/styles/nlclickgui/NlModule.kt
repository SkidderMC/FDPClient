/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.Companion.getInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils.applyOpacity
import net.ccbluex.liquidbounce.utils.render.RenderUtils.brighter
import net.ccbluex.liquidbounce.utils.render.RenderUtils.darker
import net.ccbluex.liquidbounce.utils.render.RenderUtils.fakeCircleGlow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.interpolateColorC
import net.ccbluex.liquidbounce.utils.render.RenderUtils.isHovering
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.BoolSetting
import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import net.ccbluex.liquidbounce.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.ColorSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.FontSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.RangeSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.StringsSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.TextSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.MultiSelectSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.KeyBindSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.Vec3Setting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings.CurveSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.max

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
            if (setting is FileValue) {
                this.downwards.add(FileSetting(setting, this))
            }
            if (setting is FontValue) {
                this.downwards.add(FontSetting(setting, this))
            }
            if (setting is MultiSelectValue) {
                this.downwards.add(MultiSelectSetting(setting, this))
            }
            if (setting is KeyBindValue) {
                this.downwards.add(KeyBindSetting(setting, this))
            }
            if (setting is Vec3Value) {
                this.downwards.add(Vec3Setting(setting, this))
            }
            if (setting is CurveValue) {
                this.downwards.add(CurveSetting(setting, this))
            }
        }
    }


    fun calcHeight(): Int {
        var h = 30
        for (downward in downwards) {
            if (downward.setting.shouldRender()) {
                h += downward.rowHeight()
            }
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
        val cardStartY = y + posy + scrollY + NeverloseGui.HEADER_HEIGHT + 10

        toggleXPosition = cardStartX + cardWidth - 22f
        toggleYPosition = (cardStartY + 6).toFloat()

        drawRound(
            cardStartX,
            cardStartY.toFloat(),
            cardWidth,
            calcHeight().toFloat(),
            2f,
            if (getInstance().light) Color(245, 245, 245) else Color(3, 13, 26)
        )

        Fonts.Nl.Nl_18.Nl_18.drawString(
            module.name,
            (cardStartX + 5f),
            (cardStartY + 5).toFloat(),
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        drawRound(
            (cardStartX + 5f),
            (cardStartY + 15).toFloat(),
            cardWidth - 10f,
            0.7f,
            0f,
            if (getInstance().light) Color(213, 213, 213) else Color(9, 21, 34)
        )


        val toggleX = toggleXPosition
        val toggleY = toggleYPosition

        HoveringAnimation.direction = if (isHovering(
                toggleX,
                toggleY - 1,
                16f,
                6.5f,
                mx,
                my
            )
        ) Direction.FORWARDS else Direction.BACKWARDS


        var cheigt = 42
        var hoveredDescription: String? = null
        for (downward in downwards.stream().filter { s: Downward<*>? -> s!!.setting.shouldRender() }
            .collect(Collectors.toList())) {
            downward.setX(posx)
            downward.setY(calcY() + cheigt)
            val rowH = downward.rowHeight()

            val description = downward.setting.description
            if (description != null && isHovering(
                    (getInstance().x + 100 + posx).toFloat(),
                    (getInstance().y + (downward.y + scrollY).toInt() + 50).toFloat(),
                    cardWidth - 10f,
                    rowH.toFloat(),
                    mx, my
                )
            ) {
                hoveredDescription = description
            }

            cheigt += rowH

            downward.draw(mx, my)
        }
        rendertoggle()

        hoveredDescription?.let { drawDescriptionTooltip(it, mx, my) }

        if (module.values.isEmpty()) {
            Fonts.Nl.Nl_22.Nl_22!!.drawString(
                "No settings.",
                x + 100 + posx,
                y + posy + scrollY + NeverloseGui.HEADER_HEIGHT + 42,
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
            2f, interpolateColorC(applyOpacity(darkRectHover, .5f), accentCircle, toggleAnimation.output.toFloat())
        )

        fakeCircleGlow(
            toggleXPosition + 3 + ((11) * toggleAnimation.output).toFloat(),
            toggleYPosition + 2, 6f, Color.BLACK, .3f
        )

        resetColor()

        drawRound(
            toggleXPosition + ((11) * toggleAnimation.output).toFloat(),
            toggleYPosition - 1,
            6.5f,
            6.5f,
            3f,
            if (module.state) NeverloseGui.neverlosecolor else if (getInstance().light) Color(
                255,
                255,
                255
            ) else Color(
                (68 - (28 * HoveringAnimation.output)).toInt(),
                (82 + (44 * HoveringAnimation.output)).toInt(),
                (87 + (83 * HoveringAnimation.output)).toInt()
            )
        )
    }

    private fun drawDescriptionTooltip(text: String, mx: Int, my: Int) {
        val width = Fonts.Nl_15.stringWidth(text) + 6
        val height = Fonts.Nl_15.height + 4
        val tipX = (mx + 8).toFloat()
        val tipY = (my - height - 2).toFloat()
        RenderUtil.drawRoundedRect(
            tipX, tipY, width.toFloat(), height.toFloat(), 2f,
            Color(0, 5, 19).rgb, 1f, Color(13, 24, 35).rgb
        )
        Fonts.Nl_15.drawString(text, tipX + 3f, tipY + 2f, Color.WHITE.rgb)
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        downwards.forEach(Consumer { e: Downward<*>? -> e!!.keyTyped(typedChar, keyCode) })
    }

    fun released(mx: Int, my: Int, mb: Int) {
        downwards.stream().filter { e: Downward<*>? -> e!!.setting.shouldRender() }
            .forEach { e: Downward<*>? -> e!!.mouseReleased(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (my < y + NeverloseGui.HEADER_HEIGHT || my >= y + h) return

        downwards.stream().filter { e: Downward<*>? -> e!!.setting.shouldRender() }
            .forEach { e: Downward<*>? -> e!!.mouseClicked(mx, my, mb) }

        if (isHovering(
                toggleXPosition,
                toggleYPosition - 1,
                16f,
                6.5f,
                mx,
                my
            ) && mb == 0
        ) {
            module.toggle()
        }
    }

    private class FileSetting(setting: FileValue, moduleRender: NlModule) : Downward<FileValue>(setting, moduleRender) {

        override fun draw(mouseX: Int, mouseY: Int) {
            val gui = NeverloseGui.getInstance()
            val mainx = gui.x
            val mainy = gui.y
            val textY = (y + getScrollY()).toInt()

            Fonts.Nl_16.drawString(
                setting.name,
                (mainx + 100 + x),
                (mainy + textY + 57).toFloat(),
                if (gui.light) Color(95, 95, 95).rgb else -1
            )

            val display = setting.shortName
            val stringWidth = Fonts.Nl_15.stringWidth(display) + 6

            RenderUtil.drawRoundedRect(
                (mainx + 170 + x),
                (mainy + textY + 54).toFloat(),
                max(80, stringWidth).toFloat(),
                14f,
                2f,
                if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
                1f,
                Color(13, 24, 35).rgb
            )

            Fonts.Nl_15.drawString(
                display,
                mainx + 174 + x,
                (mainy + textY + 59).toFloat(),
                if (gui.light) Color(95, 95, 95).rgb else -1
            )
        }

        override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            val gui = NeverloseGui.getInstance()
            val boxX = (gui.x + 170 + x)
            val boxY = (gui.y + (y + getScrollY()).toInt() + 54).toFloat()

            val display = setting.shortName
            val stringWidth = Fonts.Nl_15.stringWidth(display) + 6
            val boxWidth = max(80, stringWidth).toFloat()

            if (mouseButton == 0 && RenderUtil.isHovering(boxX, boxY, boxWidth, 14f, mouseX, mouseY)) {
                setting.openDialog()
            }
        }

        override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
    }
}