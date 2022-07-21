package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.element.components

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.extensions.animLinear
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils

import java.awt.Color

class ToggleSwitch(var accentColor: Color = Color(0, 140, 255)) {
    private var smooth = 0F
    var state = false

    fun onDraw(x: Float, y: Float, width: Float, height: Float, bgColor: Color) {
        smooth = smooth.animLinear((if (state) 0.2F else -0.2F) * RenderUtils.deltaTime * 0.045F, 0F, 1F)
        val borderColor = BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf(Color(160, 160, 160), accentColor), smooth)
        val mainColor = BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf(bgColor, accentColor), smooth)
        val switchColor = BlendUtils.blendColors(floatArrayOf(0F, 1F), arrayOf(Color(160, 160, 160), bgColor), smooth)

        RenderUtils.originalRoundedRect(x - 0.5F, y - 0.5F, x + width + 0.5F, y + height + 0.5F, (height + 1F) / 2F, borderColor.rgb)
        RenderUtils.originalRoundedRect(x, y, x + width, y + height, height / 2F, mainColor.rgb)
        RenderUtils.drawFilledCircle(x + (1F - smooth) * (2F + (height - 4F) / 2F) + smooth * (width - 2F - (height - 4F) / 2F), y + 2F + (height - 4F) / 2F, (height - 4F) / 2F, switchColor)
    }
}