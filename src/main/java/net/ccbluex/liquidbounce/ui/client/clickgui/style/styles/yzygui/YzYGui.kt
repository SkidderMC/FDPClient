/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.SideGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager.GUIManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.utils.render.Pair
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

/**
 * @author opZywl - YZY GUI
 */
class YzYGui(private val clickGui: ClickGUIModule) : GuiScreen() {

    private val panels: MutableList<Panel> = mutableListOf()
    private val guiManager: GUIManager = FDPClient.guiManager
    private val sideGui = SideGui()
    private val hudIcon = ResourceLocation("${CLIENT_NAME.lowercase()}/custom_hud_icon.png")
    private var lastMS: Long = System.currentTimeMillis()
    private var yShift = 0
    private var slide: Double = 0.0
    private var progress: Double = 0.0
    val alpha = 255

    private var isInitialized = false
    private var bindSelectionMode = false
    private var selectedModuleForBind: Module? = null
    private var bindInputBox: BindInputBox? = null

    init {
        initializePanels()
    }

    private fun initializePanels() {
        if (isInitialized) return

        try {
            panels.clear()
            var panelX = 5
            yzyCategory.entries.forEach { category ->
                val positions = guiManager.getPositions(category)
                val panel = if (!guiManager.positions.containsKey(category)) {
                    Panel(this, category, panelX, 5).also { panelX += it.width + 5 }
                } else {
                    Panel(this, category, positions.key, positions.value ?: 5).apply {
                        isExtended = guiManager.extendeds[category] == true
                    }
                }
                guiManager.positions[category] = Pair(panel.x, panel.y)
                guiManager.extendeds[category] = panel.isExtended
                panels.add(panel)
                panel.isExtended = guiManager.isExtended(category)
            }
            isInitialized = true
        } catch (e: Exception) {
            println("Error initializing yzygui panels: ${e.message}")
        }
    }

    override fun initGui() {
        super.initGui()
        slide = 0.0
        progress = 0.0
        lastMS = System.currentTimeMillis()

        try {
            sideGui.initGui()
        } catch (e: Exception) {
            println("Error initializing sideGui: ${e.message}")
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()

        try {
            mc.gameSettings.guiScale = clickGui.lastScale
            panels.forEach { panel ->
                panel.onGuiClosed()
                // Save scroll position and extended states
                guiManager.positions[panel.category] = Pair(panel.x, panel.y)
                guiManager.extendeds[panel.category] = panel.isExtended
            }
            mc.entityRenderer.loadEntityShader(null)
        } catch (e: Exception) {
            println("Error during yzygui cleanup: ${e.message}")
        }
    }

    private fun handleScroll(wheel: Int) {
        if (wheel != 0) {
            val scrollAmount = if (wheel > 0) 30 else -30
            panels.forEach { panel ->
                val newY = panel.y + scrollAmount
                panel.y = newY
                // Update GUI manager positions
                guiManager.positions[panel.category] = Pair(panel.x, panel.y)
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            assumeNonVolatile = true

            if (Mouse.hasWheel()) {
                val wheel = Mouse.getDWheel()
                if (wheel != 0) {
                    val handledScroll = panels.asReversed().any { it.handleScroll(mouseX, mouseY, wheel) }
                    if (!handledScroll) {
                        handleScroll(wheel)
                    }
                }
            }

            if (hudIcon != null) {
                drawImage(hudIcon, 9, height - 41, 32, 32)
            }

            val delta = (System.currentTimeMillis() - lastMS).toInt()
            panels.forEach {
                it.updateFade(delta)
                it.drawScreen(mouseX, mouseY, partialTicks)
            }

            sideGui.drawScreen(mouseX, mouseY, partialTicks, alpha)

            bindInputBox?.let { box ->
                box.drawScreen(mouseX, mouseY, partialTicks)
            }

            lastMS = System.currentTimeMillis()

            drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

            assumeNonVolatile = false
        } catch (e: Exception) {
            println("Error during yzygui rendering: ${e.message}")
            assumeNonVolatile = false
        }
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        try {
            bindInputBox?.let { box ->
                if (box.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return
                }
            }

            val adjustedMouseY = mouseY + yShift

            if (mouseButton == 2) { // Middle click
                panels.forEach { panel ->
                    if (panel.isExtended) {
                        panel.elements.forEach { element ->
                            if (element.isHovering(mouseX, adjustedMouseY)) {
                                startBindSelection(element.module, mouseX, mouseY)
                                return
                            }
                        }
                    }
                }
            }

            panels.forEach { it.mouseClicked(mouseX, adjustedMouseY, mouseButton) }
            sideGui.mouseClicked(mouseX, mouseY, mouseButton)

            if (mouseX in 9 until 41 && mouseY in (height - 41) until height) {
                mc.displayGuiScreen(GuiHudDesigner())
            }
        } catch (e: Exception) {
            println("Error during mouse click handling: ${e.message}")
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        try {
            bindInputBox?.mouseReleased(mouseX, mouseY, state)

            val adjustedMouseY = mouseY + yShift
            panels.forEach { it.mouseReleased(mouseX, adjustedMouseY, state) }
            sideGui.mouseReleased(mouseX, mouseY, state)
        } catch (e: Exception) {
            println("Error during mouse release handling: ${e.message}")
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        try {
            if (bindSelectionMode && bindInputBox != null) {
                if (keyCode == 1) { // ESC key
                    bindSelectionMode = false
                    selectedModuleForBind = null
                    bindInputBox = null
                } else if (keyCode != 0) {
                    selectedModuleForBind?.keyBind = keyCode
                    bindSelectionMode = false
                    selectedModuleForBind = null
                    bindInputBox = null
                }
                return
            }

            super.keyTyped(typedChar, keyCode)
            panels.forEach { it.keyTyped(typedChar, keyCode) }
            sideGui.keyTyped(typedChar, keyCode)
        } catch (e: Exception) {
            println("Error during key handling: ${e.message}")
        }
    }

    fun resetGui() {
        isInitialized = false
        panels.clear()
        initializePanels()
    }

    private fun startBindSelection(module: Module, mouseX: Int, mouseY: Int) {
        bindSelectionMode = true
        selectedModuleForBind = module
        bindInputBox = BindInputBox(mouseX, mouseY, module.name)
    }
}

class BindInputBox(private val x: Int, private val y: Int, private val moduleName: String) {
    private val width = 120
    private val height = 40

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            // Draw background
            net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyRectangle(
                (x - width/2).toFloat(),
                (y - height/2).toFloat(),
                width.toFloat(),
                height.toFloat(),
                Color(40, 40, 40, 200)
            )

            // Draw border
            net.ccbluex.liquidbounce.utils.render.RenderUtils.yzyRectangle(
                (x - width/2 - 1).toFloat(),
                (y - height/2 - 1).toFloat(),
                (width + 2).toFloat(),
                (height + 2).toFloat(),
                Color(100, 100, 255, 150)
            )

            // Draw text
            net.ccbluex.liquidbounce.FDPClient.customFontManager["lato-bold-13"]?.drawStringWithShadow(
                "Bind: $moduleName",
                (x - width/2 + 5).toDouble(),
                (y - 10).toDouble(),
                -1
            )

            net.ccbluex.liquidbounce.FDPClient.customFontManager["lato-bold-11"]?.drawStringWithShadow(
                "Press any key...",
                (x - width/2 + 5).toDouble(),
                (y + 5).toDouble(),
                Color.GRAY.rgb
            )
        } catch (e: Exception) {
            println("Error drawing bind input box: ${e.message}")
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return mouseX in (x - width/2)..(x + width/2) && mouseY in (y - height/2)..(y + height/2)
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
}