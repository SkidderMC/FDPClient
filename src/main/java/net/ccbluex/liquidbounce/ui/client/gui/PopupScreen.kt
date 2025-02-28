/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.ui.client.gui.PopupScreen.Builder
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color

data class ButtonData(val text: String, val action: Runnable)

inline fun PopupScreen(builderAction: Builder.() -> Unit) = Builder().apply(builderAction).build()

class PopupScreen(
    private val title: String,
    private val message: String,
    private val buttons: List<ButtonData>,
    private val onClose: Runnable
) {
    class Builder {
        private var title: String? = null
        private var message: String? = null
        private val buttons = mutableListOf<ButtonData>()
        private var onClose: Runnable = Runnable {}

        fun title(title: String): Builder = apply {
            this.title = title
        }

        fun message(message: String): Builder = apply {
            this.message = message
        }

        fun button(text: String, action: Runnable = Runnable {}): Builder = apply {
            this.buttons += ButtonData(text, action)
        }

        fun onClose(onClose: Runnable): Builder = apply {
            this.onClose = onClose
        }

        fun build(): PopupScreen {
            requireNotNull(title) { "title should be not null" }
            requireNotNull(message) { "message should be not null" }

            return PopupScreen(title!!, message!!, buttons, onClose)
        }
    }

    private val backgroundColor = Color(32, 32, 32, 220).rgb
    private val borderColor = Color(64, 64, 64, 255).rgb
    private val buttonColor = Color(48, 48, 48, 255).rgb
    private val buttonHoverColor = Color(64, 64, 64, 255).rgb
    private val dismissButtonColor = Color(200, 0, 0, 180).rgb
    private val dismissButtonHoverColor = Color(255, 0, 0, 220).rgb
    private val textColor = Color(255, 255, 255, 255).rgb

    private var buttonWidth = 100
    private var buttonHeight = 20
    private var popupWidth = 300
    private var popupHeight = 200
    private var x: Int = 0
    private var y: Int = 0
    private val messageYOffset = 50
    private val fontHeight = Fonts.fontSemibold35.fontHeight
    private var scrollOffset = 0

    private val buttonRects = mutableListOf<Rect>()
    private var dismissButtonRect: Rect? = null
    private val buttonAnimations = mutableMapOf<Int, Float>()
    private var dismissButtonAnimation = 0f

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.lineSequence()

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                lines.add("")
                continue
            }

            val words = paragraph.split(" ")
            var currentLine = ""

            for (word in words) {
                val tempLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = Fonts.fontSemibold35.getStringWidth(tempLine)

                if (width > maxWidth && currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = tempLine
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
        }

        return lines
    }

    fun drawScreen(screenWidth: Int, screenHeight: Int, mouseX: Int, mouseY: Int) {
        x = (screenWidth - popupWidth) / 2
        y = (screenHeight - popupHeight) / 2

        Gui.drawRect(0, 0, screenWidth, screenHeight, Color(0, 0, 0, 200).rgb)

        RenderUtils.drawRoundedBorderRect(
            x.toFloat(), y.toFloat(), (x + popupWidth).toFloat(), (y + popupHeight).toFloat(),
            5f, backgroundColor, borderColor, 3f
        )

        Fonts.fontExtraBold40.drawCenteredString(
            title,
            (screenWidth / 2).toFloat(),
            (y + 15).toFloat(),
            textColor,
            true
        )

        drawDismissButton(mouseX, mouseY)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        setupScissorBox(x + 10, y + messageYOffset, popupWidth - 20, popupHeight - messageYOffset - buttonHeight - 30)

        val messageLines = wrapText(message, popupWidth - 20)
        var messageY = y + messageYOffset + scrollOffset

        for (line in messageLines) {
            Fonts.fontSemibold35.drawString(
                line,
                (x + 10).toFloat(),
                messageY.toFloat(),
                textColor,
                true
            )
            messageY += fontHeight
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)

        drawScrollBar(messageLines.size * fontHeight)

        drawButtons(screenWidth, screenHeight, mouseX, mouseY)
    }

    private fun drawDismissButton(mouseX: Int, mouseY: Int) {
        val size = 16
        val dismissX = x + popupWidth - size - 10
        val dismissY = y + 10

        dismissButtonRect = Rect(dismissX, dismissY, size, size)

        val isHovered =
            mouseX >= dismissX && mouseX <= dismissX + size && mouseY >= dismissY && mouseY <= dismissY + size

        dismissButtonAnimation = if (isHovered) (dismissButtonAnimation + 0.1f).coerceAtMost(1f)
        else (dismissButtonAnimation - 0.1f).coerceAtLeast(0f)

        val currentColor = interpolateColor(dismissButtonColor, dismissButtonHoverColor, dismissButtonAnimation)

        RenderUtils.drawRoundedRect(
            dismissX.toFloat(),
            dismissY.toFloat(),
            (dismissX + size).toFloat(),
            (dismissY + size).toFloat(),
            currentColor,
            3f
        )

        Fonts.fontRegular35.drawCenteredString(
            "X",
            (dismissX + size / 2).toFloat() + 0.5f,
            (dismissY + size / 2 - Fonts.fontRegular35.fontHeight / 2).toFloat() + 2f,
            textColor
        )
    }

    private fun drawButtons(screenWidth: Int, screenHeight: Int, mouseX: Int, mouseY: Int) {
        val buttonY = y + popupHeight - buttonHeight - 15
        val totalButtonWidth = buttons.size * buttonWidth + (buttons.size - 1) * 10
        var currentButtonX = screenWidth / 2 - totalButtonWidth / 2

        buttonRects.clear()

        for (i in buttons.indices) {
            val button = buttons[i]
            val buttonX = currentButtonX

            val rect = Rect(buttonX, buttonY, buttonWidth, buttonHeight)
            buttonRects.add(rect)

            val isHovered = mouseX >= rect.x && mouseX <= rect.x + rect.width &&
                    mouseY >= rect.y && mouseY <= rect.y + rect.height

            val progress = buttonAnimations.getOrPut(i) { 0f }
            buttonAnimations[i] = if (isHovered) (progress + 0.1f).coerceAtMost(1f)
            else (progress - 0.1f).coerceAtLeast(0f)

            val currentColor = interpolateColor(buttonColor, buttonHoverColor, buttonAnimations[i]!!)

            RenderUtils.drawRoundedRect(
                rect.x.toFloat(),
                rect.y.toFloat(),
                (rect.x + rect.width).toFloat(),
                (rect.y + rect.height).toFloat(),
                currentColor,
                4f
            )

            Fonts.fontRegular35.drawCenteredString(
                button.text,
                (rect.x + rect.width / 2).toFloat(),
                (rect.y + rect.height / 2 - Fonts.fontRegular35.fontHeight / 2).toFloat() + 2f,
                textColor
            )

            currentButtonX += buttonWidth + 10
        }
    }

    private fun interpolateColor(color1: Int, color2: Int, factor: Float): Int {
        val r1 = (color1 shr 16) and 0xFF
        val g1 = (color1 shr 8) and 0xFF
        val b1 = color1 and 0xFF
        val a1 = (color1 shr 24) and 0xFF

        val r2 = (color2 shr 16) and 0xFF
        val g2 = (color2 shr 8) and 0xFF
        val b2 = color2 and 0xFF
        val a2 = (color2 shr 24) and 0xFF

        val r = (r1 + factor * (r2 - r1)).toInt()
        val g = (g1 + factor * (g2 - g1)).toInt()
        val b = (b1 + factor * (b2 - b1)).toInt()
        val a = (a1 + factor * (a2 - a1)).toInt()

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun drawScrollBar(totalMessageHeight: Int) {
        val availableSpace = popupHeight - messageYOffset - buttonHeight - 30

        if (totalMessageHeight > availableSpace) {
            val scrollbarTrackLength = availableSpace.toFloat()
            val scrollbarLengthRatio = availableSpace.toFloat() / totalMessageHeight.toFloat()
            val scrollbarLength = (scrollbarTrackLength * scrollbarLengthRatio).coerceAtMost(availableSpace.toFloat())

            val maxScrollOffset = (totalMessageHeight - availableSpace).coerceAtLeast(0)
            val scrollbarPositionRatio =
                if (maxScrollOffset > 0) (-scrollOffset).toFloat() / maxScrollOffset.toFloat() else 0f
            val scrollbarStartY = y + messageYOffset + (availableSpace - scrollbarLength) * scrollbarPositionRatio

            val scrollbarWidth = 3f
            Gui.drawRect(
                x + popupWidth - 7,
                scrollbarStartY.toInt(),
                (x + popupWidth - 7 + scrollbarWidth).toInt(),
                (scrollbarStartY + scrollbarLength).toInt(),
                0xFFFFFFFF.toInt()
            )
        }
    }

    private fun setupScissorBox(x: Int, y: Int, width: Int, height: Int) {
        val scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor

        GL11.glScissor(
            x * scaleFactor,
            Minecraft.getMinecraft().displayHeight - ((y + height) * scaleFactor),
            width * scaleFactor,
            height * scaleFactor
        )
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        dismissButtonRect?.let { rect ->
            if (mouseX >= rect.x && mouseX <= rect.x + rect.width && mouseY >= rect.y && mouseY <= rect.y + rect.height) {
                onClose.run()
                return
            }
        }

        for (i in buttons.indices) {
            val rect = buttonRects[i]
            if (mouseX >= rect.x && mouseX <= rect.x + rect.width && mouseY >= rect.y && mouseY <= rect.y + rect.height) {
                buttons[i].action.run()
                onClose.run()
                return
            }
        }
    }

    fun handleMouseWheel(amount: Int) {
        val totalMessageHeight = wrapText(message, popupWidth - 20).size * fontHeight
        val availableSpace = popupHeight - messageYOffset - buttonHeight - 30

        if (totalMessageHeight > availableSpace) {
            scrollOffset += amount / 8
            scrollOffset = scrollOffset.coerceIn(-(totalMessageHeight - availableSpace), 0)
        }
    }

    private data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)
}