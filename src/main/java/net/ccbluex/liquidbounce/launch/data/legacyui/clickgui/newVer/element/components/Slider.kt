package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.element.components

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.ColorManager
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.extensions.animSmooth
import net.ccbluex.liquidbounce.utils.render.RenderUtils

import java.awt.Color

class Slider(var accentColor: Color = Color(0, 140, 255)) {
    private var smooth = 0F
    private var value = 0F

    fun onDraw(x: Float, y: Float, width: Float) {
        smooth = smooth.animSmooth(value, 0.5F)
        RenderUtils.originalRoundedRect(x - 1F, y - 1F, x + width + 1F, y + 1F, 1F, ColorManager.unusedSlider.rgb)
        RenderUtils.originalRoundedRect(x - 1F, y - 1F, x + width * (smooth / 100F) + 1F, y + 1F, 1F, this.accentColor.rgb)
        RenderUtils.drawFilledCircle(x + width * (smooth / 100F), y, 5F, Color.white)
        RenderUtils.drawFilledCircle(x + width * (smooth / 100F), y, 3F, ColorManager.background)
    }

    fun setValue(desired: Float, min: Float, max: Float) {
        value = (desired - min) / (max - min) * 100F
    }
}
