/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.Vec3Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Vec3 Element - YZY GUI
 */
class Vec3Element(
    private val element: ModuleElement,
    private val setting: Vec3Value,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    private val labels = arrayOf("X", "Y", "Z")

    private var editingComponent = -1
    private var buffer = ""

    private fun fieldWidth(): Int = (width - 4) / 3

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return

        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            Color(26, 26, 26)
        )

        val fw = fieldWidth()
        for (i in 0 until 3) {
            val fx = x + i * (fw + 2)
            val editing = editingComponent == i
            RenderUtils.yzyRectangle(
                fx.toFloat(), y.toFloat(),
                fw.toFloat(), height.toFloat(),
                if (editing) Color(45, 45, 45) else Color(34, 34, 34)
            )

            val componentText = if (editing) buffer
                else String.format("%.2f", setting.value[i])

            font.drawString(
                "${labels[i]}:$componentText" + if (editing) "|" else "",
                (fx + 2).toFloat(),
                y + (height / 4.0f) + 0.5f,
                if (editing) Color.WHITE.rgb else Color(0xD2D2D2).rgb
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (button != 0) return
        commit()
        editingComponent = -1
        val fw = fieldWidth()
        for (i in 0 until 3) {
            val fx = x + i * (fw + 2)
            if (mouseX in fx..(fx + fw) && mouseY in y..(y + height)) {
                editingComponent = i
                buffer = String.format("%.2f", setting.value[i])
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {
        if (editingComponent < 0) return
        when (code) {
            Keyboard.KEY_RETURN, Keyboard.KEY_ESCAPE -> {
                if (code == Keyboard.KEY_RETURN) commit()
                editingComponent = -1
            }
            Keyboard.KEY_BACK -> if (buffer.isNotEmpty()) buffer = buffer.dropLast(1)
            else -> {
                if (character.isDigit() || character == '.' || character == '-') {
                    buffer += character
                }
            }
        }
    }

    private fun commit() {
        if (editingComponent < 0) return
        val parsed = buffer.toDoubleOrNull() ?: return
        when (editingComponent) {
            0 -> setting.x = parsed
            1 -> setting.y = parsed
            2 -> setting.z = parsed
        }
    }
}
