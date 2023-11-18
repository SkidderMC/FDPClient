package net.ccbluex.liquidbounce.ui.client.gui.newVer.element.module.value.impl

import net.ccbluex.liquidbounce.ui.client.gui.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.gui.newVer.element.components.Slider
import net.ccbluex.liquidbounce.ui.client.gui.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.IntegerValue

import java.awt.Color

class IntElement(val savedValue: IntegerValue): ValueElement<Int>(savedValue) {
    private val slider = Slider()
    private var dragged = false

    override fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, bgColor: Color, accentColor: Color): Float {
        val valueDisplay = 30F + Fonts.font40.getStringWidth("${savedValue.maximum}")
        val maxLength = Fonts.font40.getStringWidth("${savedValue.maximum}")
        val minLength = Fonts.font40.getStringWidth("${savedValue.minimum}")
        val nameLength = Fonts.font40.getStringWidth(value.name)
        val sliderWidth = width - 50F - nameLength - maxLength - minLength - valueDisplay
        val startPoint = x + width - 20F - sliderWidth - maxLength - valueDisplay
        if (dragged)
            savedValue.set((savedValue.minimum + (savedValue.maximum - savedValue.minimum) / sliderWidth * (mouseX - startPoint)).toInt().coerceIn(savedValue.minimum, savedValue.maximum))
        val currLength = Fonts.font40.getStringWidth("${savedValue.get()}${savedValue.suffix}")
        Fonts.font40.drawString(value.name, x + 10F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("${savedValue.maximum}", 
                                x + width - 10F - maxLength - valueDisplay, 
                                y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("${savedValue.minimum}", 
                                x + width - 30F - sliderWidth - maxLength - minLength - valueDisplay, 
                                y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        slider.setValue(savedValue.get().coerceIn(savedValue.minimum, savedValue.maximum).toFloat(), savedValue.minimum.toFloat(), savedValue.maximum.toFloat())
        slider.onDraw(x + width - 20F - sliderWidth - maxLength - valueDisplay, y + 10F, sliderWidth, accentColor)
        RenderUtils.originalRoundedRect(x + width - 5F - valueDisplay, y + 2F, x + width - 10F, y + 18F, 4F, ColorManager.button.rgb)
        RenderUtils.customRounded(x + width - 18F, y + 2F, x + width - 10F, y + 18F, 0F, 4F, 4F, 0F, ColorManager.buttonOutline.rgb)
        RenderUtils.customRounded(x + width - 5F - valueDisplay, y + 2F, x + width + 3F - valueDisplay, y + 18, 4F, 0F, 0F, 4F, ColorManager.buttonOutline.rgb)
        Fonts.font40.drawString("${savedValue.get()}", x + width + 6F - valueDisplay, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("-", x + width - 3F - valueDisplay, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)
        Fonts.font40.drawString("+", x + width - 17F, y + 10F - Fonts.font40.FONT_HEIGHT / 2F + 2F, -1)

        return valueHeight
    }

    override fun onClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        val valueDisplay = 30F + Fonts.font40.getStringWidth("${savedValue.maximum}${savedValue.suffix}")
        val maxLength = Fonts.font40.getStringWidth("${savedValue.maximum}${savedValue.suffix}")
        val minLength = Fonts.font40.getStringWidth("${savedValue.minimum}${savedValue.suffix}")
        val nameLength = Fonts.font40.getStringWidth(value.name)
        val sliderWidth = width - 50F - nameLength - maxLength - minLength - valueDisplay
        val startPoint = x + width - 30F - sliderWidth - valueDisplay - maxLength
        val endPoint = x + width - 10F - valueDisplay - maxLength

        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, startPoint, y + 5F, endPoint, y + 15F))
            dragged = true
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, x + width - 5F - valueDisplay, y + 2F, x + width + 3F - valueDisplay, y + 18F))
            savedValue.set((savedValue.get() - 1).coerceIn(savedValue.minimum, savedValue.maximum))
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, x + width - 18F, y + 2F, x + width - 10F, y + 18F))
            savedValue.set((savedValue.get() + 1).coerceIn(savedValue.minimum, savedValue.maximum))
    }

    override fun onRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float) {
        if (dragged) dragged = false
    }
}