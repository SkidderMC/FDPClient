package net.ccbluex.liquidbounce.features.special

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

object GradientBackground {

    var nowGradient: Gradient
    var gradientSide = GradientSide.LEFT
    var animated = true

    val gradients = mutableListOf<Gradient>()
    val gradientSides = GradientSide.values() // this is faster than get enum values

    init {
        // color data from https://uigradients.com/
        val json = JsonParser().parse(
            GradientBackground::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/ui/misc/gradient.json")!!
                .reader(Charsets.UTF_8)).asJsonArray
        json.forEach {
            val obj = it.asJsonObject
            val colors = obj.getAsJsonArray("colors")
            gradients.add(Gradient(obj.get("name").asString, colors.first().asString, colors.last().asString))
        }
        nowGradient = gradients.first()
    }

    fun draw(width: Int, height: Int) {
        if(!animated) {
            val col1 = nowGradient.from.rgb
            val col2 = nowGradient.to.rgb
            when(gradientSide) {
                GradientSide.LEFT ->  RenderUtils.drawGradientSidewaysH(0.0, 0.0, width.toDouble(), height.toDouble(), col1, col2)
                GradientSide.RIGHT -> RenderUtils.drawGradientSidewaysH(0.0, 0.0, width.toDouble(), height.toDouble(), col2, col1)
                GradientSide.TOP -> RenderUtils.drawGradientSidewaysV(0.0, 0.0, width.toDouble(), height.toDouble(), col1, col2)
                GradientSide.BOTTOM -> RenderUtils.drawGradientSidewaysV(0.0, 0.0, width.toDouble(), height.toDouble(), col2, col1)
            }
            return
        }
        val posAffect = 100 / 1500.0
        val time = System.currentTimeMillis()
        for(i in 0..1500 step 100) {
            val pct1 = getGradientPercent(time + i)
            val pct2 = getGradientPercent(time + i + 100)
            val col1 = interpolateColor(nowGradient.from, nowGradient.to, pct1)
            val col2 = interpolateColor(nowGradient.from, nowGradient.to, pct2)
            val pos = i / 1500.0
            when(gradientSide) {
                GradientSide.LEFT ->  RenderUtils.drawGradientSidewaysH(width * pos, 0.0, width * (pos + posAffect), height.toDouble(), col1, col2)
                GradientSide.RIGHT -> RenderUtils.drawGradientSidewaysH(width * (1 - pos), 0.0, width * (1 - pos + posAffect), height.toDouble(), col2, col1)
                GradientSide.TOP -> RenderUtils.drawGradientSidewaysV(0.0, height * pos, width.toDouble(), height * (pos + posAffect), col1, col2)
                GradientSide.BOTTOM -> RenderUtils.drawGradientSidewaysV(0.0, height * (1 - pos), width.toDouble(), height * (1 - pos + posAffect), col2, col1)
            }
        }
    }

    private fun getGradientPercent(time: Long): Float {
        val stage = time % 3000
        val part = (stage / 1500f) > 1
        return if (part) 1 - (stage % 1500f) / 1500f else stage % 1500f / 1500f
    }

    private fun interpolate(a: Float, b: Float, proportion: Float): Float {
        return a + (b - a) * proportion
    }

    /**
     * https://stackoverflow.com/questions/4414673/android-color-between-two-colors-based-on-percentage
     */
    private fun interpolateColor(col1: Color, col2: Color, proportion: Float): Int {
        val hsva = Color.RGBtoHSB(col1.red, col1.green, col1.blue, null)
        val hsvb = Color.RGBtoHSB(col2.red, col2.green, col2.blue, null)
        for (i in 0..2) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion)
        }
        return Color.HSBtoRGB(hsvb[0], hsvb[1], hsvb[2])
    }

    class Gradient(val name: String, val from: Color, val to: Color) {
        constructor(name: String, from: String, to: String) : this(name, Color.decode(from), Color.decode(to))
    }

    enum class GradientSide {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
    }
}