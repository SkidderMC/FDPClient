/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel

import net.ccbluex.liquidbounce.FDPClient.customFontManager
import net.ccbluex.liquidbounce.FDPClient.guiManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl.ModuleElement
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.YzYGui
import net.ccbluex.liquidbounce.utils.attack.CPSCounter.isHovering
import net.ccbluex.liquidbounce.utils.render.Pair
import net.ccbluex.liquidbounce.utils.render.RenderUtils.mc
import net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyRectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyTexture
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*

/**
 * @author opZywl - YZY GUI
 */
class Panel(
    val parent: YzYGui,
    val category: yzyCategory,
    var x: Int,
    var y: Int
) {
    val elements: MutableList<ModuleElement> = try {
        moduleManager.getModuleInCategory(category.parent).mapIndexed { index, module ->
            ModuleElement(module, this, x + 1, PANEL_HEIGHT + 1 + index * ModuleElement.MODULE_HEIGHT, PANEL_WIDTH - 2, ModuleElement.MODULE_HEIGHT)
        }.toMutableList()
    } catch (e: Exception) {
        println("Error initializing panel elements for ${category.name}: ${e.message}")
        mutableListOf()
    }

    var width: Int = PANEL_WIDTH
    var height: Int = PANEL_HEIGHT

    private var dragged: Int = 0
    private var lastX: Int = 0
    private var lastY: Int = 0

    var open: Boolean = false

    private var fade: Float = 0f
    private var isDragging: Boolean = false
    var isExtended: Boolean = false

    private val elementsHeight = 0f

    private val moduleStates = mutableMapOf<String, Boolean>()

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return isHovering(mouseX, mouseY, x, y, x + width, y + height)
    }

    fun handleScroll(mouseX: Int, mouseY: Int, wheel: Int): Boolean {
        try {
            if (mouseX in x..(x + width) && mouseY in y..(y + height + elementsHeight.toInt())) {
                when {
                    wheel > 0 -> {
                        dragged = (dragged - 1)
                        return true
                    }
                    wheel < 0 -> {
                        dragged = (dragged + 1)
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            println("Error handling scroll in panel ${category.name}: ${e.message}")
        }
        return false
    }

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            if (isDragging) {
                val newX = mouseX + lastX
                val newY = mouseY + lastY
                if (newX >= 0 && newX <= parent.width - width && newY >= 0 && newY <= parent.height - height) {
                    x = newX
                    y = newY
                }
            }

            var panelHeight = height.toFloat()

            for (element in elements) {
                if (isExtended) {
                    panelHeight += element.height.toFloat()
                }
                panelHeight += element.getExtendedHeight()
            }

            yzyRectangle(x - 0.5f, y - 0.5f, width + 1.0f, panelHeight + 3.0f, category.color)
            yzyRectangle(x.toFloat(), y.toFloat(), width.toFloat(), panelHeight + 2.0f, Color(26, 26, 26))

            customFontManager["lato-bold-15"]?.drawStringWithShadow(
                category.name.lowercase(Locale.getDefault()),
                (x + 3).toDouble(),
                (y + (height / 4.0f) + 0.5f).toDouble(),
                -1
            )

            try {
                pushMatrix()
                enableAlpha()
                enableBlend()

                mc.textureManager.bindTexture(ResourceLocation("fdpclient/texture/clickgui/eye.png"))
                val size = height - 7
                yzyTexture(
                    (x + width - size * 2 - 7).toDouble(),
                    (y + (height / 4.0f)).toDouble(),
                    0.0f, 0.0f, size.toDouble(), size.toDouble(), size.toFloat(), size.toFloat(), category.color
                )

                mc.textureManager.bindTexture(category.getIcon())
                yzyTexture(
                    (x + width - size - 3).toDouble(),
                    (y + (height / 4.0f)).toDouble(),
                    0.0f, 0.0f, size.toDouble(), size.toDouble(), size.toFloat(), size.toFloat(), category.color
                )

                disableBlend()
                disableAlpha()
                popMatrix()
            } catch (e: Exception) {
                println("Error rendering textures for panel ${category.name}: ${e.message}")
            }

            if (isExtended) {
                var addition = height
                elements.forEach { element ->
                    element.x = x + 1
                    element.y = y + addition
                    element.drawScreen(mouseX, mouseY, partialTicks)
                    addition += element.height + if (element.isExtended) element.getExtendedHeight().toInt() else 0
                }
            }
        } catch (e: Exception) {
            println("Error drawing panel ${category.name}: ${e.message}")
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        if (isHovering(mouseX, mouseY)) {
            when (button) {
                0 -> {
                    isDragging = true
                    lastX = x - mouseX
                    lastY = y - mouseY
                }
                1 -> {
                    var clickedOnElement = false
                    if (isExtended) {
                        elements.forEach { element ->
                            if (element.isHovering(mouseX, mouseY)) {
                                clickedOnElement = true
                                return@forEach
                            }
                        }
                    }

                    if (!clickedOnElement) {
                        isExtended = !isExtended
                        guiManager.extendeds[category] = isExtended

                        if (!isExtended) {
                            elements.forEach { element ->
                                moduleStates[element.module.name] = element.isExtended
                                element.isExtended = false
                            }
                        } else {
                            elements.forEach { element ->
                                element.isExtended = moduleStates[element.module.name] ?: false
                            }
                        }
                    }
                }
            }
        }

        if (isExtended) {
            elements.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        if (isExtended) elements.forEach { it.mouseReleased(mouseX, mouseY, state) }
    }

    fun keyTyped(character: Char, code: Int) {
        if (isExtended) elements.forEach { it.keyTyped(character, code) }
    }

    fun updateFade(delta: Int) {
        fade = when {
            open && fade < elementsHeight -> fade + 0.4f * delta
            !open && fade > 0 -> fade - 0.4f * delta
            else -> fade
        }.coerceIn(0f, elementsHeight)
    }

    fun onGuiClosed() {
        guiManager.positions[category] = Pair(x, y)
        guiManager.extendeds[category] = isExtended

        elements.forEach { element ->
            moduleStates[element.module.name] = element.isExtended
        }
    }

    fun hasActiveOverlay(): Boolean {
        return elements.any { element ->
            element.isExtended && element.isBindingSelection
        }
    }

    fun renderOverlays(mouseX: Int, mouseY: Int, partialTicks: Float) {

    }

    fun restoreState() {
        val savedPosition = guiManager.positions[category]
        if (savedPosition != null) {
            x = savedPosition.key
            y = savedPosition.value ?: y
        }

        isExtended = guiManager.extendeds[category] ?: false

        elements.forEach { element ->
            element.isExtended = moduleStates[element.module.name] ?: false
        }
    }

    companion object {
        const val PANEL_WIDTH: Int = 100
        const val PANEL_HEIGHT: Int = 15
    }
}