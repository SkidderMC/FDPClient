/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
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
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.config.BlockValue
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
    var isBindingSelection = false

    init {
        module.values.filter { it.shouldRender() }.forEach { value ->
            val element = when (value) {
                is BoolValue -> BooleanElement(this, value, parent, x + 4, y, width - 8, 12)
                is FloatValue -> FloatElement(this, value, parent, x + 4, y, width - 4, 12)
                is IntValue -> IntegerElement(this, value, parent, x + 4, y, width - 4, 12)
                is ListValue -> ListElement(this, value, parent, x + 4, y, width - 8, 12)
                is ColorValue -> ColorElement(this, value, parent, x + 4, y, width - 8, 12)
                is TextValue -> TextElement(this, value, parent, x + 4, y, width - 8, 12)
                is BlockValue -> BlockElement(this, value, parent, x + 4, y, width - 8, 12)
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
            val actualHeight = if (element is ColorElement) {
                element.getActualHeight()
            } else {
                element.height
            }
            elementY += actualHeight
        }
    }

    fun getExtendedHeight(): Float {
        return if (isExtended) {
            val totalHeight = elements.sumOf { element ->
                if (element is ColorElement) {
                    element.getActualHeight().toDouble()
                } else {
                    element.height.toDouble()
                }
            }.toFloat() + 2
            totalHeight
        } else {
            0.0f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        update()
        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return
        var moduleHeight = height

        if (isExtended) {
            moduleHeight += elements.sumOf { element ->
                if (element is ColorElement) {
                    element.getActualHeight()
                } else {
                    element.height
                }
            } + 2
        }

        val moduleColor = if (module.state) {
            yzyCategory.of(module.category)?.color ?: Color(37, 37, 37)
        } else {
            Color(37, 37, 37)
        }

        val backgroundColor = if (isExtended) Color(26, 26, 26) else moduleColor
        val textColor = if (module.state && !isExtended) Color.WHITE else Color(0xD2D2D2)

        if (isBindingSelection) {
            RenderUtils.yzyRectangle(
                x + 0.5f, y.toFloat(),
                (width - 1).toFloat(), moduleHeight.toFloat(),
                Color(255, 165, 0, 150) // Orange highlight for bind selection
            )
        } else {
            RenderUtils.yzyRectangle(
                x + 0.5f, y.toFloat(),
                (width - 1).toFloat(), moduleHeight.toFloat(),
                backgroundColor
            )
        }

        if (isHovering(mouseX, mouseY) && !isExtended && !isBindingSelection) {
            RenderUtils.yzyRectangle(
                x + 0.5f, y.toFloat(),
                (width - 1).toFloat(), height.toFloat(),
                Color(moduleColor.red, moduleColor.green, moduleColor.blue, 100)
            )
        }

        var text = module.name.lowercase()

        if (isBindingSelection) {
            text = "Select bind for ${module.name}..."
        } else if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && module.keyBind != Keyboard.KEY_GRAVE) {
            text += " [${Keyboard.getKeyName(module.keyBind).uppercase()}]"
        } else if (isBinding) {
            text = "binding..."
        }

        font.drawString(
            text,
            (x + width - font.getWidth(text) - 3).toFloat(),
            y + (height / 4.0f) + 0.5f,
            if (isExtended && module.state) moduleColor.rgb else textColor.rgb
        )

        if (module.values.filter { it.shouldRender() }.isNotEmpty()) {
            val indicator = if (isExtended) "▼" else "▶"
            font.drawString(
                indicator,
                (x + 2).toFloat(),
                y + (height / 4.0f) + 0.5f,
                Color(0xD2D2D2).rgb
            )
        }

        if (isBindingSelection) {
            RenderUtils.yzyRectangle(
                (x + width - 20).toFloat(), (y + 2).toFloat(),
                16f, (height - 4).toFloat(),
                Color(255, 165, 0, 200)
            )
            font.drawString(
                "B",
                (x + width - 16).toFloat(),
                y + (height / 4.0f) + 0.5f,
                Color.WHITE.rgb
            )
        }

        if (isExtended) {
            elements.forEach { it.drawScreen(mouseX, mouseY, partialTicks) }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            when (button) {
                0 -> {
                    if (isBindingSelection) {
                        // Cancel bind selection
                        isBindingSelection = false
                    } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        isBinding = !isBinding
                    } else {
                        module.toggle()
                    }
                }
                1 -> {
                    if (isBindingSelection) {
                        // Cancel bind selection
                        isBindingSelection = false
                    } else if (module.values.filter { it.shouldRender() }.isNotEmpty()) {
                        isExtended = !isExtended
                        // State is managed locally through isExtended property
                    }
                }
                2 -> {
                    isBindingSelection = !isBindingSelection
                    if (isBindingSelection) {
                        isBinding = false // Cancel regular binding if active
                    }
                }
            }
        }

        if (isExtended && !isBindingSelection) {
            elements.forEach { element ->
                if (element.isHovering(mouseX, mouseY)) {
                    element.mouseClicked(mouseX, mouseY, button)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isExtended && !isBindingSelection) {
            elements.forEach { it.mouseReleased(mouseX, mouseY, state) }
        }
    }

    override fun keyTyped(character: Char, code: Int) {
        if (isBindingSelection) {
            val keyCode = if (code == Keyboard.KEY_BACK || code == Keyboard.KEY_ESCAPE) {
                if (code == Keyboard.KEY_ESCAPE) {
                    isBindingSelection = false
                    return
                }
                Keyboard.KEY_NONE
            } else {
                code
            }
            module.keyBind = keyCode
            isBindingSelection = false
        } else if (isBinding) {
            val keyCode = if (code == Keyboard.KEY_BACK) Keyboard.KEY_NONE else code
            module.keyBind = keyCode
            isBinding = false
        }

        if (isExtended && !isBindingSelection) {
            elements.forEach { it.keyTyped(character, code) }
        }
    }
}