/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.ListValue
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Module Element - YZY GUI
 * @author opZywl
 */
class ModuleElement(
    val module: Module,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    companion object {
        const val MODULE_HEIGHT = 14
    }

    private val elements = mutableListOf<PanelElement>()
    var isExtended = false
    private var isBinding = false

    init {
        module.values.forEach { value ->
            val element = when (value) {
                is BoolValue -> BooleanElement(this, value, parent, x + 4, y, width - 8, 12)
                is FloatValue -> FloatElement(this, value, parent, x + 4, y, width - 4, 12)
                is IntValue -> IntegerElement(this, value, parent, x + 4, y, width - 4, 12)
                is ListValue -> ListElement(this, value, parent, x + 4, y, width - 8, 12)
                else -> null
            }
            element?.let { elements.add(it) }
        }
        update()
    }

    private fun update() {
        var elementY = y + height
        elements.forEach { element ->
            element.x = x + 4
            element.y = elementY
            elementY += element.height
        }
    }

    fun getExtendedHeight(): Float {
        return if (isExtended) {
            elements.sumOf { it.height.toDouble() }.toFloat() + 2
        } else {
            0.0f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        update()
        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return
        var moduleHeight = height

        if (isExtended) {
            moduleHeight += elements.sumOf { it.height } + 2
        }

        val moduleColor = if (module.state) {
            yzyCategory.of(module.category)?.color ?: Color(37, 37, 37)
        } else {
            Color(37, 37, 37)
        }

        RenderUtils.yzyRectangle(
            x + 0.5f, y.toFloat(),
            (width - 1).toFloat(), moduleHeight.toFloat(),
            if (isExtended) Color(26, 26, 26) else moduleColor
        )

        var text = module.name.lowercase()

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && module.keyBind != Keyboard.KEY_GRAVE) {
            text += " [${Keyboard.getKeyName(module.keyBind).uppercase()}]"
        } else if (isBinding) {
            text = "binding..."
        }

        font.drawString(
            text,
            (x + width - font.getWidth(text) - 3).toFloat(),
            y + (height / 4.0f) + 0.5f,
            if (isExtended && module.state) moduleColor.rgb else Color(0xD2D2D2).rgb
        )

        if (isExtended) {
            elements.forEach { it.drawScreen(mouseX, mouseY, partialTicks) }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            when (button) {
                0 -> {
                    if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        isBinding = !isBinding
                    } else {
                        module.toggle()
                    }
                }
                1 -> if (module.values.isNotEmpty()) {
                    isExtended = !isExtended
                }
            }
        }

        if (isExtended) {
            elements.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isExtended) {
            elements.forEach { it.mouseReleased(mouseX, mouseY, state) }
        }
    }

    override fun keyTyped(character: Char, code: Int) {
        if (isBinding) {
            val keyCode = if (code == Keyboard.KEY_BACK) Keyboard.KEY_NONE else code
            module.keyBind = keyCode
            isBinding = false
        }

        if (isExtended) {
            elements.forEach { it.keyTyped(character, code) }
        }
    }
}