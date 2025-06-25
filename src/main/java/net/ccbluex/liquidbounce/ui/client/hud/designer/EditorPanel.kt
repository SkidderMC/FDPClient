/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.designer

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle.rgbaLabels
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.ELEMENTS
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Image
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold35
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.lerpWith
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.blendColors
import net.ccbluex.liquidbounce.utils.render.ColorUtils.minecraftRed
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorderedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRoundedRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedCornerRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTexture
import net.ccbluex.liquidbounce.utils.render.RenderUtils.makeScissorBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.updateTextureCache
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.ccbluex.liquidbounce.utils.ui.EditableText
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.MathHelper
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

class EditorPanel(private val hudDesigner: GuiHudDesigner, var x: Int, var y: Int) : MinecraftInstance {

    var width = 80
        private set
    var height = 20
        private set
    var realHeight = 20
        private set

    private var drag = false
    private var dragX = 0
    private var dragY = 0

    private var mouseDown = false
    private var rightMouseDown = false

    private var showConfirmation = false

    private var scroll = 0

    var create = false
    private var currentElement: Element? = null

    /**
     * Draw editor panel to screen
     *
     * @param mouseX
     * @param mouseY
     * @param wheel
     */
    fun drawPanel(mouseX: Int, mouseY: Int, wheel: Int) {
        // Drag panel
        drag(mouseX, mouseY)

        // Set current element
        if (currentElement != hudDesigner.selectedElement) scroll = 0
        currentElement = hudDesigner.selectedElement

        // Scrolling start
        var currMouseY = mouseY
        val shouldScroll = realHeight > 200

        if (shouldScroll) {
            glPushMatrix()
            makeScissorBox(x.toFloat(), y + 1F, x + width.toFloat(), y + 200F)
            glEnable(GL_SCISSOR_TEST)

            if (y + 200 < currMouseY) currMouseY = -1

            if (Mouse.hasWheel() && mouseX in x..x + width && currMouseY in y..y + 200) {
                if (wheel < 0 && -scroll + 205 <= realHeight) {
                    scroll -= 12
                } else if (wheel > 0) {
                    scroll += 12
                    if (scroll > 0) scroll = 0
                }
            }
        }

        // Draw panel
        drawRoundedCornerRect(x.toFloat()-2, y + 10F, x + width.toFloat()+2, y + realHeight.toFloat()+2,3f, Color(0, 0, 0, 150).rgb)
        when {
            create -> drawCreate(mouseX, currMouseY)
            currentElement != null -> drawEditor(mouseX, currMouseY)
            else -> drawSelection(mouseX, currMouseY)
        }

        // Scrolling end
        if (shouldScroll) {
            drawRect(
                x + width - 5, y + 15, x + width - 2, y + 197, Color(41, 41, 41).rgb
            )

            val v = 197 * (-scroll / (realHeight - 170F))
            drawRect(
                x + width - 5F, y + 15 + v, x + width - 2F, y + 20 + v, Color(37, 126, 255).rgb
            )

            glDisable(GL_SCISSOR_TEST)
            glPopMatrix()
        }

        // Save mouse states
        mouseDown = Mouse.isButtonDown(0)
        rightMouseDown = Mouse.isButtonDown(1)
    }

    /**
     * Draw create panel
     */
    private fun drawCreate(mouseX: Int, mouseY: Int) {
        height = 15 + scroll
        realHeight = 15
        width = 90

        for ((element, info) in ELEMENTS) {

            if (info == null) {
                println("Warning: Element with null info found.")
                continue
            }

            if (info.single && HUD.elements.any { it.javaClass == element }) continue

            val name = info.name

            fontSemibold35.drawString(name, x + 2f, y + height.toFloat(), Color.WHITE.rgb)

            val stringWidth = fontSemibold35.getStringWidth(name) + 8
            if (stringWidth > width) width = stringWidth

            if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
                try {
                    val newElement = element.newInstance()

                    if (newElement.createElement()) HUD.addElement(newElement)
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                    println("Error instantiating element: ${element.name}")
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    println("Error instantiating element: ${element.name}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Error instantiating element: ${element.name}")
                }
                create = false
            }

            height += 10
            realHeight += 10
        }

        drawGradientRoundedRect(x.toFloat()-4f, y-2F, x + width.toFloat()+4, y + 12F ,3, 1, Color(guiColor).rgb)
        val centerX = (x..x + width).lerpWith(0.5F)
        fontSemibold35.drawCenteredStringWithShadow("§lCreate element", centerX, y + 3.5F, Color.WHITE.rgb)
    }

    /**
     * Draw selection panel
     */
    private fun drawSelection(mouseX: Int, mouseY: Int) {
        height = 15 + scroll
        realHeight = 15
        width = 120

        fontSemibold35.drawString("§lCreate element", x + 2f, y.toFloat() + height, Color.WHITE.rgb)
        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY >= y + height && mouseY <= y + height + 10) create =
            true

        height += 10
        realHeight += 10

        fontSemibold35.drawString("§lReset", x + 2f, y.toFloat() + height, Color.WHITE.rgb)
        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
            showConfirmation = true // Show confirmation button
        }

        height += 15
        realHeight += 15

        fontSemibold35.drawString("§lAvailable Elements", x + 2f, y + height.toFloat(), Color.WHITE.rgb)
        height += 10
        realHeight += 10

        for (element in HUD.elements) {
            fontSemibold35.drawString(element.name, x + 2, y + height, Color.WHITE.rgb)

            val stringWidth = fontSemibold35.getStringWidth(element.name) + 8
            if (width < stringWidth) width = stringWidth

            if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
                hudDesigner.selectedElement = element
            }

            height += 10
            realHeight += 10
        }

        drawGradientRoundedRect(x.toFloat()-4f, y-2F, x + width.toFloat()+4, y + 12F ,3, 1, Color(guiColor).rgb)
        glColor4f(1f, 1f, 1f, 1f)
        val centerX = (x..x + width).lerpWith(0.5F)
        fontSemibold35.drawCenteredStringWithShadow("§lElement Editor", centerX, y + 3.5f, Color.WHITE.rgb)

        if (showConfirmation) {
            val confirmationMessage = "Are you sure you want to reset?"
            val textWidth = fontSemibold35.getStringWidth(confirmationMessage)

            val dialogX = x
            val dialogX2 = x + textWidth
            val dialogY = y + height
            val padding = 5
            val dialogHeight = 30
            val centerDialogX = (dialogX..dialogX2).lerpWith(0.5F)

            drawRect(
                dialogX - padding, dialogY + 10,
                dialogX2 + padding, dialogY + dialogHeight + 10,
                Color(0, 0, 0, 150).rgb
            )

            fontSemibold35.drawCenteredStringWithShadow(confirmationMessage, centerDialogX, dialogY + 12f, Color.WHITE.rgb)

            val buttonData = listOf(
                "Yes" to Color.GREEN to (dialogX.toFloat()..centerDialogX),
                "No" to Color.RED to (centerDialogX..dialogX2.toFloat())
            )

            val answerButtonY = (dialogY + 12 + fontSemibold35.height..dialogY + dialogHeight + 10).lerpWith(0.5F)
            val buttonWidth = fontSemibold35.getStringWidth("Yes")
            val paddingY = buttonWidth / 2

            buttonData.forEach { (labelAndColor, bounds) ->
                val (label, color) = labelAndColor
                val buttonX = bounds.lerpWith(0.5F)
                val isHovered = mouseX.toFloat() in (buttonX - buttonWidth..buttonX + buttonWidth) &&
                        mouseY.toFloat() in (answerButtonY - paddingY..answerButtonY + paddingY)

                drawRect(
                    buttonX - buttonWidth, answerButtonY - paddingY,
                    buttonX + buttonWidth, answerButtonY + paddingY,
                    color.let { if (isHovered) it.darker() else it }
                )

                fontSemibold35.drawCenteredString(label, buttonX, answerButtonY - 2, Color.WHITE.rgb, true)

                if (Mouse.isButtonDown(0) && !mouseDown && isHovered) {
                    if (label == "Yes") HUD.setDefault()
                    showConfirmation = false
                }
            }
        }
    }

    /**
     * Draw editor panel
     */
    private fun drawEditor(mouseX: Int, mouseY: Int) {
        height = scroll + 15
        realHeight = 15

        val prevWidth = width

        val element = currentElement ?: return

        // X
        fontSemibold35.drawString(
            "X: ${"%.2f".format(element.renderX)} (${"%.2f".format(element.x)})", x + 2, y + height, Color.WHITE.rgb
        )
        height += 10
        realHeight += 10

        // Y
        fontSemibold35.drawString(
            "Y: ${"%.2f".format(element.renderY)} (${"%.2f".format(element.y)})", x + 2, y + height, Color.WHITE.rgb
        )
        height += 10
        realHeight += 10

        // Scale
        fontSemibold35.drawString("Scale: ${"%.2f".format(element.scale)}", x + 2, y + height, Color.WHITE.rgb)
        height += 10
        realHeight += 10

        // Horizontal
        fontSemibold35.drawString("H:", x + 2, y + height, Color.WHITE.rgb)
        fontSemibold35.drawString(
            element.side.horizontal.sideName, x + 12, y + height, Color.GRAY.rgb
        )

        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
            val values = Side.Horizontal.entries.toTypedArray()
            val currIndex = values.indexOf(element.side.horizontal)

            val x = element.renderX

            element.side.horizontal = values[(currIndex + 1) % values.size]
            element.x = when (element.side.horizontal) {
                Side.Horizontal.LEFT -> x
                Side.Horizontal.MIDDLE -> (ScaledResolution(mc).scaledWidth / 2) - x
                Side.Horizontal.RIGHT -> ScaledResolution(mc).scaledWidth - x
            }
        }

        height += 10
        realHeight += 10

        // Vertical
        fontSemibold35.drawString("V:", x + 2, y + height, Color.WHITE.rgb)
        fontSemibold35.drawString(
            element.side.vertical.sideName, x + 12, y + height, Color.GRAY.rgb
        )

        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
            val values = Side.Vertical.entries.toTypedArray()
            val currIndex = values.indexOf(element.side.vertical)

            val y = element.renderY

            element.side.vertical = values[(currIndex + 1) % values.size]
            element.y = when (element.side.vertical) {
                Side.Vertical.UP -> y
                Side.Vertical.MIDDLE -> (ScaledResolution(mc).scaledHeight / 2) - y
                Side.Vertical.DOWN -> ScaledResolution(mc).scaledHeight - y
            }

        }

        height += 10
        realHeight += 10

        // Values
        for (value in element.values) {
            if (!value.isSupported()) continue

            val leftClickPressed = Mouse.isButtonDown(0) && !mouseDown
            val rightClickPressed = Mouse.isButtonDown(1) && !rightMouseDown

            when (value) {
                is BoolValue -> {
                    // Title
                    fontSemibold35.drawString(
                        value.name, x + 2, y + height, if (value.get()) Color.WHITE.rgb else Color.GRAY.rgb
                    )

                    val stringWidth = fontSemibold35.getStringWidth(value.name)
                    if (width < stringWidth + 8) width = stringWidth + 8

                    // Toggle value
                    if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
                        value.toggle()
                        element.updateElement()
                        ClickGui.style.clickSound()
                    }

                    // Change pos
                    height += 10
                    realHeight += 10
                }

                is FloatValue -> {
                    val current = value.get()
                    val min = value.minimum
                    val max = value.maximum

                    // Title
                    val text = "${value.name}: §c${"%.2f".format(current)}"

                    fontSemibold35.drawString(text, x + 2, y + height, Color.WHITE.rgb)

                    val stringWidth = fontSemibold35.getStringWidth(text)
                    if (width < stringWidth + 8) width = stringWidth + 8

                    // Slider
                    drawRect(x + 8F, y + height + 12F, x + prevWidth - 8F, y + height + 13F, Color.WHITE)

                    // Slider mark
                    val sliderValue = x + ((prevWidth - 18F) * (current - min) / (max - min))
                    drawRect(
                        8F + sliderValue, y + height + 9F, sliderValue + 11F, y + height + 15F, Color(37, 126, 255).rgb
                    )

                    // Slider changer
                    if (Mouse.isButtonDown(0) && mouseX in x + 8..x + prevWidth && mouseY in y + height + 9..y + height + 15) {
                        val curr = MathHelper.clamp_float((mouseX - x - 8F) / (prevWidth - 18F), 0F, 1F)

                        value.set(min + (max - min) * curr)
                        element.updateElement()
                    }

                    // Change pos
                    height += 20
                    realHeight += 20
                }

                is IntValue -> {
                    val current = value.get()
                    val min = value.minimum
                    val max = value.maximum

                    // Title
                    val text = "${value.name}: §c$current"

                    fontSemibold35.drawString(text, x + 2, y + height, Color.WHITE.rgb)

                    val stringWidth = fontSemibold35.getStringWidth(text)
                    if (width < stringWidth + 8) width = stringWidth + 8

                    // Slider
                    drawRect(x + 8F, y + height + 12F, x + prevWidth - 8F, y + height + 13F, Color.WHITE)

                    // Slider mark
                    val sliderValue = x + ((prevWidth - 18F) * (current - min) / (max - min))
                    drawRect(
                        8F + sliderValue, y + height + 9F, sliderValue + 11F, y + height + 15F, Color(37, 126, 255).rgb
                    )

                    // Slider changer
                    if (Mouse.isButtonDown(0) && mouseX in x + 8..x + prevWidth && mouseY in y + height + 9..y + height + 15) {
                        val curr = MathHelper.clamp_float((mouseX - x - 8F) / (prevWidth - 18F), 0F, 1F)

                        value.set((min + (max - min) * curr).toInt())
                        element.updateElement()
                    }

                    // Change pos
                    height += 20
                    realHeight += 20
                }

                is ListValue -> {
                    // Title
                    fontSemibold35.drawString(value.name, x + 2, y + height, Color.WHITE.rgb)

                    height += 10
                    realHeight += 10

                    // Selectable values
                    for (s in value.values) {
                        // Value title
                        val text = "§c> §r$s"
                        fontSemibold35.drawString(
                            text, x + 2, y + height, if (s == value.get()) Color.WHITE.rgb else Color.GRAY.rgb
                        )

                        val stringWidth = fontSemibold35.getStringWidth(text)
                        if (width < stringWidth + 8) width = stringWidth + 8

                        // Select value
                        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
                            value.set(s)
                            element.updateElement()
                            ClickGui.style.clickSound()
                        }

                        // Change pos
                        height += 10
                        realHeight += 10
                    }
                }

                is FontValue -> {
                    // Title
                    val displayString = value.displayName

                    fontSemibold35.drawString(displayString, x + 2, y + height, Color.WHITE.rgb)

                    val stringWidth = fontSemibold35.getStringWidth(displayString)
                    if (width < stringWidth + 8) width = stringWidth + 8

                    if (((Mouse.isButtonDown(0) && !mouseDown) || (Mouse.isButtonDown(1) && !rightMouseDown)) && mouseX in x..x + width && mouseY in y + height..y + height + 10) {
                        if (Mouse.isButtonDown(0)) value.next()
                        else value.previous()
                        element.updateElement()
                        ClickGui.style.clickSound()
                    }

                    height += 10
                    realHeight += 10
                }

                is ColorValue -> {
                    val currentColor = value.selectedColor()

                    val startText = "${value.name}: "
                    val valueText = "#%08X".format(currentColor.rgb)
                    val combinedText = startText + valueText

                    val optimalWidth = (fontSemibold35.getStringWidth(combinedText) * 1.5F).roundToInt()

                    if (optimalWidth > width) {
                        width = optimalWidth
                    }

                    val spacing = 14

                    val maxX = x + width
                    val startX = x
                    val startY = y + height - 1

                    val rgbaOptionHeight = if (value.showOptions) fontSemibold35.height * 4 else 0

                    // Color preview
                    val colorPreviewSize = 9
                    val colorPreviewX2 = maxX - colorPreviewSize
                    val colorPreviewX1 = colorPreviewX2 - colorPreviewSize
                    val colorPreviewY1 = startY + 1
                    val colorPreviewY2 = colorPreviewY1 + colorPreviewSize

                    val rainbowPreviewX2 = colorPreviewX1 - (colorPreviewSize / 1.5F).roundToInt()
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

                    if (leftClickPressed || rightClickPressed) {
                        val isColorPreview =
                            mouseX in colorPreviewX1..colorPreviewX2 && mouseY in colorPreviewY1..colorPreviewY2
                        val isRainbowPreview =
                            mouseX in rainbowPreviewX1..rainbowPreviewX2 && mouseY in colorPreviewY1..colorPreviewY2

                        when {
                            isColorPreview -> {
                                if (leftClickPressed && rainbow) value.rainbow = false
                                if (rightClickPressed) value.showPicker = !value.showPicker
                                ClickGui.style.clickSound()
                                element.updateElement()
                            }

                            isRainbowPreview -> {
                                if (leftClickPressed) value.rainbow = true
                                if (rightClickPressed) value.showPicker = !value.showPicker
                                ClickGui.style.clickSound()
                                element.updateElement()
                            }
                        }
                    }

                    val valueX = startX + fontSemibold35.getStringWidth(startText)
                    val valueWidth = fontSemibold35.getStringWidth(valueText)

                    if (rightClickPressed && mouseX in valueX..valueX + valueWidth && mouseY.toFloat() in textY - 2..textY + fontSemibold35.height - 3F) {
                        value.showOptions = !value.showOptions

                        if (!value.showOptions) {
                            resetChosenText(value)
                        }
                    }

                    val widestLabel = rgbaLabels.maxOf { fontSemibold35.getStringWidth(it) }

                    var highlightCursor = {}

                    hudDesigner.elementEditableText?.chosenText?.let {
                        if (it.value != value) {
                            return@let
                        }

                        val startValueX = textX + widestLabel + 3
                        val cursorY = textY + value.rgbaIndex * fontSemibold35.height + 10

                        if (it.selectionActive()) {
                            val start = startValueX + fontSemibold35.getStringWidth(it.string.take(it.selectionStart!!))
                            val end = startValueX + fontSemibold35.getStringWidth(it.string.take(it.selectionEnd!!))
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

                        val chosenText = hudDesigner.elementEditableText?.chosenText

                        rgbaLabels.forEachIndexed { index, label ->
                            val rgbaValueText = "${rgbaValues[index]}"
                            val colorX = textX + widestLabel + 4
                            val yPosition = rgbaYStart + index * fontSemibold35.height

                            val isEmpty = chosenText?.value == value && value.rgbaIndex == index && chosenText.string.isEmpty()

                            val extraSpacing = if (isEmpty) maxWidth + 4 else 0
                            val finalX = colorX + extraSpacing

                            val defaultColor = if (isEmpty) Color.LIGHT_GRAY else minecraftRed
                            val defaultText = if (isEmpty) "($rgbaValueText)" else rgbaValueText

                            fontSemibold35.drawString(label, textX, yPosition, Color.WHITE.rgb)
                            fontSemibold35.drawString(defaultText, finalX, yPosition, defaultColor.rgb)

                            if (leftClickPressed) {
                                if (mouseX.toFloat() in finalX..finalX + maxWidth && mouseY.toFloat() in yPosition - 2..yPosition + 6) {
                                    hudDesigner.elementEditableText =
                                        ElementEditableText(element, EditableText.forRGBA(value, index))
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
                                    id, colorPickerStartX, colorPickerStartY, colorPickerWidth, colorPickerHeight
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

                                        val alpha = ((1 - y.toFloat() / hueSliderHeight.toFloat()) * 255).roundToInt()

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

                        if (leftClickPressed && (inColorPicker || inHueSlider || inOpacitySlider) || value.lastChosenSlider != null) {
                            if (inColorPicker && sliderType == null || sliderType == ColorValue.SliderType.COLOR) {
                                val newS = ((mouseX - colorPickerStartX) / colorPickerWidth.toFloat()).coerceIn(
                                    0f, 1f
                                )
                                val newB = (1.0f - (mouseY - colorPickerStartY) / colorPickerHeight.toFloat()).coerceIn(
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
                                value.hueSliderY = ((mouseY - hueSliderStartY) / hueSliderHeight.toFloat()).coerceIn(
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

                            value.changeValue(finalColor)

                            if (!WaitTickUtils.hasScheduled(this)) {
                                WaitTickUtils.conditionalSchedule(this, 10) {
                                    (value.lastChosenSlider == null).also { if (it) saveConfig(valuesConfig) }
                                }
                            }

                            if (leftClickPressed) {
                                value.lastChosenSlider = when {
                                    inColorPicker && !rainbow -> ColorValue.SliderType.COLOR
                                    inHueSlider && !rainbow -> ColorValue.SliderType.HUE
                                    inOpacitySlider -> ColorValue.SliderType.OPACITY
                                    else -> null
                                }
                            }
                        }

                        val inc = colorPickerHeight + colorPreviewSize - 6

                        height += inc
                        realHeight += inc
                    }
                    drawBorderedRect(
                        colorPreviewX1,
                        colorPreviewY1,
                        colorPreviewX2,
                        colorPreviewY2,
                        1.5f,
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
                    height += spacing + rgbaOptionHeight
                    realHeight += spacing + rgbaOptionHeight
                }

                // TODO: branch completion
                else -> {}
            }
        }

        // Header
        drawGradientRoundedRect(x.toFloat()-4f, y-2F, x + width.toFloat()+4, y + 12F ,3, 1, Color(guiColor).rgb)
        fontSemibold35.drawString("§l${element.name}", x + 2F, y + 3.5F, Color.WHITE.rgb)

        // Save button (Image only)
        if (element is Image) {
            val stringWidth = x + width - fontSemibold35.getStringWidth("§lSave") - 2 - 2 - fontSemibold35.getStringWidth("§lDelete") - 2
            fontSemibold35.drawString("§lSave", stringWidth.toFloat(), y + 3.5F, Color.WHITE.rgb)
            if (Mouse.isButtonDown(0) && !mouseDown && mouseX in stringWidth..x + width && mouseY in y..y + 10) {
                element.saveImage()
            }
        }

        // Delete button
        if (!element.info.force) {
            val deleteWidth = x + width - fontSemibold35.getStringWidth("§lDelete") - 2
            fontSemibold35.drawString("§lDelete", deleteWidth.toFloat(), y + 3.5F, Color.WHITE.rgb)
            if (Mouse.isButtonDown(0) && !mouseDown && mouseX in deleteWidth..x + width && mouseY in y..y + 10) {
                HUD.removeElement(hudDesigner, element)
            }
        }
    }

    /**
     * Drag panel
     */
    private fun drag(mouseX: Int, mouseY: Int) {
        if (Mouse.isButtonDown(0) && !mouseDown && mouseX in x..x + width && mouseY in y..y + 12) {
            drag = true
            dragX = mouseX - x
            dragY = mouseY - y
        }

        if (Mouse.isButtonDown(0) && drag) {
            x = mouseX - dragX
            y = mouseY - dragY
        } else drag = false
    }

    fun resetChosenText(value: Value<*>) {
        if (hudDesigner.elementEditableText?.chosenText?.value == value) {
            hudDesigner.elementEditableText = null
        }
    }

    fun moveRGBAIndexBy(delta: Int) {
        val elementEditableText = this.hudDesigner.elementEditableText ?: return

        val editableText = elementEditableText.chosenText

        if (editableText.value !is ColorValue) {
            return
        }

        this.hudDesigner.elementEditableText = ElementEditableText(
            elementEditableText.element,
            EditableText.forRGBA(editableText.value, (editableText.value.rgbaIndex + delta).mod(4))
        )
    }

    data class ElementEditableText(val element: Element, val chosenText: EditableText)

}