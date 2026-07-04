/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui

import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.text.Normalizer
import java.util.Locale

/** Shared search behavior used by every native ClickGUI implementation. */
class ModuleSearch {

    var query = ""
        private set

    var focused = false
        private set

    val active: Boolean
        get() = query.isNotBlank()

    private var fieldLeft = 0
    private var fieldTop = 0
    private var fieldRight = 0
    private var fieldBottom = 0

    // Draggable position: -1 means "not placed yet", fall back to the default top-center anchor.
    private var posX = -1
    private var posY = -1
    private var dragging = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0

    fun matches(module: Module): Boolean {
        if (!active) return true

        val searchable = normalize(
            listOf(
                module.name,
                module.spacedName,
                module.aliases.joinToString(" "),
                module.category.displayName,
                module.subCategory.displayName,
                module.description
            ).joinToString(" ")
        )

        return normalize(query).split(' ').filter(String::isNotBlank).all(searchable::contains)
    }

    /** Returns true when the key belongs to the search field and must not propagate. */
    fun keyTyped(typedChar: Char, keyCode: Int, controlDown: Boolean): Boolean {
        if (controlDown && keyCode == Keyboard.KEY_F) {
            focused = true
            return true
        }

        if (!focused) return false

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                val hadQuery = query.isNotEmpty()
                query = ""
                focused = false
                // Empty search: don't swallow ESC, let the ClickGUI close on this press as before.
                if (!hadQuery) return false
            }
            Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> focused = false
            Keyboard.KEY_BACK -> if (query.isNotEmpty()) query = query.dropLast(1)
            Keyboard.KEY_DELETE -> query = ""
            else -> if (query.length < MAX_QUERY_LENGTH && ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                query += typedChar
            }
        }

        return true
    }

    /** Returns true only when the search field consumed the click. */
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        val hovered = mouseX in fieldLeft..fieldRight && mouseY in fieldTop..fieldBottom
        if (mouseButton == 0) {
            focused = hovered
            if (hovered) {
                // Clicking grabs the field; holding and moving drags it, a plain click just focuses.
                dragging = true
                dragOffsetX = mouseX - fieldLeft
                dragOffsetY = mouseY - fieldTop
            }
        }
        if (!hovered) return false

        if (mouseButton == 1) {
            query = ""
            focused = true
        }
        return mouseButton == 0 || mouseButton == 1
    }

    fun draw(font: FontRenderer, screenWidth: Int, accentColor: Int) {
        val mc = Minecraft.getMinecraft()
        val sr = ScaledResolution(mc)

        if (posX < 0) {
            posX = screenWidth / 2 - FIELD_WIDTH / 2
            posY = FIELD_MARGIN
        }

        if (dragging) {
            if (Mouse.isButtonDown(0)) {
                val mouseX = Mouse.getX() * sr.scaledWidth / mc.displayWidth
                val mouseY = sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight - 1
                posX = (mouseX - dragOffsetX).coerceIn(0, screenWidth - FIELD_WIDTH)
                posY = (mouseY - dragOffsetY).coerceIn(0, sr.scaledHeight - FIELD_HEIGHT)
            } else {
                dragging = false
            }
        }

        fieldLeft = posX
        fieldTop = posY
        fieldRight = fieldLeft + FIELD_WIDTH
        fieldBottom = fieldTop + FIELD_HEIGHT

        Gui.drawRect(fieldLeft - 1, fieldTop - 1, fieldRight + 1, fieldBottom + 1, if (focused) accentColor else 0x90000000.toInt())
        Gui.drawRect(fieldLeft, fieldTop, fieldRight, fieldBottom, 0xDC111318.toInt())

        val placeholder = "Search modules (Ctrl+F)"
        val content = if (query.isEmpty()) placeholder else query
        val color = if (query.isEmpty()) 0xFF777C86.toInt() else 0xFFF1F3F5.toInt()
        val availableWidth = FIELD_WIDTH - 12
        val visibleText = font.trimStringToWidth(content.reversed(), availableWidth).reversed()
        val textX = fieldLeft + 6
        val textY = fieldTop + (FIELD_HEIGHT - font.FONT_HEIGHT) / 2
        font.drawStringWithShadow(visibleText, textX.toFloat(), textY.toFloat(), color)

        if (focused && query.isNotEmpty() && System.currentTimeMillis() / 500L % 2L == 0L) {
            val cursorX = (textX + font.getStringWidth(visibleText) + 1).coerceAtMost(fieldRight - 3)
            Gui.drawRect(cursorX, textY, cursorX + 1, textY + font.FONT_HEIGHT, accentColor)
        }
    }

    fun unfocus() {
        focused = false
    }

    private fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase(Locale.ROOT)

    private companion object {
        const val FIELD_WIDTH = 180
        const val FIELD_HEIGHT = 16
        const val FIELD_MARGIN = 7
        const val MAX_QUERY_LENGTH = 64
        val DIACRITICS = "\\p{M}+".toRegex()
    }
}
