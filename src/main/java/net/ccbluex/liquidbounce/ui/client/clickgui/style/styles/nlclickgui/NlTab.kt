package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color

class NlTab(val type: Category, val y2: Int) {
    var x = 0
    var y = 0
    var w = 0
    var h = 0

    val nlSubList: MutableList<NlSub> = ArrayList()

    init {
        var y3 = 0
        for (subCategory in type.subCategories) {
            nlSubList.add(NlSub(type, subCategory, y2 + y3))
            y3 += 18
        }
    }

    fun draw(mx: Int, my: Int) {
        Fonts.Nl_16.drawString(
            type.name,
            (x + 10).toFloat(),
            (y + y2).toFloat(),
            if (NeverloseGui.getInstance().light) Color(60, 60, 60).rgb else Color(66, 64, 62).rgb
        )

        for (nlSub in nlSubList) {
            nlSub.x = x
            nlSub.y = y
            nlSub.w = w
            nlSub.h = h

            if (!nlSub.isSelected) {
                for (nlModule in nlSub.nlModules) {
                    for (nlSetting in nlModule.downwards) {
                        if (nlSetting is Numbersetting) {
                            nlSetting.percent = 0f
                        }
                        if (nlSetting is BoolSetting) {
                            if (nlSetting.toggleAnimation.direction == Direction.FORWARDS) {
                                nlSetting.toggleAnimation.reset()
                            }
                        }
                    }
                    if (nlModule.toggleAnimation.direction == Direction.FORWARDS) {
                        nlModule.toggleAnimation.reset()
                    }
                }
            }

            nlSub.draw(mx, my)
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        nlSubList.forEach { it.keyTyped(typedChar, keyCode) }
    }

    fun released(mx: Int, my: Int, mb: Int) {
        nlSubList.forEach { it.released(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        nlSubList.forEach { it.click(mx, my, mb) }
        if (mb == 0) {
            for (categoryRender in nlSubList) {
                if (RenderUtil.isHovering(categoryRender.x + 7f, categoryRender.y + categoryRender.y2 + 8f, 76f, 15f, mx, my)) {
                    NeverloseGui.getInstance().selectedSub = categoryRender
                }
            }
        }
    }
}
