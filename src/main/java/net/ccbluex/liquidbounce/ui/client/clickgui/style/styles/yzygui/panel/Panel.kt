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
import net.ccbluex.liquidbounce.utils.render.RenderHelper
import net.ccbluex.liquidbounce.utils.render.RenderUtils.mc
import net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyRectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyTexture
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
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
            val contentSpan = if (isExtended || parent.search.active) contentHeight() else 0
            if (mouseX in x..(x + width) && mouseY in y..(y + height + contentSpan)) {
                if (maxScrollPx() == 0) return false
                when {
                    wheel > 0 -> {
                        dragged -= 1
                        scrollPx()
                        return true
                    }
                    wheel < 0 -> {
                        dragged += 1
                        scrollPx()
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            println("Error handling scroll in panel ${category.name}: ${e.message}")
        }
        return false
    }

    private fun contentHeight(): Int =
        visibleElements().sumOf { it.height + it.getExtendedHeight().toInt() }

    private fun maxScrollPx(): Int {
        if (!isExtended && !parent.search.active) return 0
        val viewport = parent.height - (y + height) - 4
        return (contentHeight() - viewport).coerceAtLeast(0)
    }

    private fun scrollPx(): Int {
        val max = maxScrollPx()
        val maxSteps = (max + SCROLL_STEP - 1) / SCROLL_STEP
        dragged = dragged.coerceIn(0, maxSteps)
        return (dragged * SCROLL_STEP).coerceAtMost(max)
    }

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            val visibleElements = visibleElements()
            if (parent.search.active && visibleElements.isEmpty()) return

            if (isDragging) {
                val newX = mouseX + lastX
                val newY = mouseY + lastY
                if (newX >= 0 && newX <= parent.width - width && newY >= 0 && newY <= parent.height - height) {
                    x = newX
                    y = newY
                }
            }

            var panelHeight = height.toFloat()

            for (element in visibleElements) {
                if (isExtended || parent.search.active) {
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

            if (isExtended || parent.search.active) {
                val scrollOffset = scrollPx()
                val viewportTop = y + height
                if (scrollOffset > 0) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST)
                    RenderHelper.scissor(
                        (x - 1).toDouble(),
                        viewportTop.toDouble(),
                        (width + 2).toDouble(),
                        (parent.height - viewportTop).toDouble()
                    )
                }
                var addition = height - scrollOffset
                visibleElements.forEach { element ->
                    element.x = x + 1
                    element.y = y + addition
                    element.drawScreen(mouseX, mouseY, partialTicks)
                    addition += element.height + if (element.isExtended) element.getExtendedHeight().toInt() else 0
                }
                if (scrollOffset > 0) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST)
                }
            }
        } catch (e: Exception) {
            println("Error drawing panel ${category.name}: ${e.message}")
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        val visibleElements = visibleElements()
        if (parent.search.active && visibleElements.isEmpty()) return

        if (isHovering(mouseX, mouseY)) {
            when (button) {
                0 -> {
                    isDragging = true
                    lastX = x - mouseX
                    lastY = y - mouseY
                }
                1 -> {
                    var clickedOnElement = false
                    if (isExtended || parent.search.active) {
                        visibleElements.forEach { element ->
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
                                element.isExtended = guiManager.moduleExtendeds[element.module.name]
                                    ?: moduleStates[element.module.name] ?: false
                            }
                        }
                    }
                }
            }
        }

        if ((isExtended || parent.search.active) && mouseY > y + height) {
            visibleElements.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        if (isExtended || parent.search.active) visibleElements().forEach { it.mouseReleased(mouseX, mouseY, state) }
    }

    fun keyTyped(character: Char, code: Int) {
        if (isExtended || parent.search.active) visibleElements().forEach { it.keyTyped(character, code) }
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
            guiManager.moduleExtendeds[element.module.name] = element.isExtended
        }
    }

    fun hasActiveOverlay(): Boolean {
        return visibleElements().any { element ->
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

    fun visibleElements(): List<ModuleElement> {
        if (!parent.search.active) return elements
        return elements.filter { parent.search.matches(it.module) }
    }

    companion object {
        const val PANEL_WIDTH: Int = 100
        const val PANEL_HEIGHT: Int = 15
        const val SCROLL_STEP: Int = ModuleElement.MODULE_HEIGHT
    }
}
