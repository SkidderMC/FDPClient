/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Text Element - YZY GUI
 * @author opZywl
 */
class TextElement(
    private val element: ModuleElement,
    private val setting: TextValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private var editing = false
    private var currentText = setting.get()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val displayText = if (editing) currentText else setting.get()
        val backgroundColor = if (editing) Color(45, 45, 45) else Color(26, 26, 26)
        val borderColor = if (editing) parent.category.color else Color(60, 60, 60)

        // Draw background with border when editing
        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            backgroundColor
        )

        if (editing) {
            RenderUtils.yzyRectangle(
                x.toFloat(), y.toFloat(),
                width.toFloat(), 1f,
                borderColor
            )
            RenderUtils.yzyRectangle(
                x.toFloat(), (y + height - 1).toFloat(),
                width.toFloat(), 1f,
                borderColor
            )
        }

        val font = FDPClient.customFontManager["lato-bold-15"]

        font?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        val textWidth = font?.getWidth(displayText) ?: 0
        val finalText = if (editing) "$displayText|" else displayText
        font?.drawString(
            finalText,
            (x + width - textWidth - 10).toFloat(),
            y + (height / 4.0f) + 0.5f,
            if (editing) Color.WHITE.rgb else Color(0xD2D2D2).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            editing = !editing
            if (editing) {
                currentText = setting.get()
            } else {
                setting.set(currentText)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {
        if (editing) {
            when (code) {
                Keyboard.KEY_RETURN, Keyboard.KEY_ESCAPE -> {
                    if (code == Keyboard.KEY_RETURN) {
                        setting.set(currentText)
                    } else {
                        currentText = setting.get()
                    }
                    editing = false
                }
                Keyboard.KEY_BACK -> {
                    if (currentText.isNotEmpty()) {
                        currentText = currentText.dropLast(1)
                    }
                }
                else -> {
                    if (character.code >= 32 && character.code <= 126) {
                        currentText += character
                    }
                }
            }
        }
    }
}