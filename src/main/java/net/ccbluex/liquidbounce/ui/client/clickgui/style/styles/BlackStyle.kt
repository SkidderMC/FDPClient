/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
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
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.extensions.component1
import net.ccbluex.liquidbounce.utils.extensions.component2
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledCircle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
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

        val xPos = panel.x - (font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)) - 100) / 2
        font35.drawString(panel.name, xPos, panel.y + 4, Color.WHITE.rgb)
    }

    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val lines = text.lines()

        val width =
            lines.maxOfOrNull { font35.getStringWidth(it) + 14 } ?: return // Makes no sense to render empty lines
        val height = (font35.fontHeight * lines.size) + 3

        // Don't draw hover text beyond window boundaries
        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val x = mouseX.clamp(0, (scaledWidth / scale - width).roundToInt())
        val y = mouseY.clamp(0, (scaledHeight / scale - height).roundToInt())

        drawBorderedRect(x + 9, y, x + width, y + height, 3, Color(40, 40, 40).rgb, Color(40, 40, 40).rgb)

        lines.forEachIndexed { index, text ->
            font35.drawString(text, x + 12, y + 3 + (font35.fontHeight) * index, Color.WHITE.rgb)
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

        font35.drawString(buttonElement.displayName, buttonElement.x + 5, buttonElement.y + 5, Color.WHITE.rgb)
    }

    override fun drawModuleElementAndClick(
        mouseX: Int,
        mouseY: Int,
        moduleElement: ModuleElement,
        mouseButton: Int?
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
                Color(20, 20, 20, moduleElement.slowlyFade),
                moduleElement.hoverTime,
                !moduleElement.module.isActive
            )
        )

        font35.drawString(
            moduleElement.displayName, moduleElement.x + 5, moduleElement.y + 5,
            if (moduleElement.module.state && !moduleElement.module.isActive) Color(255, 255, 255, 128).rgb
            else Color.WHITE.rgb
        )

        // Draw settings
        val moduleValues = moduleElement.module.values.filter { it.shouldRender() }
        if (moduleValues.isNotEmpty()) {
            font35.drawString(
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

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                value.toggle()
                                clickSound()
                                return true
                            }

                            font35.drawString(
                                text, minX + 2, yPos + 2, if (value.get()) Color.WHITE.rgb else Int.MAX_VALUE
                            )

                            yPos += 11
                        }

                        is ListValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 16

                            if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + font35.fontHeight) {
                                value.openList = !value.openList
                                clickSound()
                                return true
                            }

                            font35.drawString(text, minX + 2, yPos + 2, Color.WHITE.rgb)
                            font35.drawString(
                                if (value.openList) "-" else "+",
                                (maxX - if (value.openList) 5 else 6),
                                yPos + 2,
                                Color.WHITE.rgb
                            )

                            yPos += font35.fontHeight + 1

                            for (valueOfList in value.values) {
                                moduleElement.settingsWidth = font35.getStringWidth("> $valueOfList") + 12

                                if (value.openList) {
                                    if (mouseButton == 0 && mouseX in minX..maxX && mouseY in yPos..yPos + 9) {
                                        value.set(valueOfList)
                                        clickSound()
                                        return true
                                    }

                                    font35.drawString(
                                        "> $valueOfList",
                                        minX + 2,
                                        yPos + 2,
                                        if (value.get() == valueOfList) Color.WHITE.rgb else Int.MAX_VALUE
                                    )

                                    yPos += font35.fontHeight + 1
                                }
                            }
                            if (!value.openList) {
                                yPos += 1
                            }
                        }

                        is FloatValue -> {
                            val text = value.name + "§f: " + round(value.get()) + " §7$suffix"

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                (x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)).roundToInt()

                            if ((mouseButton == 0 || sliderValueHeld == value)
                                && mouseX in minX..maxX
                                && mouseY in yPos + 15..yPos + 21
                            ) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(
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

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerValue -> {
                            val text = value.name + "§f: " + if (value is BlockValue) {
                                getBlockName(value.get()) + " (" + value.get() + ")"
                            } else {
                                value.get()
                            } + " §7$suffix"

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue =
                                x + width * (displayValue - value.minimum) / (value.maximum - value.minimum)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX in x..x + width && mouseY in y - 2..y + 5) {
                                val percentage = (mouseX - x) / width.toFloat()
                                value.set(
                                    (value.minimum + (value.maximum - value.minimum) * percentage).roundToInt()
                                        .coerceIn(value.range)
                                )

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(x, y, sliderValue, y + 2, color.rgb)
                            drawFilledCircle(sliderValue, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 3, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is IntegerRangeValue -> {
                            val slider1 = value.get().first
                            val slider2 = value.get().last

                            val text = "${value.name}§f: $slider1 - $slider2 §7$suffix§f (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4
                            val y = yPos + 14
                            val width = moduleElement.settingsWidth - 12
                            val color = Color(20, 20, 20)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX in x..x + width && mouseY in y - 2..y + 5) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum).toFloat() / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().first
                            val displayValue2 = value.get().last

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1, y + 1, 3f, color)
                            drawFilledCircle(sliderValue2, y + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FloatRangeValue -> {
                            val slider1 = value.get().start
                            val slider2 = value.get().endInclusive

                            val text = "${value.name}§f: ${round(slider1)} - ${round(slider2)} §7$suffix§f (Beta)"
                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            val x = minX + 4f
                            val y = yPos + 14f
                            val width = moduleElement.settingsWidth - 12f
                            val color = Color(20, 20, 20)

                            if ((mouseButton == 0 || sliderValueHeld == value) && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y - 2..y + 5) {
                                val slider1Pos =
                                    minX + ((slider1 - value.minimum) / (value.maximum - value.minimum)) * (maxX - minX)
                                val slider2Pos =
                                    minX + ((slider2 - value.minimum) / (value.maximum - value.minimum)) * (maxX - minX)

                                val distToSlider1 = mouseX - slider1Pos
                                val distToSlider2 = mouseX - slider2Pos

                                val percentage = (mouseX - minX - 4F) / (maxX - minX - 8F)

                                if (abs(distToSlider1) <= abs(distToSlider2) && distToSlider2 <= 0) {
                                    value.setFirst(value.lerpWith(percentage).coerceIn(value.minimum, slider2))
                                } else value.setLast(value.lerpWith(percentage).coerceIn(slider1, value.maximum))

                                sliderValueHeld = value

                                if (mouseButton == 0) return true
                            }

                            val displayValue1 = value.get().start
                            val displayValue2 = value.get().endInclusive

                            val sliderValue1 =
                                x + width * (displayValue1 - value.minimum) / (value.maximum - value.minimum)
                            val sliderValue2 =
                                x + width * (displayValue2 - value.minimum) / (value.maximum - value.minimum)

                            drawRect(x, y, x + width, y + 2, Int.MAX_VALUE)
                            drawRect(sliderValue1, y, sliderValue2, y + 2, color.rgb)
                            drawFilledCircle(sliderValue1.roundToInt(), y.roundToInt() + 1, 3f, color)
                            drawFilledCircle(sliderValue2.roundToInt(), y.roundToInt() + 1, 3f, color)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 19
                        }

                        is FontValue -> {
                            val displayString = value.displayName
                            moduleElement.settingsWidth = font35.getStringWidth(displayString) + 8

                            font35.drawString(displayString, minX + 2, yPos + 2, Color.WHITE.rgb)

                            if (mouseButton != null && mouseX in minX..maxX && mouseY in yPos..yPos + 12) {
                                if (mouseButton == 0) value.next() else value.previous()
                                clickSound()
                                return true
                            }

                            yPos += 11
                        }

                        is ColorValue -> {
                            val colorValue = value

                            val currentColor = if (colorValue.rainbow) {
                                ColorUtils.rainbow()
                            } else {
                                colorValue.get()
                            }

                            val previewSize = 10
                            val previewX1 = moduleElement.x + moduleElement.width + 4
                            val previewY1 = yPos + 2
                            val previewX2 = previewX1 + previewSize
                            val previewY2 = previewY1 + previewSize

                            drawRect(previewX1, previewY1, previewX2, previewY2, currentColor.rgb)

                            if (mouseButton == 0 && mouseX in previewX1..previewX2 && mouseY in previewY1..previewY2) {
                                colorValue.showPicker = !colorValue.showPicker
                                clickSound()
                                return true
                            }

                            if (!colorValue.showPicker) {
                                yPos += (previewSize + 6)
                                return false
                            }

                            if (!colorValue.rainbow) {
                                val hsb = Color.RGBtoHSB(currentColor.red, currentColor.green, currentColor.blue, null)
                                var hue = hsb[0]
                                var saturation = hsb[1]
                                var brightness = hsb[2]

                                val colorPickerWidth = 75
                                val colorPickerHeight = 50
                                val hueSliderWidth = 7
                                val hueSliderHeight = 50

                                val startX = previewX1
                                val startY = previewY1 + previewSize + 4

                                font35.drawCenteredStringWithShadow(
                                    "Color",
                                    (startX + 1).toFloat(),
                                    (startY - 10).toFloat(),
                                    0xFFFFFFFF.toInt()
                                )

                                for (px in 0 until colorPickerWidth) {
                                    for (py in 0 until colorPickerHeight) {
                                        val localS = px / colorPickerWidth.toFloat()
                                        val localB = 1.0f - (py / colorPickerHeight.toFloat())
                                        val rgb = Color.HSBtoRGB(hue, localS, localB)
                                        drawRect(startX + px, startY + py, startX + px + 1, startY + py + 1, rgb)
                                    }
                                }

                                val markerX = startX + (saturation * colorPickerWidth).toInt()
                                val markerY = startY + ((1.0f - brightness) * colorPickerHeight).toInt()
                                RenderUtils.drawBorderedRectRGB(
                                    markerX - 2, markerY - 2,
                                    markerX + 3, markerY + 3,
                                    1.0f,
                                    0xFFFFFFFF.toInt(),
                                    0x00000000
                                )

                                val hueSliderX = startX + colorPickerWidth + 5
                                val hueSliderY = startY
                                for (i in 0 until hueSliderHeight) {
                                    val localHue = i / hueSliderHeight.toFloat()
                                    val rgb = Color.HSBtoRGB(localHue, 1.0f, 1.0f)
                                    drawRect(hueSliderX, hueSliderY + i, hueSliderX + hueSliderWidth, hueSliderY + i + 1, rgb)
                                }

                                val hueMarkerY = hueSliderY + (hue * hueSliderHeight).toInt()
                                RenderUtils.drawBorderedRectRGB(
                                    hueSliderX - 1, hueMarkerY - 1,
                                    hueSliderX + hueSliderWidth + 1, hueMarkerY + 2,
                                    1.0f,
                                    0xFFFFFFFF.toInt(),
                                    0x00000000
                                )

                                val inColorPicker =
                                    (mouseX in startX until (startX + colorPickerWidth)) &&
                                            (mouseY in startY until (startY + colorPickerHeight))

                                val inHueSlider =
                                    (mouseX in hueSliderX until (hueSliderX + hueSliderWidth)) &&
                                            (mouseY in hueSliderY until (hueSliderY + hueSliderHeight))

                                if ((mouseButton == 0 || sliderValueHeld == colorValue) && (inColorPicker || inHueSlider)) {
                                    if (inColorPicker) {
                                        val newS = ((mouseX - startX) / colorPickerWidth.toFloat()).coerceIn(0f, 1f)
                                        val newB = (1.0f - ((mouseY - startY) / colorPickerHeight.toFloat())).coerceIn(0f, 1f)
                                        saturation = newS
                                        brightness = newB
                                    }
                                    if (inHueSlider) {
                                        val newH = ((mouseY - hueSliderY) / hueSliderHeight.toFloat()).coerceIn(0f, 1f)
                                        hue = newH
                                    }

                                    sliderValueHeld = colorValue

                                    if (mouseButton == 0) {
                                        return true
                                    }
                                }

                                val finalRGB = Color.HSBtoRGB(hue, saturation, brightness)
                                val finalColor = Color(finalRGB)
                                colorValue.set(finalColor)

                                val previewSubSize = 6
                                drawRect(
                                    startX + 75,
                                    startY - 10,
                                    startX + 75 + previewSubSize,
                                    startY - 10 + previewSubSize,
                                    finalColor.rgb
                                )

                                yPos += colorPickerHeight + previewSize + 6
                            } else {
                                yPos += previewSize + 15
                            }

                            if (mouseButton == -1) {
                                sliderValueHeld = null
                            }
                        }

                        else -> {
                            val text = value.name + "§f: " + value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 12
                        }
                    }
                }

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