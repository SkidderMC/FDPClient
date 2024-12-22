/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects

import com.google.common.base.Predicate
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PasswordField : Gui {

    /** Unique component ID. */
    val id: Int

    /** Height of this text field. */
    val height: Int

    /** Font renderer used for drawing. */
    val fontRenderer: FontRenderer?

    /**
     * Instead of `var width: Int`, rename to avoid `getWidth()` auto-generation.
     */
    var fieldWidth: Int

    /**
     * If you still want a manual function, name it `getActualWidth()` or `getFieldWidth()`.
     * (If not needed, just rely on `fieldWidth` property itself.)
     */
    fun computeFieldWidth(): Int {
        return if (enableBackgroundDrawing) fieldWidth - 8 else fieldWidth
    }

    var bottomBarColor: Int = -1
    var textColorValue: Int = -1
    var cursorColor: Int = -1
    var xPosition: Int
    var yPosition: Int
    var placeholder: String?
    var placeHolderTextX: Double

    /**
     * Instead of `var maxStringLength`, rename to avoid `setMaxStringLength(...)` collision.
     */
    var maxLength: Int = 32

    /**
     * If you also had a manual function `setMaxStringLength(...)`, rename it to something else, e.g.
     */
    fun applyMaxLength(value: Int) {
        maxLength = value
        if (textValue.length > value) {
            textValue = textValue.substring(0, value)
        }
    }

    /**
     * The text content (renamed from `text` to `textValue` to avoid collisions if you had setText()).
     */
    var textValue: String = ""

    private var cursorCounter = 0
    var enableBackgroundDrawing: Boolean = true
    var canLoseFocus = true
    private var isFocused = false
    private var isEnabled = true
    private var lineScrollOffset = 0

    var cursorPosition = 0
        private set

    var selectionEnd = 0
        private set

    private var enabledColor = 0xE0E0E0
    private var disabledColor = 0x707070
    var visible: Boolean = true

    private var field_175210_x: GuiResponder? = null
    private var field_175209_y: Predicate<String> = Predicate { true }

    constructor(
        placeholder: String?,
        componentId: Int,
        x: Int,
        y: Int,
        par5Width: Int,
        par6Height: Int,
        fr: FontRenderer?
    ) {
        this.placeholder = placeholder
        this.id = componentId
        this.xPosition = x
        this.yPosition = y
        this.fieldWidth = par5Width
        this.height = par6Height
        this.fontRenderer = fr
        this.placeHolderTextX = ((xPosition + fieldWidth) / 2f).toDouble()
    }

    constructor(
        placeholder: String?,
        componentId: Int,
        x: Int,
        y: Int,
        par5Width: Int,
        par6Height: Int,
        fr: FontRenderer?,
        textColor: Int
    ) {
        this.placeholder = placeholder
        this.id = componentId
        this.xPosition = x
        this.yPosition = y
        this.fieldWidth = par5Width
        this.height = par6Height
        this.fontRenderer = fr
        this.textColorValue = textColor
        this.placeHolderTextX = ((xPosition + fieldWidth) / 2f).toDouble()
    }

    fun func_175207_a(guiResponder: GuiResponder?) {
        field_175210_x = guiResponder
    }

    fun updateCursorCounter() {
        cursorCounter++
    }

    /**
     * Rename from setText(...) to avoid collision with `textValue` property’s setter.
     */
    fun updateText(newText: String) {
        if (field_175209_y.apply(newText)) {
            textValue = if (newText.length > maxLength) {
                newText.substring(0, maxLength)
            } else {
                newText
            }
            setCursorPositionEnd()
        }
    }

    fun updateTextColor(color: Int) {
        // If you have a property 'textColorValue' or 'textColor', just set it here:
        textColorValue = color
        // or this.textColor = color;
    }

    /** The selected portion of text. */
    val selectedText: String
        get() {
            val i = min(cursorPosition, selectionEnd)
            val j = max(cursorPosition, selectionEnd)
            return textValue.substring(i, j)
        }

    fun func_175205_a(predicate: Predicate<String>) {
        field_175209_y = predicate
    }

    fun writeText(input: String) {
        val s1 = ChatAllowedCharacters.filterAllowedCharacters(input)
        val i = min(cursorPosition, selectionEnd)
        val j = max(cursorPosition, selectionEnd)
        val maxCharsAllowed = maxLength - textValue.length - (i - j)

        var newText = ""
        if (textValue.isNotEmpty()) {
            newText += textValue.substring(0, i)
        }

        val appended = if (maxCharsAllowed < s1.length) {
            s1.substring(0, maxCharsAllowed)
        } else {
            s1
        }
        newText += appended

        if (textValue.isNotEmpty() && j < textValue.length) {
            newText += textValue.substring(j)
        }

        if (field_175209_y.apply(newText)) {
            textValue = newText
            moveCursorBy(i - selectionEnd + appended.length)
            field_175210_x?.func_175319_a(id, textValue)
        }
    }

    fun deleteWords(numWords: Int) {
        if (textValue.isEmpty()) return

        if (selectionEnd != cursorPosition) {
            writeText("")
        } else {
            deleteFromCursor(getNthWordFromCursor(numWords) - cursorPosition)
        }
    }

    fun drawPasswordBox() {
        drawTextBox(textValue, password = true)
    }

    fun deleteFromCursor(numChars: Int) {
        if (textValue.isEmpty()) return

        if (selectionEnd != cursorPosition) {
            writeText("")
        } else {
            val negative = numChars < 0
            val start = if (negative) cursorPosition + numChars else cursorPosition
            val end = if (negative) cursorPosition else cursorPosition + numChars

            var newText = ""
            if (start >= 0) {
                newText += textValue.substring(0, start)
            }
            if (end < textValue.length) {
                newText += textValue.substring(end)
            }

            if (field_175209_y.apply(newText)) {
                textValue = newText
                if (negative) moveCursorBy(numChars)
                field_175210_x?.func_175319_a(id, textValue)
            }
        }
    }

    fun getNthWordFromCursor(n: Int): Int {
        return getNthWordFromPos(n, cursorPosition)
    }

    fun getNthWordFromPos(n: Int, pos: Int): Int {
        return func_146197_a(n, pos, true)
    }

    fun func_146197_a(n: Int, pos: Int, skipSpaces: Boolean): Int {
        var index = pos
        val backwards = n < 0
        val steps = abs(n)

        repeat(steps) {
            if (!backwards) {
                val length = textValue.length
                index = textValue.indexOf(' ', index).takeIf { it != -1 } ?: length
                if (skipSpaces) {
                    while (index < length && textValue[index] == ' ') {
                        index++
                    }
                }
            } else {
                if (skipSpaces) {
                    while (index > 0 && textValue[index - 1] == ' ') {
                        index--
                    }
                }
                while (index > 0 && textValue[index - 1] != ' ') {
                    index--
                }
            }
        }
        return index
    }

    fun moveCursorBy(amount: Int) {
        setCursorPosition(selectionEnd + amount)
    }

    fun setCursorPositionZero() {
        setCursorPosition(0)
    }

    fun setCursorPositionEnd() {
        setCursorPosition(textValue.length)
    }

    fun textboxKeyTyped(charTyped: Char, keyCode: Int): Boolean {
        if (!isFocused) {
            return false
        }
        when {
            GuiScreen.isKeyComboCtrlA(keyCode) -> {
                setCursorPositionEnd()
                setSelectionPos(0)
                return true
            }
            GuiScreen.isKeyComboCtrlC(keyCode) -> {
                GuiScreen.setClipboardString(selectedText)
                return true
            }
            GuiScreen.isKeyComboCtrlV(keyCode) -> {
                if (isEnabled) {
                    writeText(GuiScreen.getClipboardString())
                }
                return true
            }
            GuiScreen.isKeyComboCtrlX(keyCode) -> {
                GuiScreen.setClipboardString(selectedText)
                if (isEnabled) {
                    writeText("")
                }
                return true
            }
            else -> {
                when (keyCode) {
                    14 -> {
                        // Backspace
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (isEnabled) deleteWords(-1)
                        } else if (isEnabled) {
                            deleteFromCursor(-1)
                        }
                        return true
                    }
                    199 -> {
                        // Home
                        if (GuiScreen.isShiftKeyDown()) {
                            setSelectionPos(0)
                        } else {
                            setCursorPositionZero()
                        }
                        return true
                    }
                    203 -> {
                        // Left arrow
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                setSelectionPos(getNthWordFromPos(-1, selectionEnd))
                            } else {
                                setSelectionPos(selectionEnd - 1)
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            setCursorPosition(getNthWordFromCursor(-1))
                        } else {
                            moveCursorBy(-1)
                        }
                        return true
                    }
                    205 -> {
                        // Right arrow
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                setSelectionPos(getNthWordFromPos(1, selectionEnd))
                            } else {
                                setSelectionPos(selectionEnd + 1)
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            setCursorPosition(getNthWordFromCursor(1))
                        } else {
                            moveCursorBy(1)
                        }
                        return true
                    }
                    207 -> {
                        // End
                        if (GuiScreen.isShiftKeyDown()) {
                            setSelectionPos(textValue.length)
                        } else {
                            setCursorPositionEnd()
                        }
                        return true
                    }
                    211 -> {
                        // Delete
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (isEnabled) deleteWords(1)
                        } else if (isEnabled) {
                            deleteFromCursor(1)
                        }
                        return true
                    }
                    else -> {
                        // Normal character input
                        if (ChatAllowedCharacters.isAllowedCharacter(charTyped)) {
                            if (isEnabled) {
                                writeText(charTyped.toString())
                            }
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val withinBounds =
            mouseX >= xPosition && mouseX < xPosition + fieldWidth &&
                    mouseY >= yPosition && mouseY < yPosition + height

        if (canLoseFocus) {
            setFocused(withinBounds)
        }
        if (isFocused && withinBounds && mouseButton == 0) {
            var i = mouseX - xPosition
            if (enableBackgroundDrawing) {
                i -= 4
            }
            val clippedText = fontRenderer?.trimStringToWidth(
                textValue.substring(lineScrollOffset),
                computeFieldWidth()
            ).orEmpty()
            setCursorPosition(
                (fontRenderer?.trimStringToWidth(clippedText, i) ?: "").length + lineScrollOffset
            )
        }
    }

    @JvmOverloads
    fun drawTextBox(content: String = textValue, password: Boolean = false) {
        var displayText = content
        if (password) {
            displayText = displayText.replace("\\.".toRegex(), "*")
        }

        if (!visible) return

        if (enableBackgroundDrawing) {
            GlStateManager.color(1f, 1f, 1f, 1f)
            drawRect(
                xPosition,
                yPosition + height,
                xPosition + fieldWidth,
                yPosition + height + 1,
                bottomBarColor
            )
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
        val textCol = textColorValue
        val j = cursorPosition - lineScrollOffset
        var k = selectionEnd - lineScrollOffset

        val trimmed = fontRenderer?.trimStringToWidth(
            displayText.substring(lineScrollOffset),
            computeFieldWidth()
        ).orEmpty()

        val cursorInside = (j in 0..trimmed.length)
        val showCursor = (isFocused && cursorCounter / 6 % 2 == 0 && cursorInside)
        val drawX = if (enableBackgroundDrawing) xPosition + 4 else xPosition
        val drawY = if (enableBackgroundDrawing) yPosition + (height - 8) / 4 else yPosition
        var textX = drawX

        // Placeholder if empty and not focused
        if (!isFocused && placeholder != null && displayText.isEmpty()) {
            fontRenderer?.drawCenteredString(placeholder, placeHolderTextX.toFloat(), drawY.toFloat(), textCol)
        }

        if (k > trimmed.length) {
            k = trimmed.length
        }

        if (trimmed.isNotEmpty() && cursorInside) {
            val sub = trimmed.substring(0, j)
            textX = (fontRenderer?.drawString(sub, drawX.toFloat(), drawY.toFloat(), textCol) ?: 0).toInt()
        }

        val hasMoreChars = (cursorPosition < displayText.length || displayText.length >= maxLength)
        var cursorX = textX
        if (!cursorInside) {
            cursorX = if (j > 0) drawX + fieldWidth else drawX
        } else if (hasMoreChars) {
            cursorX--
            textX--
        }

        // Draw the rest of the text after the cursor
        if (trimmed.isNotEmpty() && cursorInside && j < trimmed.length) {
            textX = (fontRenderer?.drawString(
                trimmed.substring(j),
                (textX + 6).toFloat(),
                drawY.toFloat(),
                textCol
            ) ?: 0).toInt()
        }

        if (showCursor) {
            GlStateManager.color(1f, 1f, 1f, 1f)
            if (hasMoreChars) {
                drawRect(
                    cursorX + 4,
                    drawY - 1,
                    cursorX + 5,
                    drawY + 1 + (fontRenderer?.height ?: 9),
                    cursorColor
                )
            } else {
                fontRenderer?.drawString("|", (cursorX + 4).toFloat(), drawY.toFloat(), textCol)
            }
        }

        if (k != j) {
            val endX = drawX + (fontRenderer?.stringWidth(trimmed.substring(0, k)) ?: 0)
            drawCursorVertical(cursorX, drawY - 1, endX - 1, drawY + 1 + (fontRenderer?.height ?: 9))
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun drawCursorVertical(startX: Int, startY: Int, endX: Int, endY: Int) {
        var x1 = startX
        var y1 = startY
        var x2 = endX
        var y2 = endY

        if (x1 < x2) {
            val temp = x1
            x1 = x2
            x2 = temp
        }
        if (y1 < y2) {
            val temp = y1
            y1 = y2
            y2 = temp
        }

        if (x2 > xPosition + fieldWidth) {
            x2 = xPosition + fieldWidth
        }
        if (x1 > xPosition + fieldWidth) {
            x1 = xPosition + fieldWidth
        }

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        GlStateManager.color(0f, 0f, 255f, 255f)
        GlStateManager.disableTexture2D()
        GlStateManager.enableColorLogic()
        GlStateManager.colorLogicOp(5387)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), 0.0).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), 0.0).endVertex()
        worldrenderer.pos(x2.toDouble(), y1.toDouble(), 0.0).endVertex()
        worldrenderer.pos(x1.toDouble(), y1.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.disableColorLogic()
        GlStateManager.enableTexture2D()
    }

    /**
     * Renamed from `setMaxStringLength(...)` to avoid the property’s auto-generated setter clash.
     */
    fun applyMaxStringLen(length: Int) {
        this.maxLength = length
        if (textValue.length > length) {
            textValue = textValue.substring(0, length)
        }
    }

    fun setCursorPosition(pos: Int) {
        cursorPosition = MathHelper.clamp_int(pos, 0, textValue.length)
        setSelectionPos(cursorPosition)
    }

    fun setSelectionPos(pos: Int) {
        var newPos = pos
        val len = textValue.length

        if (newPos > len) newPos = len
        if (newPos < 0) newPos = 0

        selectionEnd = newPos

        if (fontRenderer != null) {
            if (lineScrollOffset > len) {
                lineScrollOffset = len
            }
            val widthAvailable = computeFieldWidth()
            val trimmed = fontRenderer.trimStringToWidth(textValue.substring(lineScrollOffset), widthAvailable)
            val endIndex = trimmed.length + lineScrollOffset

            if (newPos == lineScrollOffset) {
                lineScrollOffset -= fontRenderer.trimStringToWidth(textValue, widthAvailable, true).length
            }
            if (newPos > endIndex) {
                lineScrollOffset += newPos - endIndex
            } else if (newPos <= lineScrollOffset) {
                lineScrollOffset -= (lineScrollOffset - newPos)
            }

            lineScrollOffset = MathHelper.clamp_int(lineScrollOffset, 0, len)
        }
    }

    fun isFocused(): Boolean = isFocused

    fun setFocused(focused: Boolean) {
        if (focused && !isFocused) {
            cursorCounter = 0
        }
        isFocused = focused
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}