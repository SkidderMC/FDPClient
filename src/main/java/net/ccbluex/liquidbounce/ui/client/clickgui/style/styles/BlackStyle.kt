/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule.scale
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui.clamp
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.style.Style
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold35
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.blendColors
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexture
import net.ccbluex.liquidbounce.utils.render.RenderUtils.updateTextureCache
import net.ccbluex.liquidbounce.utils.ui.EditableText
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@SideOnly(Side.CLIENT)
object BlackStyle : Style() {
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        drawBorderedRect(
            panel.x, panel.y - 3, panel.x + panel.width, panel.y + 17, 3, Color(20, 20, 20).rgb, Color(20, 20, 20).rgb
        )

        if (panel.fade > 0) {
            drawBorderedRect(
                panel.x,
                panel.y + 17,
                panel.x + panel.width,
                panel.y + 19 + panel.fade,
                3,
                Color(40, 40, 40).rgb,
                Color(40, 40, 40).rgb
            )
            drawBorderedRect(
                panel.x,
                panel.y + 17 + panel.fade,
                panel.x + panel.width,
                panel.y + 24 + panel.fade,
                3,
                Color(20, 20, 20).rgb,
                Color(20, 20, 20).rgb
            )
        }

        val xPos = panel.x - (fontSemibold35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)) - 100) / 2
        fontSemibold35.drawString(panel.name, xPos, panel.y + 2, Color.WHITE.rgb)
    }

    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val lines = text.lines()

        val width = lines.maxOfOrNull { fontSemibold35.getStringWidth(it) + 14 }
            ?: return // Makes no sense to render empty lines
        val height = (fontSemibold35.fontHeight * lines.size) + 3

        // Don't draw hover text beyond window boundaries
        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val x = mouseX.clamp(0, (scaledWidth / scale - width).roundToInt())
        val y = mouseY.clamp(0, (scaledHeight / scale - height).roundToInt())

        drawBorderedRect(x + 9, y, x + width, y + height, 3, Color(40, 40, 40).rgb, Color(40, 40, 40).rgb)

        lines.forEachIndexed { index, text ->
            fontSemibold35.drawString(text, x + 12, y + 3 + (fontSemibold35.fontHeight) * index, Color.WHITE.rgb)
        }
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        drawRect(
            buttonElement.x - 1,
            buttonElement.y - 1,
            buttonElement.x + buttonElement.width + 1,
            buttonElement.y + buttonElement.height + 1,
            getHoverColor(
                if (buttonElement.color != Int.MAX_VALUE) Color(20, 20, 20) else Color(40, 40, 40),
                buttonElement.hoverTime
            )
        )

        fontSemibold35.drawString(buttonElement.displayName, buttonElement.x + 5, buttonElement.y + 5, Color.WHITE.rgb)
    }

    override fun drawModuleElementAndClick(
        mouseX: Int, mouseY: Int, moduleElement: ModuleElement, mouseButton: Int?
    ): Boolean {
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(Color(40, 40, 40), moduleElement.hoverTime)
        )
        drawRect(
            moduleElement.x - 1,
            moduleElement.y - 1,
            moduleElement.x + moduleElement.width + 1,
            moduleElement.y + moduleElement.height + 1,
            getHoverColor(
                Color(20, 20, 20, moduleElement.slowlyFade), moduleElement.hoverTime, !moduleElement.module.isActive
            )
        )

        fontSemibold35.drawString(
            moduleElement.displayName,
            moduleElement.x + 5,
            moduleElement.y + 5,
            if (moduleElement.module.state && !moduleElement.module.isActive) Color(255, 255, 255, 128).rgb
            else Color.WHITE.rgb
        )

        // Draw settings
        val moduleValues = moduleElement.module.values.filter { it.shouldRender() }
        if (moduleValues.isNotEmpty()) {
            fontSemibold35.drawString(
                if (moduleElement.showSettings) "<" else ">",
                moduleElement.x + moduleElement.width - 8,
                moduleElement.y + 5,
                Color.WHITE.rgb
            )

            if (moduleElement.showSettings) {
                var yPos = moduleElement.y + 6

                val minX = moduleElement.x + moduleElement.width + 4
                val maxX = moduleElement.x + moduleElement.width + moduleElement.settingsWidth

                if (moduleElement.settingsWidth > 0 && moduleElement.settingsHeight > 0) drawBorderedRect(
                    minX,
                    yPos,
                    maxX,
                    yPos + moduleElement.settingsHeight,
                    3,
                    Color(20, 20, 20).rgb,
                    Color(40, 40, 40).rgb
                )

                for (value in moduleValues) {
                    assumeNonVolatile = value.get() is Number

                    val suffix = value.suffix ?: ""

                    when (value) {
                        is BoolValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                value.toggle()
                                clickSound()
                                return true
                            }

                            fontSemibold35.drawString(
                                text, minX + 2, yPos + 2, if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE
                            )

                            yPos += 11
                        }

                        is ListValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 16

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + fontSemibold35.fontHeight) {
                                value.openList = !value.openList
                                clickSound()
                                return true
                            }

                            fontSemibold35.drawString(text, minX + 2, yPos + 2, Color.WHITE.rgb)
                            fontSemibold35.drawString(
                                if (value.openList) "-" else "+",
                                (maxX - if (value.openList) 5 else 6),
                                yPos + 2,
                                Color.WHITE.rgb
                            )

                            yPos += fontSemibold35.fontHeight + 1

                            for (valueOfList in value.values) {
                                moduleElement.settingsWidth = fontSemibold35.getStringWidth("> $valueOfList") + 12

                                if (value.openList) {
                                    if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 9) {
                                        value.set(valueOfList)
                                        clickSound()
                                        return true
                                    }

                                    fontSemibold35.drawString(
                                        "> $valueOfList",
                                        minX + 2,
                                        yPos + 2,
                                        if (value.get() == valueOfList) Color.WHITE.rgb else Int.MAX_VALUE
                                    )

                                    yPos += fontSemibold35.fontHeight + 1
                                }
                            }
                            if (!value.openList) {
                                yPos += 1
                            }
                        }

                        is FloatValue -> {
                            val text = value.name + "§f: " + round(value.get()) + " §7$suffix"

                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                (x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)).roundToInt()

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.setAndSaveValueOnButtonRelease(
                                    round(value.minimum + (value.maximum - value.minimum) * percentage).coerceIn(
                                        value.range
                                    )
                                )

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            fontSemibold35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is BlockValue -> {
                            val text =
                                value.name + "§f: " + getBlockName(value.get()) + " (" + value.get() + ")" + " §7$suffix"

                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.setAndSaveValueOnButtonRelease(
                                    value.range.lerpWith(percentage).roundToInt().coerceIn(value.range)
                                )

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            fontSemibold35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntValue -> {
                            val text = value.name + "§f: " + value.get() + " §7$suffix"

                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.setAndSaveValueOnButtonRelease(value.lerpWith(percentage).coerceIn(value.range))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            fontSemibold35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntRangeValue -> {
                            val slider1 = value.get().first
                            val slider2 = value.get().last

                            val text = "${value.name}§f: $slider1 - $slider2 §7$suffix"
                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)
                            val endX = x + width

                            val currSlider = value.lastChosenSlider

                            if (mouseButton == 0 && mouseX in x..endX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val leftSliderPos =
                                    x + (slider1 - value.minimum).toFloat() / (value.maximum - value.minimum) * (endX - x)
                                val rightSliderPos =
                                    x + (slider2 - value.minimum).toFloat() / (value.maximum - value.minimum) * (endX - x)

                                val distToSlider1 = mouseX - leftSliderPos
                                val distToSlider2 = mouseX - rightSliderPos

                                val closerToLeft = abs(distToSlider1) < abs(distToSlider2)

                                val isOnLeftSlider =
                                    (mouseX.toFloat() in x.toFloat()..leftSliderPos || closerToLeft) && rightSliderPos > x
                                val isOnRightSlider =
                                    (mouseX.toFloat() in rightSliderPos..endX.toFloat() || !closerToLeft) && leftSliderPos < endX

                                val percentage = (mouseX.toFloat() - x) / (endX - x)

                                if (isOnLeftSlider && currSlider == null || currSlider == RangeSlider.LEFT) {
                                    withDelayedSave {
                                        value.setFirst(
                                            value.lerpWith(percentage).coerceIn(value.minimum, slider2), false
                                        )
                                    }
                                }

                                if (isOnRightSlider && currSlider == null || currSlider == RangeSlider.RIGHT) {
                                    withDelayedSave {
                                        value.setLast(
                                            value.lerpWith(percentage).coerceIn(slider1, value.maximum), false
                                        )
                                    }
                                }

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) {
                                    value.lastChosenSlider = when {
                                        isOnLeftSlider -> RangeSlider.LEFT
                                        isOnRightSlider -> RangeSlider.RIGHT
                                        else -> null
                                    }
                                    return true
                                }
                            }

                            val sliderValue1 = x + width * (slider1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 = x + width * (slider2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1, y + 1, 3f, color)
                            drawFilledCircle(sliderValue2, y + 1, 3f, color)

                            fontSemibold35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FloatRangeValue -> {
                            val slider1 = value.get().start
                            val slider2 = value.get().endInclusive

                            val text = "${value.name}§f: ${round(slider1)} - ${round(slider2)} §7$suffix"
                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val endX = x + width

                            val currSlider = value.lastChosenSlider

                            if (mouseButton == 0 && mouseX in x..endX && mouseY in y - 2..y + 5 || sliderValueHeld == value) {
                                val leftSliderPos =
                                    x + (slider1 - value.minimum) / (value.maximum - value.minimum) * (endX - x)
                                val rightSliderPos =
                                    x + (slider2 - value.minimum) / (value.maximum - value.minimum) * (endX - x)

                                val distToSlider1 = mouseX - leftSliderPos
                                val distToSlider2 = mouseX - rightSliderPos

                                val closerToLeft = abs(distToSlider1) < abs(distToSlider2)

                                val isOnLeftSlider =
                                    (mouseX.toFloat() in x.toFloat()..leftSliderPos || closerToLeft) && rightSliderPos > x
                                val isOnRightSlider =
                                    (mouseX.toFloat() in rightSliderPos..endX.toFloat() || !closerToLeft) && leftSliderPos < endX

                                val percentage = (mouseX.toFloat() - x) / (endX - x)

                                if (isOnLeftSlider && currSlider == null || currSlider == RangeSlider.LEFT) {
                                    withDelayedSave {
                                        value.setFirst(
                                            value.lerpWith(percentage).coerceIn(value.minimum, slider2), false
                                        )
                                    }
                                }

                                if (isOnRightSlider && currSlider == null || currSlider == RangeSlider.RIGHT) {
                                    withDelayedSave {
                                        value.setLast(
                                            value.lerpWith(percentage).coerceIn(slider1, value.maximum), false
                                        )
                                    }
                                }

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) {
                                    value.lastChosenSlider = when {
                                        isOnLeftSlider -> RangeSlider.LEFT
                                        isOnRightSlider -> RangeSlider.RIGHT
                                        else -> null
                                    }
                                    return true
                                }
                            }

                            val sliderValue1 = x + width * (slider1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 = x + width * (slider2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y.toFloat(), sliderValue2, y + 2f, color.rgb)
                            drawFilledCircle(sliderValue1.roundToInt(), y + 1, 3f, color)
                            drawFilledCircle(sliderValue2.roundToInt(), y + 1, 3f, color)

                            fontSemibold35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FontValue -> {
                            val displayString = value.displayName
                            moduleElement.settingsWidth = fontSemibold35.getStringWidth(displayString) + 8

                            fontSemibold35.drawString(displayString, minX + 2, yPos + 2, Color.WHITE.rgb)

                            if (mouseButton != null && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                if (mouseButton == 0) value.next() else value.previous()
                                clickSound()
                                return true
                            }

                            yPos += 11
                        }

                        is ColorValue -> {
                            val currentColor = value.selectedColor()

                            val spacing = 12

                            val startX = moduleElement.x + moduleElement.width + 4
                            val startY = yPos - 1

                            // Color preview
                            val colorPreviewSize = 9
                            val colorPreviewX2 = maxX - colorPreviewSize
                            val colorPreviewX1 = colorPreviewX2 - colorPreviewSize
                            val colorPreviewY1 = startY + 1
                            val colorPreviewY2 = colorPreviewY1 + colorPreviewSize

                            val rainbowPreviewX2 = colorPreviewX1 - colorPreviewSize
                            val rainbowPreviewX1 = rainbowPreviewX2 - colorPreviewSize

                            // Text
                            val textX = startX + 2F
                            val textY = startY + 3F

                            // Sliders
                            val hueSliderWidth = 7
                            val hueSliderHeight = 50
                            val colorPickerWidth = 75
                            val colorPickerHeight = 50

                            val spacingBetweenSliders = 5

                            val rgbaOptionHeight = if (value.showOptions) fontSemibold35.height * 4 else 0

                            val colorPickerStartX = textX.toInt()
                            val colorPickerEndX = colorPickerStartX + colorPickerWidth
                            val colorPickerStartY = rgbaOptionHeight + colorPreviewY2 + spacing / 3
                            val colorPickerEndY = colorPickerStartY + colorPickerHeight

                            val hueSliderStartY = colorPickerStartY
                            val hueSliderEndY = colorPickerStartY + hueSliderHeight

                            val hueSliderX = colorPickerEndX + spacingBetweenSliders

                            val opacityStartX = hueSliderX + hueSliderWidth + spacingBetweenSliders
                            val opacityEndX = opacityStartX + hueSliderWidth

                            val rainbow = value.rainbow

                            if (mouseButton in arrayOf(0, 1)) {
                                val isColorPreview =
                                    mouseX in colorPreviewX1..colorPreviewX2 && mouseY in colorPreviewY1..colorPreviewY2
                                val isRainbowPreview =
                                    mouseX in rainbowPreviewX1..rainbowPreviewX2 && mouseY in colorPreviewY1..colorPreviewY2

                                when {
                                    isColorPreview -> {
                                        if (mouseButton == 0 && rainbow) value.rainbow = false
                                        if (mouseButton == 1) value.showPicker = !value.showPicker
                                        clickSound()
                                        return true
                                    }

                                    isRainbowPreview -> {
                                        if (mouseButton == 0) value.rainbow = true
                                        if (mouseButton == 1) value.showPicker = !value.showPicker
                                        clickSound()
                                        return true
                                    }
                                }
                            }


                            val startText = "${value.name}: "
                            val valueText = "#%08X".format(currentColor.rgb)
                            val combinedText = startText + valueText

                            val combinedWidth = opacityEndX - colorPickerStartX
                            val optimalWidth = maxOf(fontSemibold35.getStringWidth(combinedText), combinedWidth)
                            moduleElement.settingsWidth = optimalWidth + spacing * 4

                            val valueX = startX + fontSemibold35.getStringWidth(startText)
                            val valueWidth = fontSemibold35.getStringWidth(valueText)

                            if (mouseButton == 1 && mouseX in valueX..valueX + valueWidth && mouseY.toFloat() in textY - 2..textY + fontSemibold35.height - 3F) {
                                value.showOptions = !value.showOptions

                                if (!value.showOptions) {
                                    resetChosenText(value)
                                }
                            }

                            val widestLabel = rgbaLabels.maxOf { fontSemibold35.getStringWidth(it) }

                            var highlightCursor = {}

                            chosenText?.let {
                                if (it.value != value) {
                                    return@let
                                }

                                val startValueX = textX + widestLabel + 3
                                val cursorY = textY + value.rgbaIndex * fontSemibold35.height + 10

                                if (it.selectionActive()) {
                                    val start =
                                        startValueX + fontSemibold35.getStringWidth(it.string.take(it.selectionStart!!))
                                    val end =
                                        startValueX + fontSemibold35.getStringWidth(it.string.take(it.selectionEnd!!))
                                    drawRect(
                                        start,
                                        cursorY - 3f,
                                        end,
                                        cursorY + fontSemibold35.fontHeight - 2,
                                        Color(7, 152, 252).rgb
                                    )
                                }

                                highlightCursor = {
                                    val cursorX = startValueX + fontSemibold35.getStringWidth(it.cursorString)
                                    drawRect(
                                        cursorX,
                                        cursorY - 3F,
                                        cursorX + 1F,
                                        cursorY + fontSemibold35.fontHeight - 2,
                                        Color.WHITE.rgb
                                    )
                                }
                            }

                            if (value.showOptions) {
                                val mainColor = value.get()
                                val rgbaValues = listOf(mainColor.red, mainColor.green, mainColor.blue, mainColor.alpha)
                                val rgbaYStart = textY + 10

                                var noClickAmount = 0

                                val maxWidth = fontSemibold35.getStringWidth("255")

                                rgbaLabels.forEachIndexed { index, label ->
                                    val rgbaValueText = "${rgbaValues[index]}"
                                    val colorX = textX + widestLabel + 4
                                    val yPosition = rgbaYStart + index * fontSemibold35.height

                                    val isEmpty =
                                        chosenText?.value == value && value.rgbaIndex == index && chosenText?.string.isNullOrEmpty()

                                    val extraSpacing = if (isEmpty) maxWidth + 4 else 0
                                    val finalX = colorX + extraSpacing

                                    val defaultText = if (isEmpty) "($rgbaValueText)" else rgbaValueText
                                    fontSemibold35.drawString(label, textX, yPosition, Color.WHITE.rgb)
                                    fontSemibold35.drawString(defaultText, finalX, yPosition, Color.LIGHT_GRAY.rgb)

                                    if (mouseButton == 0) {
                                        if (mouseX.toFloat() in finalX..finalX + maxWidth && mouseY.toFloat() in yPosition - 2..yPosition + 6) {
                                            chosenText = EditableText.forRGBA(value, index)
                                        } else {
                                            noClickAmount++
                                        }
                                    }
                                }

                                // Were none of these labels clicked on?
                                if (noClickAmount == rgbaLabels.size) {
                                    resetChosenText(value)
                                }
                            }

                            fontSemibold35.drawString(combinedText, textX, textY, Color.WHITE.rgb)

                            highlightCursor()

                            val normalBorderColor = if (rainbow) 0 else Color.BLUE.rgb
                            val rainbowBorderColor = if (rainbow) Color.BLUE.rgb else 0

                            val hue = if (rainbow) {
                                Color.RGBtoHSB(currentColor.red, currentColor.green, currentColor.blue, null)[0]
                            } else {
                                value.hueSliderY
                            }

                            if (value.showPicker) {
                                // Color Picker
                                value.updateTextureCache(
                                    id = 0,
                                    hue = hue,
                                    width = colorPickerWidth,
                                    height = colorPickerHeight,
                                    generateImage = { image, _ ->
                                        for (px in 0 until colorPickerWidth) {
                                            for (py in 0 until colorPickerHeight) {
                                                val localS = px / colorPickerWidth.toFloat()
                                                val localB = 1.0f - (py / colorPickerHeight.toFloat())
                                                val rgb = Color.HSBtoRGB(hue, localS, localB)
                                                image.setRGB(px, py, rgb)
                                            }
                                        }
                                    },
                                    drawAt = { id ->
                                        drawTexture(
                                            id,
                                            colorPickerStartX,
                                            colorPickerStartY,
                                            colorPickerWidth,
                                            colorPickerHeight
                                        )
                                    })

                                val markerX = (colorPickerStartX..colorPickerEndX).lerpWith(value.colorPickerPos.x)
                                val markerY = (colorPickerStartY..colorPickerEndY).lerpWith(value.colorPickerPos.y)

                                if (!rainbow) {
                                    RenderUtils.drawBorder(
                                        markerX - 2f, markerY - 2f, markerX + 3f, markerY + 3f, 1.5f, Color.WHITE.rgb
                                    )
                                }

                                // Hue slider
                                value.updateTextureCache(
                                    id = 1,
                                    hue = hue,
                                    width = hueSliderWidth,
                                    height = hueSliderHeight,
                                    generateImage = { image, _ ->
                                        for (y in 0 until hueSliderHeight) {
                                            for (x in 0 until hueSliderWidth) {
                                                val localHue = y / hueSliderHeight.toFloat()
                                                val rgb = Color.HSBtoRGB(localHue, 1.0f, 1.0f)
                                                image.setRGB(x, y, rgb)
                                            }
                                        }
                                    },
                                    drawAt = { id ->
                                        drawTexture(
                                            id, hueSliderX, colorPickerStartY, hueSliderWidth, hueSliderHeight
                                        )
                                    })

                                // Opacity slider
                                value.updateTextureCache(
                                    id = 2,
                                    hue = currentColor.rgb.toFloat(),
                                    width = hueSliderWidth,
                                    height = hueSliderHeight,
                                    generateImage = { image, _ ->
                                        val gridSize = 1

                                        for (y in 0 until hueSliderHeight) {
                                            for (x in 0 until hueSliderWidth) {
                                                val gridX = x / gridSize
                                                val gridY = y / gridSize

                                                val checkerboardColor = if ((gridY + gridX) % 2 == 0) {
                                                    Color.WHITE.rgb
                                                } else {
                                                    Color.BLACK.rgb
                                                }

                                                val alpha =
                                                    ((1 - y.toFloat() / hueSliderHeight.toFloat()) * 255).roundToInt()

                                                val finalColor = blendColors(
                                                    Color(checkerboardColor), currentColor.withAlpha(alpha)
                                                )

                                                image.setRGB(x, y, finalColor.rgb)
                                            }
                                        }
                                    },
                                    drawAt = { id ->
                                        drawTexture(
                                            id, opacityStartX, colorPickerStartY, hueSliderWidth, hueSliderHeight
                                        )
                                    })

                                val opacityMarkerY = (hueSliderStartY..hueSliderEndY).lerpWith(1 - value.opacitySliderY)
                                val hueMarkerY = (hueSliderStartY..hueSliderEndY).lerpWith(hue)

                                RenderUtils.drawBorder(
                                    hueSliderX.toFloat() - 1,
                                    hueMarkerY - 1f,
                                    hueSliderX + hueSliderWidth + 1f,
                                    hueMarkerY + 1f,
                                    1.5f,
                                    Color.WHITE.rgb,
                                )

                                RenderUtils.drawBorder(
                                    opacityStartX.toFloat() - 1,
                                    opacityMarkerY - 1f,
                                    opacityEndX + 1f,
                                    opacityMarkerY + 1f,
                                    1.5f,
                                    Color.WHITE.rgb,
                                )

                                val inColorPicker =
                                    mouseX in colorPickerStartX until colorPickerEndX && mouseY in colorPickerStartY until colorPickerEndY && !rainbow
                                val inHueSlider =
                                    mouseX in hueSliderX - 1..hueSliderX + hueSliderWidth + 1 && mouseY in hueSliderStartY until hueSliderEndY && !rainbow
                                val inOpacitySlider =
                                    mouseX in opacityStartX - 1..opacityEndX + 1 && mouseY in hueSliderStartY until hueSliderEndY

                                // Must be outside the if statements below since we check for mouse button state.
                                // If it's inside the statement, it will not update the mouse button state on time.
                                val sliderType = value.lastChosenSlider

                                if (mouseButton == 0 && (inColorPicker || inHueSlider || inOpacitySlider) || sliderValueHeld == value && value.lastChosenSlider != null) {
                                    if (inColorPicker && sliderType == null || sliderType == ColorValue.SliderType.COLOR) {
                                        val newS = ((mouseX - colorPickerStartX) / colorPickerWidth.toFloat()).coerceIn(
                                            0f, 1f
                                        )
                                        val newB =
                                            (1.0f - (mouseY - colorPickerStartY) / colorPickerHeight.toFloat()).coerceIn(
                                                0f, 1f
                                            )
                                        value.colorPickerPos.x = newS
                                        value.colorPickerPos.y = 1 - newB
                                    }

                                    var finalColor = Color(
                                        Color.HSBtoRGB(
                                            value.hueSliderY, value.colorPickerPos.x, 1 - value.colorPickerPos.y
                                        )
                                    )

                                    if (inHueSlider && sliderType == null || sliderType == ColorValue.SliderType.HUE) {
                                        value.hueSliderY =
                                            ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(
                                                0f, 1f
                                            )

                                        finalColor = Color(
                                            Color.HSBtoRGB(
                                                value.hueSliderY, value.colorPickerPos.x, 1 - value.colorPickerPos.y
                                            )
                                        )
                                    }

                                    if (inOpacitySlider && sliderType == null || sliderType == ColorValue.SliderType.OPACITY) {
                                        value.opacitySliderY =
                                            1 - ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(
                                                0f, 1f
                                            )
                                    }

                                    finalColor = finalColor.withAlpha((value.opacitySliderY * 255).roundToInt())

                                    sliderValueHeld = value

                                    value.setAndSaveValueOnButtonRelease(finalColor)

                                    if (mouseButton == 0) {
                                        value.lastChosenSlider = when {
                                            inColorPicker && !rainbow -> ColorValue.SliderType.COLOR
                                            inHueSlider && !rainbow -> ColorValue.SliderType.HUE
                                            inOpacitySlider -> ColorValue.SliderType.OPACITY
                                            else -> null
                                        }
                                        return true
                                    }
                                }
                                yPos += colorPickerHeight + colorPreviewSize - 6
                            }
                            drawBorderedRect(
                                colorPreviewX1,
                                colorPreviewY1,
                                colorPreviewX2,
                                colorPreviewY2,
                                1.5,
                                normalBorderColor,
                                value.get().rgb
                            )

                            drawBorderedRect(
                                rainbowPreviewX1,
                                colorPreviewY1,
                                rainbowPreviewX2,
                                colorPreviewY2,
                                1.5f,
                                rainbowBorderColor,
                                ColorUtils.rainbow(alpha = value.opacitySliderY).rgb
                            )

                            yPos += spacing + rgbaOptionHeight
                        }

                        else -> {
                            val startText = value.name + "§f: "
                            var valueText = "${value.get()}"

                            val combinedWidth = fontSemibold35.getStringWidth(startText + valueText)

                            moduleElement.settingsWidth = combinedWidth + 8

                            val textY = yPos + 4
                            val startX = minX + 2
                            var textX = startX + fontSemibold35.getStringWidth(startText)

                            if (mouseButton == 0) {
                                chosenText =
                                    if (mouseX in textX..maxX && mouseY in textY - 2..textY + 6 && value is TextValue) {
                                        EditableText.forTextValue(value)
                                    } else {
                                        null
                                    }
                            }

                            val shouldPushToRight =
                                value is TextValue && chosenText?.value == value && chosenText?.string != value.get()

                            var highlightCursor: (Int) -> Unit = {}

                            chosenText?.let {
                                if (it.value != value) {
                                    return@let
                                }

                                val input = it.string

                                if (it.selectionActive()) {
                                    val start =
                                        textX - 1 + fontSemibold35.getStringWidth(input.take(it.selectionStart!!))
                                    val end = textX - 1 + fontSemibold35.getStringWidth(input.take(it.selectionEnd!!))
                                    drawRect(
                                        start,
                                        textY - 3,
                                        end,
                                        textY + fontSemibold35.fontHeight - 2,
                                        Color(7, 152, 252).rgb
                                    )
                                }

                                highlightCursor = { textX ->
                                    val cursorX = textX + fontSemibold35.getStringWidth(input.take(it.cursorIndex))
                                    drawRect(
                                        cursorX,
                                        textY - 3,
                                        cursorX + 1,
                                        textY + fontSemibold35.fontHeight - 2,
                                        Color.WHITE.rgb
                                    )
                                }
                            }

                            fontSemibold35.drawString(startText, startX, textY, Color.WHITE.rgb)

                            val defaultColor = if (shouldPushToRight) Color.LIGHT_GRAY else Color.WHITE

                            val originalX = textX - 1

                            // This usually happens when a value rejects a change and auto-sets it to a default value.
                            if (shouldPushToRight) {
                                valueText = "($valueText)"
                                val valueWidth = fontSemibold35.getStringWidth(valueText)
                                moduleElement.settingsWidth = combinedWidth + valueWidth + 12
                                fontSemibold35.drawString(chosenText!!.string, textX, textY, Color.WHITE.rgb)
                                textX += valueWidth + 4
                            }

                            fontSemibold35.drawString(valueText, textX, textY, defaultColor.rgb)

                            highlightCursor(originalX)

                            yPos += 12
                        }
                    }
                }

                moduleElement.adjustWidth()

                moduleElement.settingsHeight = yPos - moduleElement.y - 6

                if (mouseButton != null && mouseX in minX..maxX && mouseY in moduleElement.y + 6..yPos + 2) return true
            }
        }

        if (mouseButton == -1) {
            sliderValueHeld = null
        }

        return false
    }
}