/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.ui

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiScreen.getClipboardString
import net.minecraft.client.gui.GuiScreen.setClipboardString
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
import java.awt.Color

abstract class AbstractScreen : GuiScreen() {

    val screenScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + CoroutineName(this::class.java.simpleName))

    protected val textFields = arrayListOf<GuiTextField>()

    protected operator fun <T : GuiTextField> T.unaryPlus(): T {
        textFields.add(this)
        return this
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        textFields.forEach {
            it.mouseClicked(mouseX, mouseY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onGuiClosed() {
        screenScope.cancel("Screen closed")
        super.onGuiClosed()
    }

    protected operator fun <T : GuiButton> T.unaryPlus(): T {
        buttonList.add(this)
        return this
    }

    protected inline fun textField(
        id: Int,
        fontRenderer: FontRenderer,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        block: GuiTextField.() -> Unit = {}
    ) = +GuiTextField(id, fontRenderer, x, y, width, height).apply(block)

}

fun isCtrlPressed(): Boolean {
    return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
}

fun isValidInput(typedChar: Char, text: EditableText): Boolean {
    val nextString = text.string + typedChar
    return when (text.value) {
        is ColorValue -> {
            text.validator(if (text.selectionActive()) typedChar.toString() else nextString)
        }

        else -> ColorUtils.isAllowedCharacter(typedChar) || typedChar == '§'
    }
}

data class EditableText(
    val value: Value<*>,
    var string: String,
    var cursorIndex: Int = string.length,
    val validator: (String) -> Boolean = { true },
    val onUpdate: (String) -> Unit
) {
    var selectionStart: Int? = null
    var selectionEnd: Int? = null

    val cursorString get() = string.take(cursorIndex)

    fun updateText(newString: String) {
        if (validator(newString)) {
            onUpdate(newString)
        }
    }

    fun moveCursorBy(delta: Int) {
        cursorIndex = (cursorIndex + delta).coerceIn(0, string.length)
        clearSelection()
    }

    fun insertAtCursor(newText: String) {
        deleteSelectionIfActive()
        val newString = string.take(cursorIndex) + newText + string.drop(cursorIndex)
        if (validator(newString)) {
            string = newString
            cursorIndex += newText.length
        }
    }

    fun deleteAtCursor(length: Int) {
        if (selectionActive()) {
            deleteSelectionIfActive()
        } else if (cursorIndex > 0) {
            string = string.take(cursorIndex - length) + string.drop(cursorIndex)
            cursorIndex -= length
        }
    }

    fun selectAll() {
        selectionStart = 0
        selectionEnd = string.length
        cursorIndex = string.length
    }

    fun selectionActive() = selectionStart != null && selectionEnd != null

    private fun deleteSelectionIfActive() {
        if (selectionActive()) {
            val start = minOf(selectionStart!!, selectionEnd!!)
            val end = maxOf(selectionStart!!, selectionEnd!!)
            string = string.take(start) + string.drop(end)
            cursorIndex = start
            clearSelection()
        }
    }

    private fun clearSelection() {
        selectionStart = null
        selectionEnd = null
    }

    inline fun processInput(typedChar: Char, keyCode: Int, onIndexUpdate: (Int) -> Unit) {
        when {
            keyCode == Keyboard.KEY_BACK -> {
                deleteAtCursor(1)
            }

            keyCode in intArrayOf(Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT) -> {
                moveCursorBy(if (keyCode == Keyboard.KEY_LEFT) -1 else 1)
            }

            keyCode in intArrayOf(Keyboard.KEY_DOWN, Keyboard.KEY_UP) -> {
                onIndexUpdate(if (keyCode == Keyboard.KEY_DOWN) 1 else -1)
            }

            keyCode == Keyboard.KEY_C && isCtrlPressed() && selectionActive() -> {
                val start = minOf(selectionStart!!, selectionEnd!!)
                val end = maxOf(selectionStart!!, selectionEnd!!)
                setClipboardString(string.substring(start, end))
            }

            keyCode == Keyboard.KEY_V && isCtrlPressed() -> {
                getClipboardString()?.let { pastedText ->
                    insertAtCursor(pastedText)
                }
            }

            keyCode == Keyboard.KEY_A && isCtrlPressed() -> {
                selectAll()
            }

            isValidInput(typedChar, this) -> {
                insertAtCursor(typedChar.toString())
            }

            else -> {}
        }

        updateText(string)
    }

    companion object {
        fun forTextValue(value: TextValue) = EditableText(
            value = value,
            string = value.get(),
            onUpdate = { value.set(it) }
        )

        fun forRGBA(value: ColorValue, index: Int): EditableText {
            val color = value.get()

            val component = when (index) {
                0 -> color.red
                1 -> color.green
                2 -> color.blue
                3 -> color.alpha
                else -> throw IllegalArgumentException("Invalid RGBA index")
            }

            value.rgbaIndex = index

            return EditableText(
                value = value,
                string = component.toString(),
                validator = {
                    ColorUtils.isValidColorInput(it)
                },
                onUpdate = { newText ->
                    val newValue = newText.toIntOrNull()?.coerceIn(0, 255) ?: component
                    val currentColor = value.get()
                    val newColor = when (index) {
                        0 -> Color(newValue, currentColor.green, currentColor.blue, currentColor.alpha)
                        1 -> Color(currentColor.red, newValue, currentColor.blue, currentColor.alpha)
                        2 -> Color(currentColor.red, currentColor.green, newValue, currentColor.alpha)
                        3 -> Color(currentColor.red, currentColor.green, currentColor.blue, newValue)
                        else -> currentColor
                    }
                    value.set(newColor)
                }
            )
        }
    }
}