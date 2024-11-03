/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager.GUIManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.Pair
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import java.io.IOException

/**
 * @author opZywl
 */
class yzyGUI(private val clickGui: ClickGUIModule) : GuiScreen() {

    private val panels: MutableList<Panel> = ArrayList()
    private val guiManager: GUIManager = FDPClient.guiManager
    private var yShift = 0
    var slide: Double = 0.0
    var progress: Double = 0.0
    var lastMS: Long = System.currentTimeMillis()

    init {
        var panelX = 5
        for (category in yzyCategory.values()) {
            val positions = guiManager.getPositions(category)
            val panel: Panel

            if (!guiManager.positions.containsKey(category)) {
                panel = Panel(this, category, panelX, 5)
                panelX += panel.width + 5
            } else {
                panel = Panel(this, category, positions.key, positions.value)
                panel.isExtended = guiManager.extendeds[category] == true
            }

            guiManager.positions[category] = Pair(panel.x, panel.y)
            guiManager.extendeds[category] = panel.isExtended

            panels.add(panel)
            panel.isExtended = guiManager.isExtended(category)
        }
    }

    override fun initGui() {
        super.initGui()
        slide = 0.0
        progress = 0.0
        lastMS = System.currentTimeMillis()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        mc.gameSettings.guiScale = clickGui.lastScale
        panels.forEach { it.onGuiClosed() }
        mc.entityRenderer.loadEntityShader(null)
    }

    private fun handleScroll(wheel: Int) {
        if (wheel == 0) return
        panels.forEach { it.y += wheel }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (Mouse.hasWheel()) {
            val wheel = Mouse.getDWheel()
            var handledScroll = false

            for (i in panels.size - 1 downTo 0) {
                if (panels[i].handleScroll(mouseX, mouseY, wheel)) {
                    handledScroll = true
                    break
                }
            }

            if (!handledScroll) {
                handleScroll(wheel)
            }
        }

        panels.forEach { it.drawScreen(mouseX, mouseY, partialTicks) }
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val adjustedMouseY = mouseY + yShift
        panels.forEach { it.mouseClicked(mouseX, adjustedMouseY, mouseButton) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        val adjustedMouseY = mouseY + yShift
        panels.forEach { it.mouseReleased(mouseX, adjustedMouseY, state) }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        panels.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    fun getPanels(): List<Panel> {
        return panels
    }

    fun getClickGui(): ClickGUIModule {
        return clickGui
    }
}
