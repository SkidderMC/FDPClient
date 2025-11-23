package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Category.SubCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.Companion.getInstance
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.scissor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.roundToHalf
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min

class NlSub(parentCategory: Category?, var subCategory: SubCategory, var y2: Int) {
    var x: Int = 0
    var y: Int = 0
    var w: Int = 0
    var h: Int = 0

    var nlModules: MutableList<NlModule> = ArrayList<NlModule>()
    private var visibleModules: MutableList<NlModule> = ArrayList<NlModule>()

    var alphaani: Animation = EaseInOutQuad(150, 1.0, Direction.BACKWARDS)

    private var maxScroll = Float.Companion.MAX_VALUE
    private val minScroll = 0f
    private var rawScroll = 0f

    private var scroll = 0f

    private var scrollAnimation: Animation = SmoothStepAnimation(0, 0.0, Direction.BACKWARDS)

    init {
        var count = 0

        for (holder in FDPClient.moduleManager) {
            if (holder.category == parentCategory && holder.subCategory == subCategory) {
                nlModules.add(NlModule(this, holder, count % 2 == 0))
                count++
            }
        }
    }

    fun draw(mx: Int, my: Int) {
        alphaani.direction = if (isSelected) Direction.FORWARDS else Direction.BACKWARDS

        if (this.isSelected) {
            drawRound(
                (x + 7).toFloat(),
                (y + y2 + 8).toFloat(),
                76f,
                15f,
                2f,
                if (getInstance().light) Color(
                    200,
                    200,
                    200,
                    (100 + (155 * alphaani.getOutput())).toInt()
                ) else Color(8, 48, 70, (100 + (155 * alphaani.getOutput())).toInt())
            )
        }

        Fonts.NlIcon.nlfont_20.nlfont_20.drawString(
            this.icon,
            x + 10,
            y + y2 + 14,
            NeverloseGui.Companion.neverlosecolor.getRGB()
        )

        Fonts.Nl.Nl_18.Nl_18.drawString(
            subCategory.toString(), x + 10 + Fonts.NlIcon.nlfont_20.nlfont_20.stringWidth(
                this.icon
            ) + 8, y + y2 + 13, if (getInstance().light) Color(18, 18, 19).getRGB() else -1
        )

        if (this.isSelected && subCategory != SubCategory.CONFIGS) {
            val scrolll = getScroll().toDouble()
            visibleModules = getVisibleModules()
            for (nlModule in visibleModules) {
                nlModule.scrollY = roundToHalf(scrolll).toInt()
            }
            onScroll(40)

            if (!visibleModules.isEmpty()) {
                val lastModule = visibleModules.get(visibleModules.size - 1)
                maxScroll = max(0, lastModule.y + 50 + lastModule.posy + lastModule.height).toFloat()
            } else {
                maxScroll = 0f
            }

            for (nlModule in visibleModules) {
                nlModule.x = x
                nlModule.y = y
                nlModule.w = w
                nlModule.h = h

                GL11.glEnable(GL11.GL_SCISSOR_TEST)
                scissor((x + 90).toDouble(), (y + 40).toDouble(), (w - 90).toDouble(), (h - 40).toDouble())

                nlModule.draw(mx, my)
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
            }
        }

        if (this.isSelected && (subCategory == SubCategory.CONFIGS)) {
            val scrolll = getScroll().toDouble()
            getInstance().configs.setScroll(roundToHalf(scrolll).toInt())
            getInstance().configs.setBounds(x + 90, y + 40, (w - 110).toFloat())
            onScroll(40)
            maxScroll = max(0, getInstance().configs.contentHeight - (h - 40)).toFloat()

            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            scissor((x + 90).toDouble(), (y + 40).toDouble(), (w - 90).toDouble(), (h - 40).toDouble())
            getInstance().configs.draw(mx, my)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    fun onScroll(ms: Int) {
        scroll = (rawScroll - scrollAnimation.getOutput()).toFloat()
        rawScroll += Mouse.getDWheel() / 4f
        rawScroll = max(min(minScroll, rawScroll), -maxScroll)
        scrollAnimation = SmoothStepAnimation(ms, (rawScroll - scroll).toDouble(), Direction.BACKWARDS)
    }

    fun getScroll(): Float {
        scroll = (rawScroll - scrollAnimation.getOutput()).toFloat()
        return scroll
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        nlModules.forEach(Consumer { e: NlModule? -> e!!.keyTyped(typedChar, keyCode) })
    }

    fun released(mx: Int, my: Int, mb: Int) {
        nlModules.forEach(Consumer { e: NlModule? -> e!!.released(mx, my, mb) })
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (this.isSelected && subCategory != SubCategory.CONFIGS) {
            nlModules.forEach(Consumer { e: NlModule? -> e!!.click(mx, my, mb) })
        }

        if (this.isSelected && (subCategory == SubCategory.CONFIGS)) {
            getInstance().configs.click(mx, my, mb)
        }
    }

    val isSelected: Boolean
        get() = getInstance().selectedSub == this

    val layoutModules: MutableList<NlModule?>?
        get() = (if (visibleModules.isEmpty() && getInstance().isSearching) visibleModules else (if (visibleModules.isEmpty()) nlModules else visibleModules)) as MutableList<NlModule?>?

    private fun getVisibleModules(): MutableList<NlModule> {
        if (!getInstance().isSearching) {
            return nlModules
        }
        val query: String = getInstance().searchTextContent.toLowerCase()
        return nlModules.stream()
            .filter { module: NlModule? -> module!!.module.name.lowercase(Locale.getDefault()).contains(query) }
            .collect(Collectors.toList())
    }

    private val icon: String
        get() = subCategory.icon
}
