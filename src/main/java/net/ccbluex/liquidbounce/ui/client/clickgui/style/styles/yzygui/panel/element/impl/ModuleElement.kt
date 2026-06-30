/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl
import net.ccbluex.liquidbounce.utils.input.safeKeyName

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.core.ValueDispatcher
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
import net.ccbluex.liquidbounce.config.FileValue
import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.MultiSelectValue
import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.config.Vec3Value
import net.ccbluex.liquidbounce.config.CurveValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorder
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
    private val elementValues = mutableListOf<Value<*>>()
    var isExtended = FDPClient.guiManager.isModuleExtended(module.name)
    private var isBinding = false
    var isBindingSelection = false

    init {
        module.values.forEach { value ->
            buildElement(value)?.let {
                elements.add(it)
                elementValues.add(value)
            }
        }
        update()
    }

    fun buildElement(value: Value<*>): PanelElement? = when (value) {
        is BoolValue -> BooleanElement(this, value, parent, x + 4, y, width - 8, 12)
        is FloatValue -> FloatElement(value, parent, x + 4, y, width - 4, 12)
        is IntValue -> IntegerElement(value, parent, x + 4, y, width - 4, 12)
        is FloatRangeValue -> RangeElement(value, parent, x + 4, y, width - 4, 12)
        is IntRangeValue -> RangeElement(value, parent, x + 4, y, width - 4, 12)
        is ListValue -> ListElement(this, value, parent, x + 4, y, width - 8, 12)
        is ColorValue -> ColorElement(this, value, parent, x + 4, y, width - 8, 12)
        is TextValue -> TextElement(this, value, parent, x + 4, y, width - 8, 12)
        is FileValue -> FileElement(this, value, parent, x + 4, y, width - 8, 12)
        is BlockValue -> BlockElement(this, value, parent, x + 4, y, width - 8, 12)
        is FontValue -> FontElement(this, value, parent, x + 4, y, width - 8, 12)
        is MultiSelectValue -> MultiSelectElement(this, value, parent, x + 4, y, width - 8, 12)
        is KeyBindValue -> KeyBindElement(this, value, parent, x + 4, y, width - 8, 12)
        is Vec3Value -> Vec3Element(this, value, parent, x + 4, y, width - 8, 12)
        is CurveValue -> CurveElement(this, value, parent, x + 4, y, width - 8, 56)
        is Configurable -> GroupElement(this, value, parent, x + 4, y, width - 8, MODULE_HEIGHT)
        else -> null
    }

    private fun update() {
        var elementY = y + height
        activeElements().forEach { (element, _) ->
            element.x = x + 4
            element.y = elementY
            elementY += panelHeight(element)
        }
    }

    fun getExtendedHeight(): Float {
        return if (isExtended) {
            val totalHeight = activeElements().sumOf { (element, _) ->
                panelHeight(element).toDouble()
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
            moduleHeight += activeElements().sumOf { (element, _) ->
                panelHeight(element)
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
            text += " [${(safeKeyName(module.keyBind) ?: "None").uppercase()}]"
        } else if (isBinding) {
            text = "binding..."
        }

        font.drawString(
            text,
            (x + width - font.getWidth(text) - 3).toFloat(),
            y + (height / 4.0f) + 0.5f,
            if (isExtended && module.state) moduleColor.rgb else textColor.rgb
        )

        if (ValueDispatcher.visibleDeep(module).isNotEmpty()) {
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
            activeElements().forEach { (element, _) -> element.drawScreen(mouseX, mouseY, partialTicks) }

            activeElements().forEach { (element, value) ->
                val description = value.description
                if (description != null && element.isHovering(mouseX, mouseY)) {
                    drawValueTooltip(description, mouseX, mouseY, font)
                }
            }
        }
    }

    private fun drawValueTooltip(text: String, mouseX: Int, mouseY: Int, font: FontRenderer) {
        val boxWidth = font.getWidth(text) + 6
        val boxHeight = 12f
        val tipX = (mouseX + 8).toFloat()
        val tipY = (mouseY - boxHeight - 2)
        drawRect(tipX, tipY, tipX + boxWidth, tipY + boxHeight, Color(20, 20, 20, 235).rgb)
        drawBorder(tipX, tipY, tipX + boxWidth, tipY + boxHeight, 1f, Color(60, 60, 60).rgb)
        font.drawString(text, tipX + 3f, tipY + 2.5f, -1)
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
                    } else if (ValueDispatcher.visibleDeep(module).isNotEmpty()) {
                        isExtended = !isExtended
                        FDPClient.guiManager.moduleExtendeds[module.name] = isExtended
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
            activeElements().forEach { (element, _) ->
                if (element.isHovering(mouseX, mouseY)) {
                    element.mouseClicked(mouseX, mouseY, button)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isExtended && !isBindingSelection) {
            activeElements().forEach { (element, _) -> element.mouseReleased(mouseX, mouseY, state) }
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
            activeElements().forEach { (element, _) -> element.keyTyped(character, code) }
        }
    }

    private fun activeElements() =
        elements.zip(elementValues).filter { (element, value) -> isElementVisible(element, value) }
}

/**
 * A value should render when it passes its own support gate, and - for a nested
 * group - only when it actually has something visible inside, so groups whose
 * children are all gated off for the current mode/style disappear instead of
 * leaving an empty header.
 */
private fun isElementVisible(element: PanelElement, value: Value<*>): Boolean =
    value.shouldRender() && (element !is GroupElement || element.hasVisibleContent())

/**
 * Dynamic on-screen height of a panel element, accounting for the elements that
 * grow (color picker, multi-select dropdown) and nested groups.
 */
private fun panelHeight(element: PanelElement): Int = when (element) {
    is ColorElement -> element.getActualHeight()
    is MultiSelectElement -> element.getActualHeight()
    is GroupElement -> element.getActualHeight()
    else -> element.height
}

/**
 * Collapsible nested value group - YZY GUI
 * @author opZywl
 */
class GroupElement(
    private val moduleElement: ModuleElement,
    private val group: Configurable,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    companion object {
        const val HEADER_HEIGHT = 13
    }

    private val children = mutableListOf<PanelElement>()
    private val childValues = mutableListOf<Value<*>>()

    init {
        group.values.forEach { value ->
            moduleElement.buildElement(value)?.let {
                children.add(it)
                childValues.add(value)
            }
        }
    }

    private fun activeChildren() =
        children.zip(childValues).filter { (element, value) -> isElementVisible(element, value) }

    fun hasVisibleContent(): Boolean = activeChildren().isNotEmpty()

    fun getActualHeight(): Int {
        var total = HEADER_HEIGHT
        if (group.groupExpanded) {
            total += activeChildren().sumOf { (element, _) -> panelHeight(element) }
        }
        return total
    }

    private fun layoutChildren() {
        var childY = y + HEADER_HEIGHT
        activeChildren().forEach { (element, _) ->
            element.x = x + 4
            element.y = childY
            childY += panelHeight(element)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        height = getActualHeight()

        val font: FontRenderer = FDPClient.customFontManager["lato-bold-15"] ?: return

        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), HEADER_HEIGHT.toFloat(),
            Color(33, 33, 33)
        )

        font.drawString(
            group.name,
            (x + 3).toFloat(),
            y + (HEADER_HEIGHT / 4.0f) + 0.5f,
            Color(0xC8C8C8).rgb
        )

        val indicator = if (group.groupExpanded) "▼" else "▶"
        font.drawString(
            indicator,
            (x + width - font.getWidth(indicator) - 4).toFloat(),
            y + (HEADER_HEIGHT / 4.0f) + 0.5f,
            Color(0xC8C8C8).rgb
        )

        if (group.groupExpanded) {
            layoutChildren()
            activeChildren().forEach { (element, _) -> element.drawScreen(mouseX, mouseY, partialTicks) }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (mouseY in y until y + HEADER_HEIGHT && mouseX in x..x + width) {
            if (button == 0 || button == 1) {
                group.groupExpanded = !group.groupExpanded
            }
            return
        }

        if (group.groupExpanded) {
            layoutChildren()
            activeChildren().forEach { (element, _) ->
                if (element.isHovering(mouseX, mouseY)) {
                    element.mouseClicked(mouseX, mouseY, button)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (group.groupExpanded) {
            activeChildren().forEach { (element, _) -> element.mouseReleased(mouseX, mouseY, state) }
        }
    }

    override fun keyTyped(character: Char, code: Int) {
        if (group.groupExpanded) {
            activeChildren().forEach { (element, _) -> element.keyTyped(character, code) }
        }
    }
}

/**
 * File Element - YZY GUI
 * @author opZywl
 */
class FileElement(
    private val element: ModuleElement,
    private val setting: FileValue,
    parent: Panel,
    x: Int,
    y: Int,
    width: Int,
    height: Int
) : PanelElement(parent, x, y, width, height) {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        RenderUtils.yzyRectangle(
            x.toFloat(), y.toFloat(),
            width.toFloat(), height.toFloat(),
            Color(26, 26, 26)
        )

        val font = FDPClient.customFontManager["lato-bold-15"]

        font?.drawString(
            setting.name,
            (x + 1).toFloat(),
            y + (height / 4.0f) + 0.5f,
            -1
        )

        val displayText = setting.shortName
        val textWidth = font?.getWidth(displayText) ?: 0
        font?.drawString(
            displayText,
            (x + width - textWidth - 10).toFloat(),
            y + (height / 4.0f) + 0.5f,
            Color(0xD2D2D2).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            setting.openDialog()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    override fun keyTyped(character: Char, code: Int) {}
}
